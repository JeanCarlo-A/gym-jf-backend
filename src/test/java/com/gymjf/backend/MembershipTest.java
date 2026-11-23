package com.gymjf.backend;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Fase 2.2: tipos de plan, vencimiento calculado, modo de acceso, vencimientos y renovación. */
class MembershipTest extends AbstractIntegrationTest {

    private static final LocalDate HOY = LocalDate.now(ZoneId.of("America/Costa_Rica"));

    private Integer plan(Session admin, String nombre, int dias, String tipo) throws Exception {
        return create("/api/v1/subscription-plans", admin,
                Map.of("planName", nombre, "price", 25000, "durationDays", dias, "type", tipo));
    }

    private void modo(Session admin, String modo) throws Exception {
        PUT("/api/v1/gym-settings", admin, Map.of("accessMode", modo)).andExpect(status().isOk());
    }

    @Test
    @DisplayName("El pase diario siempre dura 1 día y solo un ADMIN crea planes")
    void tiposDePlan() throws Exception {
        Session admin = admin();
        POST("/api/v1/subscription-plans", registerCustomer(),
                Map.of("planName", "X", "price", 1, "durationDays", 30)).andExpect(status().isForbidden());

        Integer id = plan(admin, "Pase diario", 5, "DAY_PASS");
        GET("/api/v1/subscription-plans/" + id, admin).andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("DAY_PASS")).andExpect(jsonPath("$.durationDays").value(1));

