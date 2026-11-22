USE gym_jf;

-- Usuarios de prueba para desarrollo y evidencias. NO se monta en docker-entrypoint-initdb.d:
-- se ejecuta a mano (ver docs/DOCKER.md). Es re-ejecutable.
--   admin@gymjf.com    / Admin123!    (ADMIN)
--   cliente@gymjf.com  / Cliente123!  (CUSTOMER)
-- Las contraseñas están guardadas como hash BCrypt, igual que las crea la API.

INSERT INTO usuarios (nombre_completo, correo, contrasena, rol)
VALUES ('Administrador Gym JF', 'admin@gymjf.com', '$2a$10$6.9ZPz3mcnqqyC.ABhdyTOXF8Z1WIj7JPxXyrSx5nZMbJwP2XCdYu', 'ADMIN')
ON DUPLICATE KEY UPDATE contrasena = VALUES(contrasena), rol = VALUES(rol);

INSERT INTO usuarios (nombre_completo, correo, contrasena, rol)
VALUES ('Cliente de Prueba', 'cliente@gymjf.com', '$2a$10$Rnk2Fgu8G03OnH7hR8SStOQMFBZKgXVng7FnmGufRJrqXlzL5PZTe', 'CUSTOMER')
ON DUPLICATE KEY UPDATE contrasena = VALUES(contrasena), rol = VALUES(rol);
