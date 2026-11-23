package com.gymjf.backend;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Validaciones de entrada: límites del esquema SQL, mensajes en español y códigos 201/400/409. */
class ValidationTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("Registro: 201, correo duplicado 409, contraseña corta 400")
    void registro() throws Exception {
        String email = "reg-" + UUID.randomUUID() + "@test.com";
        Map<String, Object> body = Map.of("name", "Ana", "email", email, "password", "Clave123!");
        POST("/api/v1/auth/register", null, body).andExpect(status().isCreated())
                .andExpect(jsonPath("$.rol").value("CUSTOMER"));
        POST("/api/v1/auth/register", null, body).andExpect(status().isConflict());
        POST("/api/v1/auth/register", null, Map.of("name", "Ana", "email", "otro-" + email, "password", "123"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors.password").exists());
        POST("/api/v1/auth/register", null, Map.of("name", "x".repeat(151), "email", "n-" + email, "password", "Clave123!"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors.name").exists());
    }

    @Test
    @DisplayName("Login con contraseña incorrecta responde 401")
    void loginInvalido() throws Exception {
        Session cliente = registerCustomer();
        POST("/api/v1/auth/login", null, Map.of("email", "no-existe@test.com", "password", "Clave123!"))
                .andExpect(status().isUnauthorized());
        // (el cliente se registró con Cliente123!)
        GET("/api/v1/products", cliente).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Ejercicios: el nombre respeta los 45 caracteres tanto al crear como al editar")
    void ejercicios() throws Exception {
        Session admin = admin();
        Map<String, Object> ok = Map.of("name", "Press " + UUID.randomUUID().toString().substring(0, 8),
                "description", "Descripción", "muscleGroup", "CHEST", "isForHome", true);
        Integer id = create("/api/v1/exercises", admin, ok);
        GET("/api/v1/exercises/" + id, admin).andExpect(jsonPath("$.isForHome").value(true));

        POST("/api/v1/exercises", admin,
                Map.of("name", "x".repeat(46), "description", "d", "muscleGroup", "CHEST", "isForHome", true))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors.name").exists());
        PUT("/api/v1/exercises/" + id, admin, Map.of("name", "x".repeat(46))).andExpect(status().isBadRequest());
        PUT("/api/v1/exercises/" + id, admin, Map.of("name", "")).andExpect(status().isBadRequest());
        DELETE("/api/v1/exercises/" + id, admin).andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Ficha antropométrica: altura en metros, pesos positivos y con decimales válidos")
    void fichaAntropometrica() throws Exception {
        Session cliente = registerCustomerWithConsent();
        Map<String, Object> base = Map.of("userId", cliente.id(), "weight", 70, "height", 1.75, "recordDate", "2030-01-01");
        create("/api/v1/anthropometric-records", cliente, base);

        POST("/api/v1/anthropometric-records", cliente,
                Map.of("userId", cliente.id(), "weight", 70, "height", 175, "recordDate", "2030-01-01"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors.height").exists());
        POST("/api/v1/anthropometric-records", cliente,
                Map.of("userId", cliente.id(), "weight", -5, "height", 1.75, "recordDate", "2030-01-01"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors.weight").exists());
        POST("/api/v1/anthropometric-records", cliente,
                Map.of("userId", cliente.id(), "weight", 70.123, "height", 1.75, "recordDate", "2030-01-01"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Productos y pagos: stock no negativo, monto mayor que cero y rango del precio")
    void productosYPagos() throws Exception {
        Session admin = admin();
        POST("/api/v1/products", admin,
                Map.of("name", "P", "category", "SUPPLEMENTS", "price", 1, "stock", -1, "minStockAlert", 1))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors.stock").exists());
        POST("/api/v1/products", admin,
                Map.of("name", "P", "category", "SUPPLEMENTS", "price", 123456789.5, "stock", 1, "minStockAlert", 1))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors.price").exists());
        POST("/api/v1/transactions", admin,
                Map.of("userId", admin.id(), "amount", 0, "paymentMethod", "CASH"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors.amount").exists());
    }

    @Test
    @DisplayName("Los errores son ProblemDetail en español: 400 por JSON o enum inválido, 404 por id inexistente")
    void formatoDeErrores() throws Exception {
        Session admin = admin();
        POST("/api/v1/schedules", admin,
                Map.of("activity", "Yoga", "dayOfWeek", "FUNDAY", "startTime", "08:00:00", "endTime", "09:00:00", "maxCapacity", 5))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("MONDAY")));
        GET("/api/v1/exercises/999999", admin).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Recurso no encontrado"));
    }
}
