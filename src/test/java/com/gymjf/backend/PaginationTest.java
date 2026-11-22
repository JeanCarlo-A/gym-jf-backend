package com.gymjf.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import tools.jackson.databind.JsonNode;

/** Paginación de los listados que crecen (reservas, pedidos, pagos, fichas) y CORS/salud. */
class PaginationTest extends AbstractIntegrationTest {

    @Test
    @DisplayName("Los pedidos se entregan por páginas, del más reciente al más antiguo")
    void paginaDePedidos() throws Exception {
        Session cliente = registerCustomer();
        Integer ultimo = null;
        for (int i = 0; i < 5; i++) {
            ultimo = create("/api/v1/orders", cliente, Map.of(
                    "userId", cliente.id(), "code", "PG-" + UUID.randomUUID().toString().substring(0, 8), "total", 10));
        }

        GET("/api/v1/orders?page=0&size=2", cliente).andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(5))
                .andExpect(jsonPath("$.totalPages").value(3))
                .andExpect(jsonPath("$.content[0].id").value(ultimo));
        GET("/api/v1/orders?page=2&size=2", cliente).andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    @DisplayName("El tamaño de página tiene un máximo de 100 y ordenar por un campo inexistente es 400")
    void limitesDePagina() throws Exception {
        Session cliente = registerCustomer();
        JsonNode pagina = read(GET("/api/v1/orders?size=1000", cliente));
        assertEquals(100, pagina.get("size").asInt());
        GET("/api/v1/orders?sort=noExiste,asc", cliente).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("El admin lista todo o filtra por usuario; los cuatro listados devuelven el mismo formato")
    void listadosPaginados() throws Exception {
        Session admin = admin();
        for (String ruta : new String[] { "reservations", "orders", "transactions", "anthropometric-records" }) {
            GET("/api/v1/" + ruta + "?size=5", admin).andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.page").value(0))
                    .andExpect(jsonPath("$.size").value(5));
        }
    }

    @Test
    @DisplayName("/actuator/health es público; los demás endpoints de actuator están cerrados")
    void actuator() throws Exception {
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/actuator/health"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("UP"));
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/actuator/env"))
                .andExpect(status().isUnauthorized());
    }
}
