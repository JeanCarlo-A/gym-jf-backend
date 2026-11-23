USE gym_jf;

-- Fase 2.1 (Usuarios y acceso) para una BD que ya existe. Idempotente: se puede ejecutar varias veces.
-- Agrega la fecha de aceptacion del consentimiento informado y la tabla de tokens de recuperacion de contrasena.
-- En una BD nueva (docker compose down -v) ya viene incluido en DB_Gym_JF.sql.

SET @existe = (SELECT COUNT(*) FROM information_schema.COLUMNS
               WHERE TABLE_SCHEMA = 'gym_jf' AND TABLE_NAME = 'usuarios' AND COLUMN_NAME = 'consentimiento_salud_at');
SET @sentencia = IF(@existe = 0,
    'ALTER TABLE usuarios ADD COLUMN consentimiento_salud_at TIMESTAMP NULL AFTER rol',
    'SELECT ''La columna consentimiento_salud_at ya existe'' AS aviso');
PREPARE ejecutar FROM @sentencia;
EXECUTE ejecutar;
DEALLOCATE PREPARE ejecutar;

CREATE TABLE IF NOT EXISTS tokens_recuperacion (
  id INT NOT NULL AUTO_INCREMENT,
  usuario_id INT NOT NULL,
  token_hash CHAR(64) NOT NULL,
  expira_at TIMESTAMP NOT NULL,
  usado TINYINT(1) NOT NULL DEFAULT 0,
  created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE INDEX token_hash_UNIQUE (token_hash ASC),
  CONSTRAINT fk_tokens_usuarios FOREIGN KEY (usuario_id) REFERENCES usuarios (id) ON DELETE CASCADE
);
