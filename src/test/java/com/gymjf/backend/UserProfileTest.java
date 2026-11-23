package com.gymjf.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.gymjf.backend.shared.infrastructure.mail.EmailSender;

/** Fase 2.1: perfil, objetivo físico, consentimiento, altas de admin y recuperación de contraseña. */
class UserProfileTest extends AbstractIntegrationTest {

    @MockitoBean
    private EmailSender emailSender;

    /** Pide la recuperación y devuelve el código que se habría enviado por correo. */
    private String requestToken(String email) throws Exception {
        POST("/api/v1/auth/forgot-password", null, Map.of("email", email)).andExpect(status().isOk());
        ArgumentCaptor<String> body = ArgumentCaptor.forClass(String.class);
        verify(emailSender, org.mockito.Mockito.atLeastOnce()).send(eq(email), anyString(), body.capture());
        Matcher m = Pattern.compile("código de recuperación es:\\s*(\\S+)").matcher(body.getValue());
        assertTrue(m.find(), "el correo debe incluir el código");
        return m.group(1);
    }

    private String emailOf(Session s) throws Exception {
        return read(GET("/api/v1/users/me", s)).get("email").asString();
    }

    @Test
    @DisplayName("/me devuelve el perfil propio y permite editarlo, incluido el objetivo físico")
    void miPerfil() throws Exception {
        Session cliente = registerCustomer();

        GET("/api/v1/users/me", cliente).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(cliente.id()))
                .andExpect(jsonPath("$.rol").value("CUSTOMER"))
                .andExpect(jsonPath("$.healthConsentAccepted").value(false))
                .andExpect(jsonPath("$.password").doesNotExist());

        PUT("/api/v1/users/me", cliente,
                Map.of("name", "Nombre Nuevo", "birthDate", "1999-05-20", "physicalGoal", "GAIN_MUSCLE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Nombre Nuevo"))
                .andExpect(jsonPath("$.birthDate").value("1999-05-20"))
                .andExpect(jsonPath("$.physicalGoal").value("GAIN_MUSCLE"));

        // Un cambio parcial no borra lo demás
        PUT("/api/v1/users/me", cliente, Map.of("physicalGoal", "LOSE_WEIGHT")).andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Nombre Nuevo"))
                .andExpect(jsonPath("$.physicalGoal").value("LOSE_WEIGHT"));

