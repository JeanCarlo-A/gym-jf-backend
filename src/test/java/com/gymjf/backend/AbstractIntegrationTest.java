package com.gymjf.backend;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.testcontainers.mysql.MySQLContainer;
import org.testcontainers.utility.MountableFile;

import com.gymjf.backend.modules.auth.domain.Rol;
import com.gymjf.backend.modules.auth.domain.User;
import com.gymjf.backend.modules.auth.repositories.UserRepository;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Base de las pruebas de integración: levanta una MySQL desechable
 * (Testcontainers) con los
 * mismos scripts SQL que usa Docker (esquema, índices, triggers y
 * procedimientos), así que las
 * reglas que viven en la BD se prueban de verdad. No necesita .env ni una BD
 * local; sí Docker.
 * El contenedor se comparte entre todas las clases de prueba.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("prod") // sin SQL en el log
public abstract class AbstractIntegrationTest {

    @SuppressWarnings("resource")
    private static final MySQLContainer MYSQL = new MySQLContainer("mysql:8.4")
            .withDatabaseName("gym_jf")
            .withCopyFileToContainer(MountableFile.forHostPath("docker/mysql/charset.cnf"),
                    "/etc/mysql/conf.d/charset.cnf")
            .withCopyFileToContainer(MountableFile.forHostPath("docker/mysql/sql/DB_Gym_JF.sql"),
                    "/docker-entrypoint-initdb.d/01-schema.sql")
            .withCopyFileToContainer(MountableFile.forHostPath("docker/mysql/sql/Indices y Triggers.sql"),
                    "/docker-entrypoint-initdb.d/02-indices-triggers.sql")
            .withCopyFileToContainer(
                    MountableFile.forHostPath("docker/mysql/sql/Procedimientos almacenados y Trasacciones.sql"),
                    "/docker-entrypoint-initdb.d/03-procedimientos.sql");

    static {
        MYSQL.start();
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
        // Clave de prueba (32 bytes en base64); no es la de ningún entorno real
        registry.add("application.security.jwt.secret-key", () -> "dGVzdC1rZXktcGFyYS1wcnVlYmFzLXNvbG8tMTIzNDU2Nzg=");
    }

    @Autowired
    protected MockMvc mvc;
    @Autowired
    protected JsonMapper json;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    /** Usuario autenticado de prueba. */
    protected record Session(String token, Integer id) {
    }

    private static Session admin;

    /**
     * El administrador no se puede registrar por la API (siempre nace CUSTOMER): se
     * inserta directo.
     */
    protected synchronized Session admin() throws Exception {
        if (admin == null) {
            String email = "admin-" + UUID.randomUUID() + "@test.com";
            userRepository.save(User.builder()
                    .name("Admin de prueba").email(email)
                    .password(passwordEncoder.encode("Admin123!")).rol(Rol.ADMIN).build());
            admin = login(email, "Admin123!");
        }
        return admin;
    }

    protected Session registerCustomer() throws Exception {
        String email = "cliente-" + UUID.randomUUID() + "@test.com";
        MvcResult result = send(post("/api/v1/auth/register"), null,
                Map.of("name", "Cliente de prueba", "email", email, "password", "Cliente123!"))
                .andReturn();
        JsonNode node = json.readTree(result.getResponse().getContentAsString());
        return new Session(node.get("token").asString(), node.get("id").asInt());
    }

    /** Cliente que ya aceptó el consentimiento informado (necesario para guardar fichas de salud). */
    protected Session registerCustomerWithConsent() throws Exception {
        Session session = registerCustomer();
        POST("/api/v1/users/me/health-consent", session, null);
        return session;
    }

    protected Session login(String email, String password) throws Exception {
        MvcResult result = send(post("/api/v1/auth/login"), null, Map.of("email", email, "password", password))
                .andReturn();
        JsonNode node = json.readTree(result.getResponse().getContentAsString());
        return new Session(node.get("token").asString(), node.get("id").asInt());
    }

    // --- Atajos HTTP (token null = sin sesión) ---

    protected ResultActions GET(String url, Session s) throws Exception {
        return send(get(url), s, null);
    }

    protected ResultActions POST(String url, Session s, Object body) throws Exception {
        return send(post(url), s, body);
    }

    protected ResultActions PUT(String url, Session s, Object body) throws Exception {
        return send(put(url), s, body);
    }

    protected ResultActions DELETE(String url, Session s) throws Exception {
        return send(delete(url), s, null);
    }

    private ResultActions send(MockHttpServletRequestBuilder request, Session s, Object body) throws Exception {
        if (s != null) {
            request.header("Authorization", "Bearer " + s.token());
        }
        if (body != null) {
            request.contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(body));
        }
        return mvc.perform(request);
    }

    /** Crea un recurso y devuelve su id (comprueba que la respuesta sea 201). */
    protected Integer create(String url, Session s, Object body) throws Exception {
        MvcResult result = POST(url, s, body)
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isCreated())
                .andReturn();
        return json.readTree(result.getResponse().getContentAsString()).get("id").asInt();
    }

    protected JsonNode read(ResultActions actions) throws Exception {
        return json.readTree(actions.andReturn().getResponse().getContentAsString());
    }
}
