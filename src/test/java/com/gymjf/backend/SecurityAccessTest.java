package com.gymjf.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Roles y propiedad de los datos: 401 sin sesión, 403 al tocar lo ajeno, el admin ve todo. */
class SecurityAccessTest extends AbstractIntegrationTest {

    private static final Map<String, Object> PRODUCTO = Map.of(
            "name", "Proteina", "category", "SUPPLEMENTS", "price", 10, "stock", 5, "minStockAlert", 1);

    private Map<String, Object> ficha(Integer userId) {
        return Map.of("userId", userId, "weight", 70, "height", 1.75, "recordDate", "2030-01-01");
    }

    @Test
    @DisplayName("Sin token o con token inválido responde 401")
    void sinSesion() throws Exception {
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/products"))
                .andExpect(status().isUnauthorized());
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/products")
                .header("Authorization", "Bearer token.invalido.xx"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("El cliente lee el catálogo pero no lo modifica (403)")
    void catalogoSoloLecturaParaCliente() throws Exception {
        Session cliente = registerCustomer();
        GET("/api/v1/products", cliente).andExpect(status().isOk());
        POST("/api/v1/products", cliente, PRODUCTO).andExpect(status().isForbidden());
        POST("/api/v1/products", admin(), PRODUCTO).andExpect(status().isCreated());
    }

    @Test
    @DisplayName("El cliente no puede escribir en finanzas ni en suscripciones")
    void finanzasSoloAdmin() throws Exception {
        Session cliente = registerCustomer();
        POST("/api/v1/transactions", cliente,
                Map.of("userId", cliente.id(), "amount", 5, "paymentMethod", "CASH")).andExpect(status().isForbidden());
        POST("/api/v1/accounts-receivable", cliente,
                Map.of("userId", cliente.id(), "totalDebt", 5, "pendingBalance", 5, "issueDate", "2030-01-01"))
                .andExpect(status().isForbidden());
        POST("/api/v1/user-subscriptions", cliente,
                Map.of("userId", cliente.id(), "planId", 1, "startDate", "2030-01-01", "endDate", "2030-02-01"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Un cliente no ve, edita ni borra la ficha de salud de otro; el admin sí la ve")
    void fichaDeSaludEsPrivada() throws Exception {
        Session a = registerCustomer();
        Session b = registerCustomer();
        Integer id = create("/api/v1/anthropometric-records", a, ficha(a.id()));

        GET("/api/v1/anthropometric-records/" + id, a).andExpect(status().isOk());
        GET("/api/v1/anthropometric-records/" + id, b).andExpect(status().isForbidden());
        PUT("/api/v1/anthropometric-records/" + id, b, Map.of("weight", 1)).andExpect(status().isForbidden());
        DELETE("/api/v1/anthropometric-records/" + id, b).andExpect(status().isForbidden());
        GET("/api/v1/anthropometric-records/" + id, admin()).andExpect(status().isOk());

        // El listado de B solo trae lo de B; pedir el de A es 403
        GET("/api/v1/anthropometric-records", b).andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
        GET("/api/v1/anthropometric-records?userId=" + a.id(), b).andExpect(status().isForbidden());
        GET("/api/v1/anthropometric-records?userId=" + a.id(), admin()).andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("Nadie puede crear datos a nombre de otro usuario")
    void noSePuedeCrearAnombreDeOtro() throws Exception {
        Session a = registerCustomer();
        Session b = registerCustomer();
        POST("/api/v1/anthropometric-records", b, ficha(a.id())).andExpect(status().isForbidden());
        POST("/api/v1/reservations", b,
                Map.of("userId", a.id(), "scheduleId", 1, "reservationDate", "2030-01-01"))
                .andExpect(status().isForbidden());
        POST("/api/v1/orders", b, Map.of("userId", a.id(), "code", "X-" + a.id(), "total", 10))
                .andExpect(status().isForbidden());
        POST("/api/v1/favorite-recipes", b, Map.of("userId", a.id(), "recipeId", 1)).andExpect(status().isForbidden());
        GET("/api/v1/favorite-recipes?userId=" + a.id(), b).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("El cliente no puede darse privilegios: pedido siempre PENDING y rutina no 'hecha por admin'")
    void sinEscaladaDePrivilegios() throws Exception {
        Session cliente = registerCustomer();
        POST("/api/v1/orders", cliente,
                Map.of("userId", cliente.id(), "code", "PRIV-" + cliente.id(), "total", 10, "status", "COMPLETED"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("PENDING"));
        POST("/api/v1/routines", cliente,
                Map.of("userId", cliente.id(), "routineName", "Mia", "createdByAdmin", true))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.createdByAdmin").value(false));
    }

    @Test
    @DisplayName("Rutinas y sus ejercicios son privados entre clientes")
    void rutinasPrivadas() throws Exception {
        Session a = registerCustomer();
        Session b = registerCustomer();
        Integer rutina = create("/api/v1/routines", a,
                Map.of("userId", a.id(), "routineName", "De A", "createdByAdmin", false));
        GET("/api/v1/routines/" + rutina, b).andExpect(status().isForbidden());
        GET("/api/v1/routine-exercises?routineId=" + rutina, b).andExpect(status().isForbidden());
        DELETE("/api/v1/routines/" + rutina, b).andExpect(status().isForbidden());
        DELETE("/api/v1/routines/" + rutina, a).andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("El login entrega un token con el rol y el id del usuario")
    void loginEntregaIdYRol() throws Exception {
        Session admin = admin();
        assertEquals(Integer.class, admin.id().getClass());
        GET("/api/v1/reservations", admin).andExpect(status().isOk());
    }
}