        // No se puede escalar el rol por esta vía: el campo simplemente se ignora
        PUT("/api/v1/users/me", cliente, Map.of("rol", "ADMIN")).andExpect(status().isOk())
                .andExpect(jsonPath("$.rol").value("CUSTOMER"));
    }

    @Test
    @DisplayName("Perfil: objetivo inexistente y fecha futura son 400; sin sesión es 401")
    void validacionesDelPerfil() throws Exception {
        Session cliente = registerCustomer();
        PUT("/api/v1/users/me", cliente, Map.of("physicalGoal", "VOLAR"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("LOSE_WEIGHT")));
        PUT("/api/v1/users/me", cliente, Map.of("birthDate", "2999-01-01"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors.birthDate").exists());
        GET("/api/v1/users/me", null).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Cambiar contraseña exige la actual; después solo sirve la nueva")
    void cambiarContrasena() throws Exception {
        Session cliente = registerCustomer();
        String email = emailOf(cliente);

        PUT("/api/v1/users/me/password", cliente, Map.of("currentPassword", "mala", "newPassword", "Nueva123!"))
                .andExpect(status().isBadRequest());
        PUT("/api/v1/users/me/password", cliente, Map.of("currentPassword", "Cliente123!", "newPassword", "123"))
                .andExpect(status().isBadRequest());
        PUT("/api/v1/users/me/password", cliente, Map.of("currentPassword", "Cliente123!", "newPassword", "Nueva123!"))
                .andExpect(status().isOk());

        POST("/api/v1/auth/login", null, Map.of("email", email, "password", "Cliente123!"))
                .andExpect(status().isUnauthorized());
        POST("/api/v1/auth/login", null, Map.of("email", email, "password", "Nueva123!")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Sin consentimiento informado no se guardan medidas; al aceptarlo sí, y la fecha no cambia")
    void consentimientoInformado() throws Exception {
        Session cliente = registerCustomer();
        Map<String, Object> ficha = Map.of("userId", cliente.id(), "weight", 70, "height", 1.75,
                "recordDate", "2030-01-01");

        POST("/api/v1/anthropometric-records", cliente, ficha)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("consentimiento")));

        String primera = read(POST("/api/v1/users/me/health-consent", cliente, null)
                .andExpect(status().isOk()).andExpect(jsonPath("$.healthConsentAccepted").value(true)))
                .get("healthConsentAt").asString();
        assertNotNull(primera);
        create("/api/v1/anthropometric-records", cliente, ficha);

        String segunda = read(POST("/api/v1/users/me/health-consent", cliente, null)).get("healthConsentAt")
                .asString();
        assertEquals(primera, segunda);
    }

    @Test
    @DisplayName("Solo un ADMIN lista usuarios y crea otros administradores")
    void gestionDeUsuarios() throws Exception {
        Session admin = admin();
        Session cliente = registerCustomer();
        String email = "nuevo-admin-" + UUID.randomUUID() + "@test.com";
        Map<String, Object> nuevo = Map.of("name", "Otro Admin", "email", email, "password", "Admin456!",
                "rol", "ADMIN");

        POST("/api/v1/users", cliente, nuevo).andExpect(status().isForbidden());
        GET("/api/v1/users", cliente).andExpect(status().isForbidden());
        GET("/api/v1/users/" + cliente.id(), cliente).andExpect(status().isForbidden());

        POST("/api/v1/users", admin, nuevo).andExpect(status().isCreated())
                .andExpect(jsonPath("$.rol").value("ADMIN")).andExpect(jsonPath("$.password").doesNotExist());
        POST("/api/v1/users", admin, nuevo).andExpect(status().isConflict());
        POST("/api/v1/users", admin, Map.of("name", "X", "email", "no-es-correo", "password", "Admin456!",
                "rol", "ADMIN")).andExpect(status().isBadRequest());
        POST("/api/v1/users", admin, Map.of("name", "X", "email", "x-" + email, "password", "Admin456!",
                "rol", "SUPERHEROE")).andExpect(status().isBadRequest());

        // El nuevo admin entra con su contraseña y ve el listado
        Session otro = login(email, "Admin456!");
        GET("/api/v1/users?size=5", otro).andExpect(status().isOk()).andExpect(jsonPath("$.content").isArray());
        GET("/api/v1/users/" + cliente.id(), admin).andExpect(status().isOk());
        GET("/api/v1/users/99999999", admin).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Recuperar contraseña: el código llega por correo, sirve una vez y cambia la contraseña")
    void recuperarContrasena() throws Exception {
        Session cliente = registerCustomer();
        String email = emailOf(cliente);
        String token = requestToken(email);

        POST("/api/v1/auth/reset-password", null, Map.of("token", token, "newPassword", "Recuperada1!"))
                .andExpect(status().isOk());
        POST("/api/v1/auth/login", null, Map.of("email", email, "password", "Cliente123!"))
                .andExpect(status().isUnauthorized());
        POST("/api/v1/auth/login", null, Map.of("email", email, "password", "Recuperada1!"))
                .andExpect(status().isOk());

        // Un código ya usado no sirve de nuevo
        POST("/api/v1/auth/reset-password", null, Map.of("token", token, "newPassword", "Otra12345!"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Recuperar contraseña: código falso o reemplazado es 400; un correo desconocido responde igual y no envía nada")
    void recuperacionSegura() throws Exception {
        POST("/api/v1/auth/reset-password", null, Map.of("token", "falso", "newPassword", "Nueva123!"))
                .andExpect(status().isBadRequest());
        POST("/api/v1/auth/reset-password", null, Map.of("token", "falso", "newPassword", "123"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors.newPassword").exists());

        String desconocido = "nadie-" + UUID.randomUUID() + "@test.com";
        POST("/api/v1/auth/forgot-password", null, Map.of("email", desconocido)).andExpect(status().isOk());
        verify(emailSender, never()).send(eq(desconocido), anyString(), anyString());

        Session cliente = registerCustomer();
        String email = emailOf(cliente);
        String viejo = requestToken(email);
        String nuevo = requestToken(email);
        POST("/api/v1/auth/reset-password", null, Map.of("token", viejo, "newPassword", "Nueva123!"))
                .andExpect(status().isBadRequest());
        POST("/api/v1/auth/reset-password", null, Map.of("token", nuevo, "newPassword", "Nueva123!"))
                .andExpect(status().isOk());
    }
}
