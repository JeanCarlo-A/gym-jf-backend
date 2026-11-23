USE gym_jf;

-- Los valores de texto son los nombres de los enums de Java (EnumType.STRING), siempre en ingles.

DROP PROCEDURE IF EXISTS sp_procesar_pago_sinpe;
DROP PROCEDURE IF EXISTS sp_renovar_suscripcion;
DROP PROCEDURE IF EXISTS sp_crear_pedido_suplementos;
DROP PROCEDURE IF EXISTS sp_cancelar_reserva;

DELIMITER //

CREATE PROCEDURE sp_procesar_pago_sinpe(
    IN p_usuario_id INT,
    IN p_cxc_id INT,
    IN p_monto DECIMAL(10,2),
    IN p_num_referencia VARCHAR(255),
    IN p_comprobante_url VARCHAR(255),
    IN p_ahora DATETIME
)
BEGIN
    DECLARE v_saldo_actual DECIMAL(10,2);

    -- Ante cualquier error se deshace todo y se devuelve el error original (p. ej. referencia duplicada)
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;

    SELECT saldo_pendiente INTO v_saldo_actual
    FROM cuentas_por_cobrar
    WHERE id = p_cxc_id AND usuario_id = p_usuario_id;

    IF v_saldo_actual IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Error: la cuenta por cobrar no existe o no es de ese usuario.';
    END IF;
    IF p_monto > v_saldo_actual THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Error: el monto del pago supera el saldo pendiente.';
    END IF;

    -- La hora la manda Java (Costa Rica): CURRENT_TIMESTAMP de MySQL usaria la hora UTC del servidor
    INSERT INTO transacciones (monto, metodo_pago, numero_referencia, comprobante_imagen_url, validacion, fecha_transaccion, usuario_id, cuenta_por_cobrar_id)
    VALUES (p_monto, 'MOBILE_PAYMENT', p_num_referencia, p_comprobante_url, 'APPROVED', p_ahora, p_usuario_id, p_cxc_id);

    -- El descuento del saldo y el paso a UP_TO_DATE los hace trg_actualizar_saldo_cxc_pago al insertar el pago aprobado;
    -- hacerlo tambien aqui descontaria el monto dos veces.
    SELECT saldo_pendiente INTO v_saldo_actual
    FROM cuentas_por_cobrar
    WHERE id = p_cxc_id;

    -- Solo se activan las suscripciones ligadas a esta deuda (no las de otras compras)
    IF v_saldo_actual <= 0 THEN
        UPDATE suscripciones_usuario
        SET estado = 'ACTIVE'
        WHERE cuenta_por_cobrar_id = p_cxc_id AND estado = 'PENDING';
    END IF;

    COMMIT;
END//

DELIMITER ;


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

    INSERT INTO cuentas_por_cobrar (deuda_total, saldo_pendiente, fecha_emision, estado_morosidad, usuario_id)
    VALUES (v_precio, v_precio, p_hoy, 'OVERDUE', p_usuario_id);

    -- La suscripcion queda ligada a su deuda: al pagarla por completo se activa
    INSERT INTO suscripciones_usuario (fecha_inicio, fecha_vencimiento, estado, plan_id, usuario_id, cuenta_por_cobrar_id)
    VALUES (v_fecha_inicio, v_fecha_fin, 'PENDING', p_plan_id, p_usuario_id, LAST_INSERT_ID());

    COMMIT;
END//

DELIMITER ;


DELIMITER //

CREATE PROCEDURE sp_crear_pedido_suplementos(
    IN p_usuario_id INT,
    IN p_codigo VARCHAR(45),
    IN p_total DECIMAL(10,2),
    OUT p_pedido_id INT
)
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Error al generar el pedido. Se canceló la transacción.';
    END;

    START TRANSACTION;

    INSERT INTO pedidos (codigo, total, estado, fecha_creacion, usuario_id)
    VALUES (p_codigo, p_total, 'PENDING', NOW(), p_usuario_id);

    SET p_pedido_id = LAST_INSERT_ID();

    COMMIT;
END//

DELIMITER ;


DELIMITER //

CREATE PROCEDURE sp_cancelar_reserva(
    IN p_reserva_id INT,
    IN p_usuario_id INT
)
BEGIN
    DECLARE v_estado_actual VARCHAR(45);

    -- Los errores de negocio (SIGNAL 45000) conservan su mensaje; solo los errores inesperados reciben el mensaje generico.
    DECLARE EXIT HANDLER FOR SQLSTATE '45000'
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Error al intentar cancelar la reserva.';
    END;

    START TRANSACTION;

    SELECT estado INTO v_estado_actual
    FROM reservas
    WHERE id = p_reserva_id AND usuario_id = p_usuario_id;

    IF v_estado_actual IS NULL THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Error: La reserva no existe o no pertenece a este usuario.';
    ELSEIF v_estado_actual = 'CANCELLED' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Error: La reserva ya se encuentra cancelada.';
    ELSE
        UPDATE reservas
        SET estado = 'CANCELLED'
        WHERE id = p_reserva_id;
    END IF;

    COMMIT;
END//

DELIMITER ;
