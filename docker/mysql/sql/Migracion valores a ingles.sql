USE gym_jf;

-- Para una BD que ya tiene filas con valores en español. Idempotente: se puede ejecutar varias veces.
-- Despues de esto, volver a ejecutar los triggers (desde "DROP TRIGGER") y "Procedimientos almacenados y Trasacciones.sql".

UPDATE usuarios SET rol = 'ADMIN' WHERE rol IN ('ADMINISTRADOR', 'ADMIN');
UPDATE usuarios SET rol = 'CUSTOMER' WHERE rol = 'CLIENTE';

UPDATE suscripciones_usuario SET estado = 'ACTIVE' WHERE estado = 'ACTIVA';
UPDATE suscripciones_usuario SET estado = 'PENDING' WHERE estado = 'PENDIENTE';
UPDATE suscripciones_usuario SET estado = 'EXPIRED' WHERE estado IN ('VENCIDA', 'INACTIVA');
UPDATE suscripciones_usuario SET estado = 'CANCELLED' WHERE estado = 'CANCELADA';

UPDATE reservas SET estado = 'PENDING' WHERE estado = 'PENDIENTE';
UPDATE reservas SET estado = 'CONFIRMED' WHERE estado = 'CONFIRMADA';
UPDATE reservas SET estado = 'CANCELLED' WHERE estado = 'CANCELADA';
UPDATE reservas SET estado = 'COMPLETED' WHERE estado IN ('COMPLETADA', 'ASISTIDA');

UPDATE cuentas_por_cobrar SET estado_morosidad = 'UP_TO_DATE' WHERE estado_morosidad = 'AL_DIA';
UPDATE cuentas_por_cobrar SET estado_morosidad = 'OVERDUE' WHERE estado_morosidad IN ('MOROSO', 'PENDIENTE');

UPDATE transacciones SET validacion = 'PENDING' WHERE validacion = 'PENDIENTE';
UPDATE transacciones SET validacion = 'APPROVED' WHERE validacion = 'APROBADO';
UPDATE transacciones SET validacion = 'REJECTED' WHERE validacion = 'RECHAZADO';
UPDATE transacciones SET metodo_pago = 'MOBILE_PAYMENT' WHERE metodo_pago = 'SINPE_MOVIL';
UPDATE transacciones SET metodo_pago = 'CASH' WHERE metodo_pago = 'EFECTIVO';

UPDATE pedidos SET estado = 'PENDING' WHERE estado = 'PENDIENTE';
UPDATE pedidos SET estado = 'COMPLETED' WHERE estado IN ('ENTREGADO', 'COMPLETADO');
UPDATE pedidos SET estado = 'CANCELLED' WHERE estado IN ('CANCELADO', 'EXPIRADO');
