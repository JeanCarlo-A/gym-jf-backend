USE gym_jf;

-- Los valores de texto (estado, validacion, metodo_pago, ...) son los nombres de los enums de Java
-- (EnumType.STRING), siempre en ingles. Si se cambia un enum, se debe cambiar aqui tambien.

-- Los indices solo se crean una vez. En una BD existente, ejecutar desde "DROP TRIGGER" en adelante.
CREATE INDEX idx_reservas_aforo_fecha
ON reservas (horario_id, fecha_reserva, estado);

CREATE INDEX idx_suscripcion_usuario_activa
ON suscripciones_usuario (usuario_id, estado, fecha_vencimiento);

CREATE INDEX idx_cxc_usuario_morosidad
ON cuentas_por_cobrar (usuario_id, estado_morosidad, saldo_pendiente);

CREATE INDEX idx_productos_categoria_stock
ON productos (categoria, stock);

CREATE INDEX idx_recetas_objetivo_tiempo
ON recetas (objetivo_sugerido, tiempo_comida);


DROP TRIGGER IF EXISTS trg_validar_aforo_reserva;
DROP TRIGGER IF EXISTS trg_validar_stock_pedido;
DROP TRIGGER IF EXISTS trg_actualizar_stock_pedido;
DROP TRIGGER IF EXISTS trg_actualizar_saldo_cxc_pago;
DROP TRIGGER IF EXISTS trg_validar_stock_pedido_update;
DROP TRIGGER IF EXISTS trg_actualizar_stock_pedido_update;
DROP TRIGGER IF EXISTS trg_restaurar_stock_pedido_delete;
DROP TRIGGER IF EXISTS trg_actualizar_saldo_cxc_pago_update;
DROP TRIGGER IF EXISTS trg_restaurar_saldo_cxc_pago_delete;


DELIMITER //

CREATE TRIGGER trg_validar_aforo_reserva
BEFORE INSERT ON reservas
FOR EACH ROW
BEGIN
    DECLARE v_aforo_max INT;
    DECLARE v_reservas_actuales INT;

    SELECT aforo_maximo INTO v_aforo_max
    FROM horarios
    WHERE id = NEW.horario_id;

    SELECT COUNT(*) INTO v_reservas_actuales
    FROM reservas
    WHERE horario_id = NEW.horario_id
      AND fecha_reserva = NEW.fecha_reserva
      AND estado != 'CANCELLED';

    IF v_reservas_actuales >= v_aforo_max THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Error: Aforo máximo alcanzado para este horario y fecha.';
    END IF;
END//

DELIMITER ;


DELIMITER //

CREATE TRIGGER trg_validar_stock_pedido
BEFORE INSERT ON detalle_pedidos
FOR EACH ROW
BEGIN
    DECLARE v_stock_actual INT;

    SELECT stock INTO v_stock_actual
    FROM productos
    WHERE id = NEW.producto_id;

    IF v_stock_actual < NEW.cantidad THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Error: Stock insuficiente para procesar el pedido.';
    END IF;
END//

DELIMITER ;


DELIMITER //

CREATE TRIGGER trg_actualizar_stock_pedido
AFTER INSERT ON detalle_pedidos
FOR EACH ROW
BEGIN
    UPDATE productos
    SET stock = stock - NEW.cantidad
    WHERE id = NEW.producto_id;
END//

DELIMITER ;


DELIMITER //

CREATE TRIGGER trg_actualizar_saldo_cxc_pago
AFTER INSERT ON transacciones
FOR EACH ROW
BEGIN
    DECLARE v_nuevo_saldo DECIMAL(10,2);

    IF NEW.validacion = 'APPROVED' AND NEW.cuenta_por_cobrar_id IS NOT NULL THEN

        UPDATE cuentas_por_cobrar
        SET saldo_pendiente = saldo_pendiente - NEW.monto
        WHERE id = NEW.cuenta_por_cobrar_id;

        SELECT saldo_pendiente INTO v_nuevo_saldo
        FROM cuentas_por_cobrar
        WHERE id = NEW.cuenta_por_cobrar_id;

        IF v_nuevo_saldo <= 0 THEN
            UPDATE cuentas_por_cobrar
            SET estado_morosidad = 'UP_TO_DATE',
                saldo_pendiente = 0.00
            WHERE id = NEW.cuenta_por_cobrar_id;
        END IF;

    END IF;
