package com.gymjf.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Reglas de negocio que viven en MySQL (triggers): aforo, stock y saldo. Verifica que la API las
 * respete y que los rechazos lleguen como 409 con el mensaje en español (nunca 500).
 */
class DatabaseRulesTest extends AbstractIntegrationTest {

    private int stock(Session admin, Integer productId) throws Exception {
        return read(GET("/api/v1/products/" + productId, admin)).get("stock").asInt();
    }

    private double saldo(Session admin, Integer cuentaId) throws Exception {
        return read(GET("/api/v1/accounts-receivable/" + cuentaId, admin)).get("pendingBalance").asDouble();
    }

    @Test
    @DisplayName("Aforo: la reserva que supera el cupo se rechaza con 409 y su mensaje")
    void aforoMaximo() throws Exception {
        Session admin = admin();
        Session a = registerCustomer();
        Session b = registerCustomer();
        Integer horario = create("/api/v1/schedules", admin, Map.of(
                "activity", "Zumba-" + UUID.randomUUID().toString().substring(0, 6), "dayOfWeek", "MONDAY",
                "startTime", "08:00:00", "endTime", "09:00:00", "maxCapacity", 1));

        POST("/api/v1/reservations", a, Map.of("userId", a.id(), "scheduleId", horario, "reservationDate", "2030-01-07"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("PENDING"));
        POST("/api/v1/reservations", b, Map.of("userId", b.id(), "scheduleId", horario, "reservationDate", "2030-01-07"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("Aforo")));
        // Otra fecha del mismo horario sí tiene cupo
        POST("/api/v1/reservations", b, Map.of("userId", b.id(), "scheduleId", horario, "reservationDate", "2030-01-14"))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Stock: crear, editar y borrar un detalle de pedido ajusta el inventario; sin existencias es 409")
    void stockDePedidos() throws Exception {
        Session admin = admin();
        Session cliente = registerCustomer();
        Integer producto = create("/api/v1/products", admin, Map.of(
                "name", "Prod-" + UUID.randomUUID().toString().substring(0, 8), "category", "SUPPLEMENTS",
                "price", 10, "stock", 10, "minStockAlert", 1));
        Integer pedido = create("/api/v1/orders", cliente, Map.of(
                "userId", cliente.id(), "code", "T-" + UUID.randomUUID().toString().substring(0, 8), "total", 30));

        Integer detalle = create("/api/v1/order-details", cliente,
                Map.of("orderId", pedido, "productId", producto, "quantity", 3, "unitPrice", 10));
        assertEquals(7, stock(admin, producto));

        PUT("/api/v1/order-details/" + detalle, admin, Map.of("quantity", 5)).andExpect(status().isOk());
        assertEquals(5, stock(admin, producto));
        PUT("/api/v1/order-details/" + detalle, admin, Map.of("quantity", 1)).andExpect(status().isOk());
        assertEquals(9, stock(admin, producto));

        PUT("/api/v1/order-details/" + detalle, admin, Map.of("quantity", 50)).andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("Stock insuficiente")));
        assertEquals(9, stock(admin, producto)); // el rechazo no cambió nada

        POST("/api/v1/order-details", cliente, Map.of("orderId", pedido, "productId", producto, "quantity", 100, "unitPrice", 10))
                .andExpect(status().isConflict());

        DELETE("/api/v1/order-details/" + detalle, admin).andExpect(status().isNoContent());
        assertEquals(10, stock(admin, producto));
    }

    @Test
    @DisplayName("Saldo: aprobar, editar, rechazar y borrar un pago mantiene coherente la cuenta por cobrar")
    void saldoDeCuentasPorCobrar() throws Exception {
        Session admin = admin();
        Session cliente = registerCustomer();
        Integer cuenta = create("/api/v1/accounts-receivable", admin, Map.of(
                "userId", cliente.id(), "totalDebt", 1000, "pendingBalance", 1000, "issueDate", "2030-01-01",
                "delinquencyStatus", "OVERDUE"));

        Integer pago = create("/api/v1/transactions", admin, Map.of(
                "userId", cliente.id(), "amount", 400, "confirmationAmount", 400, "paymentMethod", "CASH",
                "accountReceivableId", cuenta));
        assertEquals(1000.0, saldo(admin, cuenta)); // pendiente: todavía no descuenta

        POST("/api/v1/transactions/" + pago + "/approve", admin, Map.of()).andExpect(status().isOk());
        assertEquals(600.0, saldo(admin, cuenta));

        POST("/api/v1/transactions/" + pago + "/reject", admin, Map.of()).andExpect(status().isOk());
        assertEquals(1000.0, saldo(admin, cuenta));

        POST("/api/v1/transactions/" + pago + "/approve", admin, Map.of()).andExpect(status().isOk());
        DELETE("/api/v1/transactions/" + pago, admin).andExpect(status().isNoContent());
        assertEquals(1000.0, saldo(admin, cuenta)); // borrar un pago aprobado devuelve el saldo
    }

    @Test
    @DisplayName("Saldo: un pago que cubre toda la deuda deja la cuenta AL DÍA (UP_TO_DATE)")
    void pagoTotalDejaCuentaAlDia() throws Exception {
        Session admin = admin();
        Session cliente = registerCustomer();
        Integer cuenta = create("/api/v1/accounts-receivable", admin, Map.of(
                "userId", cliente.id(), "totalDebt", 500, "pendingBalance", 500, "issueDate", "2030-01-01",
                "delinquencyStatus", "OVERDUE"));
        create("/api/v1/transactions", admin, Map.of(
                "userId", cliente.id(), "amount", 500, "paymentMethod", "TRANSFER", "accountReceivableId", cuenta,
                "validationStatus", "APPROVED", "referenceNumber", "REF-" + UUID.randomUUID()));
        GET("/api/v1/accounts-receivable/" + cuenta, admin)
                .andExpect(jsonPath("$.pendingBalance").value(0.0))
                .andExpect(jsonPath("$.delinquencyStatus").value("UP_TO_DATE"));
    }
}