        // Sin tipo, es una membresía normal
        POST("/api/v1/subscription-plans", admin, Map.of("planName", "Mensual", "price", 25000, "durationDays", 30))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.type").value("MEMBERSHIP"));
        POST("/api/v1/subscription-plans", admin,
                Map.of("planName", "Raro", "price", 1, "durationDays", 30, "type", "VOLAR"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("La fecha de vencimiento se calcula con la duración del plan; sin estado nace PENDING")
    void vencimientoCalculado() throws Exception {
        Session admin = admin();
        Session cliente = registerCustomer();
        Integer mensual = plan(admin, "Mensual calc", 30, "MEMBERSHIP");
        Integer diario = plan(admin, "Diario calc", 1, "DAY_PASS");

        POST("/api/v1/user-subscriptions", admin,
                Map.of("userId", cliente.id(), "planId", mensual, "startDate", "2030-01-01"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.endDate").value("2030-01-30"))
                .andExpect(jsonPath("$.status").value("PENDING"));
        POST("/api/v1/user-subscriptions", admin,
                Map.of("userId", cliente.id(), "planId", diario, "startDate", "2030-01-01"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.endDate").value("2030-01-01"));
        POST("/api/v1/user-subscriptions", admin, Map.of("userId", cliente.id(), "planId", mensual))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.startDate").value(HOY.toString()))
                .andExpect(jsonPath("$.endDate").value(HOY.plusDays(29).toString()));
        POST("/api/v1/user-subscriptions", admin, Map.of("userId", cliente.id(), "planId", 999999))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Modo de acceso: solo un ADMIN lo cambia; en SELECTIVE solo entra quien tiene suscripción vigente")
    void modoDeAcceso() throws Exception {
        Session admin = admin();
        Session cliente = registerCustomer();
        Session otro = registerCustomer();
        Integer mensual = plan(admin, "Mensual acceso", 30, "MEMBERSHIP");

        PUT("/api/v1/gym-settings", cliente, Map.of("accessMode", "SELECTIVE")).andExpect(status().isForbidden());
        PUT("/api/v1/gym-settings", admin, Map.of("accessMode", "NADA")).andExpect(status().isBadRequest());
        GET("/api/v1/gym-settings", cliente).andExpect(status().isOk());

        modo(admin, "FREE");
        GET("/api/v1/access/check?userId=" + cliente.id(), cliente).andExpect(status().isOk())
                .andExpect(jsonPath("$.allowed").value(true));

        modo(admin, "SELECTIVE");
        GET("/api/v1/access/check?userId=" + cliente.id(), cliente).andExpect(status().isOk())
                .andExpect(jsonPath("$.allowed").value(false)).andExpect(jsonPath("$.accessMode").value("SELECTIVE"));

        // PENDING (sin pagar) todavía no da acceso; ACTIVE sí
        Integer sub = create("/api/v1/user-subscriptions", admin,
                Map.of("userId", cliente.id(), "planId", mensual));
        GET("/api/v1/access/check?userId=" + cliente.id(), admin).andExpect(jsonPath("$.allowed").value(false));
        PUT("/api/v1/user-subscriptions/" + sub, admin, Map.of("status", "ACTIVE")).andExpect(status().isOk());
        GET("/api/v1/access/check?userId=" + cliente.id(), cliente).andExpect(jsonPath("$.allowed").value(true))
                .andExpect(jsonPath("$.validUntil").value(HOY.plusDays(29).toString()))
                .andExpect(jsonPath("$.subscriptionId").value(sub));

        // Nadie consulta el acceso de otro, salvo el admin
        GET("/api/v1/access/check?userId=" + cliente.id(), otro).andExpect(status().isForbidden());
        GET("/api/v1/access/check?userId=99999999", admin).andExpect(status().isNotFound());
        modo(admin, "FREE");
    }

    @Test
    @DisplayName("La revisión de vencimientos pasa a EXPIRED lo que ya cumplió su último día y quita el acceso")
    void vencimientos() throws Exception {
        Session admin = admin();
        Session cliente = registerCustomer();
        Integer mensual = plan(admin, "Mensual venc", 30, "MEMBERSHIP");
        Integer diario = plan(admin, "Diario venc", 1, "DAY_PASS");

        Integer vieja = create("/api/v1/user-subscriptions", admin,
                Map.of("userId", cliente.id(), "planId", mensual, "startDate", HOY.minusDays(40).toString(),
                        "status", "ACTIVE"));
        // El pase diario de hoy sigue vigente todo el día; el de ayer ya venció
        Integer deHoy = create("/api/v1/user-subscriptions", admin,
                Map.of("userId", cliente.id(), "planId", diario, "startDate", HOY.toString(), "status", "ACTIVE"));
        Integer deAyer = create("/api/v1/user-subscriptions", admin,
                Map.of("userId", cliente.id(), "planId", diario, "startDate", HOY.minusDays(1).toString(),
                        "status", "ACTIVE"));

        POST("/api/v1/user-subscriptions/expire-due", registerCustomer(), null).andExpect(status().isForbidden());
        int vencidas = read(POST("/api/v1/user-subscriptions/expire-due", admin, null).andExpect(status().isOk()))
                .get("expired").asInt();
        assertTrue(vencidas >= 2, "debían vencer al menos las dos suscripciones viejas");

        GET("/api/v1/user-subscriptions/" + vieja, admin).andExpect(jsonPath("$.status").value("EXPIRED"));
        GET("/api/v1/user-subscriptions/" + deAyer, admin).andExpect(jsonPath("$.status").value("EXPIRED"));
        GET("/api/v1/user-subscriptions/" + deHoy, admin).andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("Renovar usa el procedimiento almacenado: queda PENDING, la siguiente empieza al vencer la anterior")
    void renovar() throws Exception {
        Session admin = admin();
        Session cliente = registerCustomer();
        Session otro = registerCustomer();
        Integer mensual = plan(admin, "Mensual renov", 30, "MEMBERSHIP");

        POST("/api/v1/user-subscriptions/renew", cliente, Map.of("userId", cliente.id(), "planId", mensual))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.startDate").value(HOY.toString()))
                .andExpect(jsonPath("$.endDate").value(HOY.plusDays(29).toString()));
        POST("/api/v1/user-subscriptions/renew", cliente, Map.of("userId", cliente.id(), "planId", mensual))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.startDate").value(HOY.plusDays(30).toString()))
                .andExpect(jsonPath("$.endDate").value(HOY.plusDays(59).toString()));

        POST("/api/v1/user-subscriptions/renew", otro, Map.of("userId", cliente.id(), "planId", mensual))
                .andExpect(status().isForbidden());
        POST("/api/v1/user-subscriptions/renew", cliente, Map.of("userId", cliente.id(), "planId", 999999))
                .andExpect(status().isNotFound());
        POST("/api/v1/user-subscriptions/renew", cliente, Map.of("userId", cliente.id()))
                .andExpect(status().isBadRequest());

        // La renovación también generó la deuda por cobrar del plan
        GET("/api/v1/accounts-receivable?userId=" + cliente.id(), admin).andExpect(status().isOk());
    }
}
