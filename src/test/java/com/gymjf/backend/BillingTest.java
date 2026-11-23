package com.gymjf.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import tools.jackson.databind.JsonNode;

/** Fase 2.3: aprobación de pagos, referencias duplicadas, efectivo, morosidad, cierre de caja y SINPE verificado. */
class BillingTest extends AbstractIntegrationTest {

    private Integer plan(Session admin, int precio) throws Exception {
        return create("/api/v1/subscription-plans", admin,
                Map.of("planName", "Plan " + UUID.randomUUID(), "price", precio, "durationDays", 30, "type", "MEMBERSHIP"));
    }

    /** El cliente renueva: nace una suscripción PENDING con su deuda. Devuelve [idSuscripción, idDeuda]. */
    private Integer[] renovar(Session admin, Session cliente, int precio) throws Exception {
        Integer plan = plan(admin, precio);
        JsonNode sub = read(POST("/api/v1/user-subscriptions/renew", cliente,
                Map.of("userId", cliente.id(), "planId", plan)).andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING")).andExpect(jsonPath("$.accountReceivableId").exists()));
        return new Integer[] { sub.get("id").asInt(), sub.get("accountReceivableId").asInt() };
    }

    private double saldo(Session admin, Integer cuenta) throws Exception {
        return read(GET("/api/v1/accounts-receivable/" + cuenta, admin)).get("pendingBalance").asDouble();
    }

    private String ref() {
        return "SINPE-" + UUID.randomUUID();
    }

    @Test
    @DisplayName("SINPE del cliente: queda pendiente, el admin aprueba, no se paga de más ni se repite la referencia")
    void sinpeConAprobacion() throws Exception {
        Session admin = admin();
        Session cliente = registerCustomer();
        Session otro = registerCustomer();
        Integer[] ids = renovar(admin, cliente, 10000);
        Integer cuenta = ids[1];
        String ref1 = ref();

        Integer pago = read(POST("/api/v1/transactions/submit", cliente,
                Map.of("accountReceivableId", cuenta, "amount", 4000, "referenceNumber", ref1))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.validationStatus").value("PENDING"))
                .andExpect(jsonPath("$.paymentMethod").value("MOBILE_PAYMENT"))).get("id").asInt();
        assertEquals(10000.0, saldo(admin, cuenta)); // pendiente: todavía no descuenta

        // referencia repetida -> 409; monto mayor al saldo -> 400; deuda ajena -> 403; sin referencia -> 400
        POST("/api/v1/transactions/submit", cliente,
                Map.of("accountReceivableId", cuenta, "amount", 100, "referenceNumber", ref1))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("duplicado")));
        POST("/api/v1/transactions/submit", cliente,
                Map.of("accountReceivableId", cuenta, "amount", 99999, "referenceNumber", ref()))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("supera el saldo")));
        POST("/api/v1/transactions/submit", otro,
                Map.of("accountReceivableId", cuenta, "amount", 100, "referenceNumber", ref())).andExpect(status().isForbidden());
        POST("/api/v1/transactions/submit", cliente,
                Map.of("accountReceivableId", cuenta, "amount", 100)).andExpect(status().isBadRequest());

        // el cliente no aprueba; el admin sí
        POST("/api/v1/transactions/" + pago + "/approve", cliente, null).andExpect(status().isForbidden());
        POST("/api/v1/transactions/" + pago + "/approve", admin, null).andExpect(status().isOk())
                .andExpect(jsonPath("$.validationStatus").value("APPROVED"));
        assertEquals(6000.0, saldo(admin, cuenta));
        POST("/api/v1/transactions/" + pago + "/approve", admin, null).andExpect(status().isConflict());
        GET("/api/v1/user-subscriptions/" + ids[0], admin).andExpect(jsonPath("$.status").value("PENDING")); // falta pagar

        // el resto: al quedar en cero la deuda queda al día y la suscripción pasa a ACTIVE
        Integer pago2 = read(POST("/api/v1/transactions/submit", cliente,
                Map.of("accountReceivableId", cuenta, "amount", 6000, "referenceNumber", ref()))
                .andExpect(status().isCreated())).get("id").asInt();
        POST("/api/v1/transactions/" + pago2 + "/approve", admin, null).andExpect(status().isOk());
        GET("/api/v1/accounts-receivable/" + cuenta, admin)
                .andExpect(jsonPath("$.pendingBalance").value(0.0)).andExpect(jsonPath("$.delinquencyStatus").value("UP_TO_DATE"));
        GET("/api/v1/user-subscriptions/" + ids[0], admin).andExpect(jsonPath("$.status").value("ACTIVE"));

        // un pago rechazado no descuenta; el cliente solo ve los suyos
        Integer[] otra = renovar(admin, otro, 5000);
        Integer malo = read(POST("/api/v1/transactions/submit", otro,
                Map.of("accountReceivableId", otra[1], "amount", 5000, "referenceNumber", ref()))
                .andExpect(status().isCreated())).get("id").asInt();
        POST("/api/v1/transactions/" + malo + "/reject", admin, null).andExpect(status().isOk())
                .andExpect(jsonPath("$.validationStatus").value("REJECTED"));
        assertEquals(5000.0, saldo(admin, otra[1]));
        GET("/api/v1/transactions", cliente).andExpect(jsonPath("$.totalElements").value(2));
        GET("/api/v1/transactions/" + malo, cliente).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Efectivo: exige confirmar el monto; los listados se pueden filtrar")
    void efectivoYFiltros() throws Exception {
        Session admin = admin();
        Session cliente = registerCustomer();

        POST("/api/v1/transactions", admin, Map.of("userId", cliente.id(), "amount", 2500, "paymentMethod", "CASH"))
                .andExpect(status().isBadRequest());
        POST("/api/v1/transactions", admin,
                Map.of("userId", cliente.id(), "amount", 2500, "confirmationAmount", 2000, "paymentMethod", "CASH"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("confirmar")));
        POST("/api/v1/transactions", admin,
                Map.of("userId", cliente.id(), "amount", 2500, "confirmationAmount", 2500, "paymentMethod", "CASH",
                        "validationStatus", "APPROVED")).andExpect(status().isCreated());
        POST("/api/v1/transactions", admin,
                Map.of("userId", cliente.id(), "amount", 900, "paymentMethod", "TRANSFER", "referenceNumber", ref()))
                .andExpect(status().isCreated());

        String base = "/api/v1/transactions?userId=" + cliente.id();
        GET(base, admin).andExpect(jsonPath("$.totalElements").value(2));
        GET(base + "&method=CASH", admin).andExpect(jsonPath("$.totalElements").value(1));
        GET(base + "&status=PENDING", admin).andExpect(jsonPath("$.totalElements").value(1));
        GET(base + "&from=2099-01-01", admin).andExpect(jsonPath("$.totalElements").value(0));
        GET(base + "&method=VOLAR", admin).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Morosidad: deuda mayor a ₡15 000 o de más de 30 días bloquea la entrada; al pagar se desbloquea")
    void morosidad() throws Exception {
        Session admin = admin();
        Session cliente = registerCustomer();
        Integer plan = plan(admin, 1000);
        // Tiene un plan activo (registrado por el admin, sin deuda): entra mientras no deba nada
        create("/api/v1/user-subscriptions", admin,
                Map.of("userId", cliente.id(), "planId", plan, "status", "ACTIVE"));
        GET("/api/v1/access/check?userId=" + cliente.id(), cliente)
                .andExpect(jsonPath("$.allowed").value(true)).andExpect(jsonPath("$.delinquent").value(false));

        // Una deuda nueva de más de ₡15 000 todavía no cuenta (días de gracia): puede renovar por adelantado
        create("/api/v1/accounts-receivable", admin, Map.of(
                "userId", cliente.id(), "totalDebt", 20000, "pendingBalance", 20000,
                "issueDate", java.time.LocalDate.now().toString(), "delinquencyStatus", "OVERDUE"));
        GET("/api/v1/billing/delinquency/" + cliente.id(), cliente).andExpect(jsonPath("$.delinquent").value(false))
                .andExpect(jsonPath("$.totalPending").value(20000.0)).andExpect(jsonPath("$.totalOverdue").value(0.0));

        // Pasados los días de gracia, más del monto límite sí es morosidad
        Integer deuda = create("/api/v1/accounts-receivable", admin, Map.of(
                "userId", cliente.id(), "totalDebt", 16000, "pendingBalance", 16000,
                "issueDate", java.time.LocalDate.now().minusDays(10).toString(), "delinquencyStatus", "OVERDUE"));
        GET("/api/v1/billing/delinquency/" + cliente.id(), cliente)
                .andExpect(jsonPath("$.delinquent").value(true)).andExpect(jsonPath("$.totalPending").value(36000.0)).andExpect(jsonPath("$.totalOverdue").value(16000.0));
        GET("/api/v1/access/check?userId=" + cliente.id(), cliente)
                .andExpect(jsonPath("$.allowed").value(false)).andExpect(jsonPath("$.delinquent").value(true))
                .andExpect(jsonPath("$.reason").value(org.hamcrest.Matchers.containsString("deuda vencida")));
        GET("/api/v1/billing/delinquents", admin).andExpect(status().isOk());
        GET("/api/v1/billing/delinquents", cliente).andExpect(status().isForbidden());

        // Pagar lo que debe lo desbloquea
        POST("/api/v1/transactions/sinpe", admin, Map.of("userId", cliente.id(), "accountReceivableId", deuda,
                "amount", 16000, "referenceNumber", ref())).andExpect(status().isCreated());
        GET("/api/v1/access/check?userId=" + cliente.id(), cliente)
                .andExpect(jsonPath("$.allowed").value(true)).andExpect(jsonPath("$.delinquent").value(false));

        // Una deuda pequeña pero de más de 30 días también cuenta; la tarea nocturna la marca IN_COLLECTION
        Integer vieja = create("/api/v1/accounts-receivable", admin, Map.of(
                "userId", cliente.id(), "totalDebt", 100, "pendingBalance", 100, "issueDate", "2020-01-01",
                "delinquencyStatus", "OVERDUE"));
        GET("/api/v1/billing/delinquency/" + cliente.id(), cliente).andExpect(jsonPath("$.delinquent").value(true));
        POST("/api/v1/billing/delinquency/mark", cliente, null).andExpect(status().isForbidden());
        JsonNode marcadas = read(POST("/api/v1/billing/delinquency/mark", admin, null).andExpect(status().isOk()));
        assertTrue(marcadas.get("updated").asInt() >= 1);
        GET("/api/v1/accounts-receivable/" + vieja, admin).andExpect(jsonPath("$.delinquencyStatus").value("IN_COLLECTION"));

        // Un cliente no ve la morosidad de otro
        GET("/api/v1/billing/delinquency/" + cliente.id(), registerCustomer()).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("SINPE verificado por el admin (procedimiento almacenado): aprobado, saldo y suscripción en un paso")
    void sinpeVerificado() throws Exception {
        Session admin = admin();
        Session cliente = registerCustomer();
        Integer[] ids = renovar(admin, cliente, 7000);
        String referencia = ref();
        Map<String, Object> pago = Map.of("userId", cliente.id(), "accountReceivableId", ids[1],
                "amount", 7000, "referenceNumber", referencia);

        POST("/api/v1/transactions/sinpe", cliente, pago).andExpect(status().isForbidden());
        POST("/api/v1/transactions/sinpe", admin, Map.of("userId", cliente.id(), "accountReceivableId", ids[1],
                "amount", 9000, "referenceNumber", ref())).andExpect(status().isBadRequest());
        POST("/api/v1/transactions/sinpe", admin, pago).andExpect(status().isCreated())
                .andExpect(jsonPath("$.validationStatus").value("APPROVED"))
                .andExpect(jsonPath("$.paymentMethod").value("MOBILE_PAYMENT"));
        assertEquals(0.0, saldo(admin, ids[1]));
        GET("/api/v1/user-subscriptions/" + ids[0], admin).andExpect(jsonPath("$.status").value("ACTIVE"));
        POST("/api/v1/transactions/sinpe", admin, pago).andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Cierre de caja y resumen: solo el administrador; suman los pagos aprobados del día")
    void reportes() throws Exception {
        Session admin = admin();
        Session cliente = registerCustomer();
        POST("/api/v1/transactions", admin,
                Map.of("userId", cliente.id(), "amount", 777, "confirmationAmount", 777, "paymentMethod", "CASH",
                        "validationStatus", "APPROVED")).andExpect(status().isCreated());

        JsonNode cierre = read(GET("/api/v1/billing/cash-closing", admin).andExpect(status().isOk()));
        assertTrue(cierre.get("totalCollected").asDouble() >= 777);
        assertTrue(cierre.get("byMethod").toString().contains("CASH"));
        GET("/api/v1/billing/cash-closing?date=2001-01-01", admin).andExpect(jsonPath("$.totalCollected").value(0));
        GET("/api/v1/billing/summary", admin).andExpect(status().isOk())
                .andExpect(jsonPath("$.openDebtTotal").exists()).andExpect(jsonPath("$.pendingApprovalCount").exists());
        GET("/api/v1/billing/summary?from=2026-12-01&to=2026-11-01", admin).andExpect(status().isBadRequest());
        GET("/api/v1/billing/cash-closing", cliente).andExpect(status().isForbidden());
        GET("/api/v1/billing/summary", cliente).andExpect(status().isForbidden());
    }
}
