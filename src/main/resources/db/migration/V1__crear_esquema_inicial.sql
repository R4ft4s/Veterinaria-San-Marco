CREATE TABLE usuarios (
    id VARCHAR(40) NOT NULL PRIMARY KEY,
    nombre VARCHAR(60) NOT NULL,
    apellido VARCHAR(60) NOT NULL,
    fecha_nacimiento VARCHAR(20) NOT NULL,
    correo VARCHAR(120) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    direccion VARCHAR(200) NOT NULL,
    region VARCHAR(40) NOT NULL,
    genero VARCHAR(30) NOT NULL,
    rol VARCHAR(20) NOT NULL,
    intentos_fallidos INTEGER NOT NULL,
    activo BOOLEAN NOT NULL
);

CREATE TABLE categorias (
    id VARCHAR(40) NOT NULL PRIMARY KEY,
    nombre VARCHAR(80) NOT NULL UNIQUE
);

CREATE TABLE productos (
    id VARCHAR(40) NOT NULL PRIMARY KEY,
    categoria VARCHAR(80) NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    principio_activo VARCHAR(150) NOT NULL,
    presentacion VARCHAR(100) NOT NULL,
    especie VARCHAR(80) NOT NULL,
    stock INTEGER NOT NULL,
    precio INTEGER NOT NULL,
    precio_oferta INTEGER,
    icono VARCHAR(12) NOT NULL
);

CREATE TABLE compras (
    id_orden VARCHAR(50) NOT NULL PRIMARY KEY,
    usuario_id VARCHAR(40),
    fecha TIMESTAMP NOT NULL,
    direccion_entrega VARCHAR(200) NOT NULL,
    region_entrega VARCHAR(60) NOT NULL,
    tipo_entrega VARCHAR(20) NOT NULL,
    metodo_pago VARCHAR(24) NOT NULL,
    estado VARCHAR(24) NOT NULL,
    total INTEGER NOT NULL,
    CONSTRAINT fk_compras_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id)
);

CREATE TABLE detalle_compra (
    id_orden VARCHAR(50) NOT NULL,
    producto_id VARCHAR(40) NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    cantidad INTEGER NOT NULL,
    precio_unitario INTEGER NOT NULL,
    subtotal INTEGER NOT NULL,
    CONSTRAINT fk_detalle_compra_compra FOREIGN KEY (id_orden) REFERENCES compras (id_orden)
);

CREATE TABLE refresh_tokens (
    token VARCHAR(64) NOT NULL PRIMARY KEY,
    usuario_id VARCHAR(40) NOT NULL,
    expira_en TIMESTAMP NOT NULL,
    revoked BOOLEAN NOT NULL,
    CONSTRAINT fk_refresh_tokens_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id)
);

CREATE INDEX idx_compras_usuario_fecha ON compras (usuario_id, fecha);
CREATE INDEX idx_refresh_tokens_usuario ON refresh_tokens (usuario_id);