END//

DELIMITER ;


-- Los pagos y los detalles de pedido tambien se pueden modificar o eliminar desde la API (PUT / DELETE).
-- Estos triggers mantienen el saldo de la cuenta por cobrar y el stock coherentes en esos casos.

DELIMITER //

CREATE TRIGGER trg_validar_stock_pedido_update
BEFORE UPDATE ON detalle_pedidos
FOR EACH ROW
BEGIN
    DECLARE v_stock_actual INT;

    SELECT stock INTO v_stock_actual
    FROM productos
    WHERE id = NEW.producto_id;

    -- Si el producto no cambia, las unidades que ya tenia el detalle vuelven a estar disponibles.
    IF NEW.producto_id = OLD.producto_id THEN
        SET v_stock_actual = v_stock_actual + OLD.cantidad;
    END IF;

    IF v_stock_actual < NEW.cantidad THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Error: Stock insuficiente para procesar el pedido.';
    END IF;
END//

CREATE TRIGGER trg_actualizar_stock_pedido_update
AFTER UPDATE ON detalle_pedidos
FOR EACH ROW
BEGIN
    UPDATE productos SET stock = stock + OLD.cantidad WHERE id = OLD.producto_id;
    UPDATE productos SET stock = stock - NEW.cantidad WHERE id = NEW.producto_id;
END//

CREATE TRIGGER trg_restaurar_stock_pedido_delete
AFTER DELETE ON detalle_pedidos
FOR EACH ROW
BEGIN
    UPDATE productos SET stock = stock + OLD.cantidad WHERE id = OLD.producto_id;
END//

CREATE TRIGGER trg_actualizar_saldo_cxc_pago_update
AFTER UPDATE ON transacciones
FOR EACH ROW
BEGIN
    -- Se deshace el efecto del pago anterior (si estaba aprobado) y se aplica el del nuevo.
    IF OLD.validacion = 'APPROVED' AND OLD.cuenta_por_cobrar_id IS NOT NULL THEN
        UPDATE cuentas_por_cobrar
        SET saldo_pendiente = saldo_pendiente + OLD.monto,
            estado_morosidad = IF(estado_morosidad = 'UP_TO_DATE', 'OVERDUE', estado_morosidad)
        WHERE id = OLD.cuenta_por_cobrar_id;
    END IF;

    IF NEW.validacion = 'APPROVED' AND NEW.cuenta_por_cobrar_id IS NOT NULL THEN
        UPDATE cuentas_por_cobrar
        SET saldo_pendiente = GREATEST(saldo_pendiente - NEW.monto, 0),
            estado_morosidad = IF(saldo_pendiente <= 0, 'UP_TO_DATE', estado_morosidad)
        WHERE id = NEW.cuenta_por_cobrar_id;
    END IF;
END//

CREATE TRIGGER trg_restaurar_saldo_cxc_pago_delete
AFTER DELETE ON transacciones
FOR EACH ROW
BEGIN
    IF OLD.validacion = 'APPROVED' AND OLD.cuenta_por_cobrar_id IS NOT NULL THEN
        UPDATE cuentas_por_cobrar
        SET saldo_pendiente = saldo_pendiente + OLD.monto,
            estado_morosidad = IF(estado_morosidad = 'UP_TO_DATE', 'OVERDUE', estado_morosidad)
        WHERE id = OLD.cuenta_por_cobrar_id;
    END IF;
END//

DELIMITER ;
