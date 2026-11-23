USE gym_jf;

-- Fase 2.2 (Membresias) para una BD que ya existe. Idempotente: se puede ejecutar varias veces.
-- Agrega el tipo de plan, la tabla de ajustes del gimnasio (modo de acceso) y actualiza sp_renovar_suscripcion.
-- En una BD nueva (docker compose down -v) ya viene incluido en DB_Gym_JF.sql y en el script de procedimientos.

SET @existe = (SELECT COUNT(*) FROM information_schema.COLUMNS
               WHERE TABLE_SCHEMA = 'gym_jf' AND TABLE_NAME = 'planes_suscripcion' AND COLUMN_NAME = 'tipo');
SET @sentencia = IF(@existe = 0,
    'ALTER TABLE planes_suscripcion ADD COLUMN tipo VARCHAR(20) NOT NULL DEFAULT ''MEMBERSHIP'' AFTER duracion_dias',
    'SELECT ''La columna tipo ya existe'' AS aviso');
PREPARE ejecutar FROM @sentencia;
EXECUTE ejecutar;
DEALLOCATE PREPARE ejecutar;

CREATE TABLE IF NOT EXISTS ajustes_gimnasio (
  id INT NOT NULL,
  modo_acceso VARCHAR(20) NOT NULL DEFAULT 'FREE',
  updated_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
);

INSERT IGNORE INTO ajustes_gimnasio (id, modo_acceso) VALUES (1, 'FREE');

DROP PROCEDURE IF EXISTS sp_renovar_suscripcion;

DELIMITER //

CREATE PROCEDURE sp_renovar_suscripcion(
    IN p_usuario_id INT,
    IN p_plan_id INT,
    IN p_hoy DATE
)
BEGIN
    DECLARE v_precio DECIMAL(10,2);
    DECLARE v_duracion INT;
    DECLARE v_fecha_inicio DATE;
    DECLARE v_fecha_fin DATE;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Error al renovar la suscripción. Transacción cancelada.';
    END;

    START TRANSACTION;

    SELECT precio, duracion_dias INTO v_precio, v_duracion
    FROM planes_suscripcion
    WHERE id = p_plan_id;

    -- La renovacion empieza hoy, o el dia siguiente al vencimiento si todavia tiene una suscripcion vigente.
    -- La duracion incluye el dia de inicio (30 dias desde el 1 vencen el 30; el pase diario vence el mismo dia).
    SELECT COALESCE(DATE_ADD(MAX(fecha_vencimiento), INTERVAL 1 DAY), p_hoy) INTO v_fecha_inicio
    FROM suscripciones_usuario
    WHERE usuario_id = p_usuario_id AND estado IN ('ACTIVE', 'PENDING') AND fecha_vencimiento >= p_hoy;
    SET v_fecha_fin = DATE_ADD(v_fecha_inicio, INTERVAL (v_duracion - 1) DAY);

    INSERT INTO suscripciones_usuario (fecha_inicio, fecha_vencimiento, estado, plan_id, usuario_id)
    VALUES (v_fecha_inicio, v_fecha_fin, 'PENDING', p_plan_id, p_usuario_id);

    INSERT INTO cuentas_por_cobrar (deuda_total, saldo_pendiente, fecha_emision, estado_morosidad, usuario_id)
    VALUES (v_precio, v_precio, p_hoy, 'OVERDUE', p_usuario_id);

    COMMIT;
END//

DELIMITER ;
