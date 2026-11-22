CREATE DATABASE IF NOT EXISTS gym_jf;
USE gym_jf;

CREATE TABLE IF NOT EXISTS usuarios (
  id INT NOT NULL AUTO_INCREMENT,
  nombre_completo VARCHAR(150) NOT NULL,
  correo VARCHAR(100) NOT NULL,
  contrasena VARCHAR(255) NOT NULL,
  fecha_nacimiento DATE NULL,
  foto_perfil_url VARCHAR(255) NULL,
  objetivo_fisico VARCHAR(255) NULL,
  rol VARCHAR(45) NOT NULL,
  created_at TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE INDEX correo_UNIQUE (correo ASC)
);

CREATE TABLE IF NOT EXISTS fichas_antropometricas (
  id INT NOT NULL AUTO_INCREMENT,
  usuario_id INT NOT NULL,
  peso DECIMAL(5,2) NOT NULL,
  altura DECIMAL(3,2) NOT NULL,
  porcentaje_grasa DECIMAL(4,2) NULL,
  hombros DECIMAL(4,1) NULL,
  pecho DECIMAL(4,1) NULL,
  cintura DECIMAL(4,1) NULL,
  cadera DECIMAL(4,1) NULL,
  brazo DECIMAL(4,1) NULL,
  muslo DECIMAL(4,1) NULL,
  pantorrilla DECIMAL(4,1) NULL,
  fecha_registro DATE NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_fichas_usuarios FOREIGN KEY (usuario_id) REFERENCES usuarios (id)
);

CREATE TABLE IF NOT EXISTS planes_suscripcion (
  id INT NOT NULL AUTO_INCREMENT,
  nombre_plan VARCHAR(45) NOT NULL,
  precio DECIMAL(10,2) NOT NULL,
  duracion_dias INT NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS suscripciones_usuario (
  id INT NOT NULL AUTO_INCREMENT,
  fecha_inicio DATE NOT NULL,
  fecha_vencimiento DATE NOT NULL,
  estado VARCHAR(45) NOT NULL,
  plan_id INT NOT NULL,
  usuario_id INT NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_sub_planes FOREIGN KEY (plan_id) REFERENCES planes_suscripcion (id),
  CONSTRAINT fk_sub_usuarios FOREIGN KEY (usuario_id) REFERENCES usuarios (id)
);

CREATE TABLE IF NOT EXISTS horarios (
  id INT NOT NULL AUTO_INCREMENT,
  actividad VARCHAR(45) NOT NULL,
  dia_semana VARCHAR(45) NOT NULL,
  hora_inicio TIME NOT NULL,
  hora_fin TIME NOT NULL,
  aforo_maximo INT NOT NULL,
  PRIMARY KEY (id),
  UNIQUE INDEX idx_horario_unico (actividad ASC, dia_semana ASC, hora_inicio ASC)
);

CREATE TABLE IF NOT EXISTS reservas (
  id INT NOT NULL AUTO_INCREMENT,
  fecha_reserva DATE NOT NULL,
  estado VARCHAR(45) NOT NULL,
  usuario_id INT NOT NULL,
  horario_id INT NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_res_usuarios FOREIGN KEY (usuario_id) REFERENCES usuarios (id),
  CONSTRAINT fk_res_horarios FOREIGN KEY (horario_id) REFERENCES horarios (id)
);

CREATE TABLE IF NOT EXISTS ejercicios (
  id INT NOT NULL AUTO_INCREMENT,
  nombre VARCHAR(45) NOT NULL,
  descripcion VARCHAR(255) NULL,
  grupo_muscular VARCHAR(45) NULL,
  video_url VARCHAR(255) NULL,
  es_para_casa TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS rutinas (
  id INT NOT NULL AUTO_INCREMENT,
  nombre_rutina VARCHAR(255) NOT NULL,
  hecha_por_admin TINYINT NOT NULL,
  usuarios_id INT NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_rutinas_usuarios FOREIGN KEY (usuarios_id) REFERENCES usuarios (id)
);

CREATE TABLE IF NOT EXISTS rutina_ejercicios (
  id INT NOT NULL AUTO_INCREMENT,
  series INT NOT NULL,
  repeticiones INT NOT NULL,
  descanso INT NOT NULL,
  rutina_id INT NOT NULL,
  ejercicios_id INT NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_re_rutinas FOREIGN KEY (rutina_id) REFERENCES rutinas (id),
  CONSTRAINT fk_re_ejercicios FOREIGN KEY (ejercicios_id) REFERENCES ejercicios (id)
);

CREATE TABLE IF NOT EXISTS ingredientes_locales (
  id INT NOT NULL AUTO_INCREMENT,
  nombre VARCHAR(255) NOT NULL,
  imagen_url VARCHAR(255) NULL,
  PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS recetas (
  id INT NOT NULL AUTO_INCREMENT,
  titulo VARCHAR(255) NOT NULL,
  instruccion TEXT NULL,
  objetivo_sugerido VARCHAR(50) NULL,
  tiempo_comida VARCHAR(45) NULL,
  imagen_url VARCHAR(255) NULL,
  video_url VARCHAR(255) NULL,
  usuario_id INT NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_recetas_usuarios FOREIGN KEY (usuario_id) REFERENCES usuarios (id)
);

CREATE TABLE IF NOT EXISTS receta_ingredientes (
  id INT NOT NULL AUTO_INCREMENT,
  cantidad_sugerida VARCHAR(45) NULL,
  sustituto_local VARCHAR(45) NULL,
  receta_id INT NOT NULL,
  ingrediente_id INT NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_ri_recetas FOREIGN KEY (receta_id) REFERENCES recetas (id),
  CONSTRAINT fk_ri_ingredientes FOREIGN KEY (ingrediente_id) REFERENCES ingredientes_locales (id)
);

CREATE TABLE IF NOT EXISTS recetas_favoritas (
  id INT NOT NULL AUTO_INCREMENT,
  usuario_id INT NOT NULL,
  receta_id INT NOT NULL,
  fecha_guardado DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  CONSTRAINT fk_rf_usuarios FOREIGN KEY (usuario_id) REFERENCES usuarios (id),
  CONSTRAINT fk_rf_recetas FOREIGN KEY (receta_id) REFERENCES recetas (id)
);

CREATE TABLE IF NOT EXISTS cuentas_por_cobrar (
  id INT NOT NULL AUTO_INCREMENT,
  deuda_total DECIMAL(10,2) NOT NULL,
  saldo_pendiente DECIMAL(10,2) NOT NULL,
  fecha_emision DATE NOT NULL,
  estado_morosidad VARCHAR(45) NOT NULL,
  usuario_id INT NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_cxc_usuarios FOREIGN KEY (usuario_id) REFERENCES usuarios (id)
);

CREATE TABLE IF NOT EXISTS transacciones (
  id INT NOT NULL AUTO_INCREMENT,
  monto DECIMAL(10,2) NOT NULL,
  metodo_pago VARCHAR(45) NOT NULL,
  numero_referencia VARCHAR(255) NULL,
  comprobante_imagen_url VARCHAR(255) NULL,
  validacion VARCHAR(45) NOT NULL,
  fecha_transaccion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  usuario_id INT NOT NULL,
  cuenta_por_cobrar_id INT NULL,
  PRIMARY KEY (id),
  UNIQUE INDEX numero_referencia_UNIQUE (numero_referencia ASC),
  CONSTRAINT fk_trx_usuarios FOREIGN KEY (usuario_id) REFERENCES usuarios (id),
  CONSTRAINT fk_trx_cxc FOREIGN KEY (cuenta_por_cobrar_id) REFERENCES cuentas_por_cobrar (id)
);

CREATE TABLE IF NOT EXISTS productos (
  id INT NOT NULL AUTO_INCREMENT,
  nombre VARCHAR(100) NOT NULL,
  categoria VARCHAR(50) NOT NULL,
  precio DECIMAL(10,2) NOT NULL,
  stock INT NOT NULL,
  stock_minimo_alerta INT NOT NULL,
  imagen_url VARCHAR(255) NULL,
  PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS pedidos (
  id INT NOT NULL AUTO_INCREMENT,
  codigo VARCHAR(45) NOT NULL,
  total DECIMAL(10,2) NOT NULL,
  estado VARCHAR(45) NOT NULL,
  fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  usuario_id INT NOT NULL,
  PRIMARY KEY (id),
  UNIQUE INDEX codigo_UNIQUE (codigo ASC),
  CONSTRAINT fk_pedidos_usuarios FOREIGN KEY (usuario_id) REFERENCES usuarios (id)
);

CREATE TABLE IF NOT EXISTS detalle_pedidos (
  id INT NOT NULL AUTO_INCREMENT,
  cantidad INT NOT NULL,
  precio_unitario DECIMAL(10,2) NOT NULL,
  pedido_id INT NOT NULL,
  producto_id INT NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_dp_pedidos FOREIGN KEY (pedido_id) REFERENCES pedidos (id),
  CONSTRAINT fk_dp_productos FOREIGN KEY (producto_id) REFERENCES productos (id)
);