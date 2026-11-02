-- =============================================================================
-- ESQUEMA RELACIONAL H2 — SISTEMA AL-TANTO (PANADERÍA DE LEONARDO BETANCUR)
-- Ingeniería de Software I · Equipo Hefesto · Grupo 4
-- =============================================================================

-- Secuencias para generación de identificadores
CREATE SEQUENCE IF NOT EXISTS proveedor_seq START WITH 100;
CREATE SEQUENCE IF NOT EXISTS articulo_seq START WITH 100;
CREATE SEQUENCE IF NOT EXISTS conciliacion_seq START WITH 100;
CREATE SEQUENCE IF NOT EXISTS detalle_conciliacion_seq START WITH 100;
CREATE SEQUENCE IF NOT EXISTS alerta_seq START WITH 100;
CREATE SEQUENCE IF NOT EXISTS tanda_seq START WITH 100;
CREATE SEQUENCE IF NOT EXISTS detalle_tanda_seq START WITH 100;

-- 1. TABLA: PROVEEDORES
CREATE TABLE IF NOT EXISTS proveedores (
    id BIGINT PRIMARY KEY,
    nit VARCHAR(20) NOT NULL UNIQUE,
    razon_social VARCHAR(150) NOT NULL,
    nombre_contacto VARCHAR(100),
    telefono VARCHAR(30) NOT NULL,
    categoria_suministro VARCHAR(50) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE
);

-- 2. TABLA: ARTICULOS (Base de Insumos y Productos Terminados)
CREATE TABLE IF NOT EXISTS articulos (
    id BIGINT PRIMARY KEY,
    codigo VARCHAR(30) NOT NULL UNIQUE,
    nombre VARCHAR(120) NOT NULL,
    descripcion VARCHAR(255),
    tipo_articulo VARCHAR(30) NOT NULL,
    unidad_medida VARCHAR(20) NOT NULL,
    existencia_actual NUMERIC(12, 3) NOT NULL DEFAULT 0.000,
    activo BOOLEAN NOT NULL DEFAULT TRUE
);

-- 3. TABLA: INSUMOS_CONFIG (Parámetros de Reabastecimiento y Recetas)
CREATE TABLE IF NOT EXISTS insumos_config (
    articulo_id BIGINT PRIMARY KEY,
    proveedor_habitual_id BIGINT,
    umbral_minimo NUMERIC(12, 3) NOT NULL,
    consumo_promedio_diario NUMERIC(12, 3) NOT NULL DEFAULT 0.000,
    presentacion_compra VARCHAR(30) NOT NULL,
    factor_conversion_presentacion NUMERIC(12, 3) NOT NULL,
    CONSTRAINT fk_ins_articulo FOREIGN KEY (articulo_id) 
        REFERENCES articulos (id) ON DELETE CASCADE,
    CONSTRAINT fk_ins_proveedor FOREIGN KEY (proveedor_habitual_id) 
        REFERENCES proveedores (id)
);

-- 4. TABLA: PRODUCTOS_VENTA_CONFIG (Configuración para Mostrador y Reporte Z)
CREATE TABLE IF NOT EXISTS productos_venta_config (
    articulo_id BIGINT PRIMARY KEY,
    codigo_plu INTEGER NOT NULL UNIQUE,
    precio_venta_unitario NUMERIC(10, 2) NOT NULL,
    CONSTRAINT fk_pvc_articulo FOREIGN KEY (articulo_id) 
        REFERENCES articulos (id) ON DELETE CASCADE
);

-- 5. TABLA: CONCILIACIONES_DIARIAS (Cierres de Caja y Turnos Diarios)
CREATE TABLE IF NOT EXISTS conciliaciones_diarias (
    id BIGINT PRIMARY KEY,
    fecha_conciliacion DATE NOT NULL UNIQUE,
    total_venta_reporte_z NUMERIC(12, 2) NOT NULL,
    fecha_hora_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    estado VARCHAR(20) NOT NULL DEFAULT 'BORRADOR',
    observaciones VARCHAR(500)
);

-- 6. TABLA: DETALLES_CONCILIACION (Líneas de Balance Físico vs Teórico)
CREATE TABLE IF NOT EXISTS detalles_conciliacion (
    id BIGINT PRIMARY KEY,
    conciliacion_id BIGINT NOT NULL,
    articulo_id BIGINT NOT NULL,
    existencia_anterior NUMERIC(12, 3) NOT NULL,
    cantidad_recibida NUMERIC(12, 3) NOT NULL DEFAULT 0.000,
    cantidad_salida NUMERIC(12, 3) NOT NULL DEFAULT 0.000,
    existencia_esperada NUMERIC(12, 3) NOT NULL,
    cantidad_contada NUMERIC(12, 3) NOT NULL,
    diferencia NUMERIC(12, 3) NOT NULL,
    CONSTRAINT fk_det_conciliacion FOREIGN KEY (conciliacion_id) 
        REFERENCES conciliaciones_diarias (id) ON DELETE CASCADE,
    CONSTRAINT fk_det_articulo FOREIGN KEY (articulo_id) 
        REFERENCES articulos (id)
);

-- 7. TABLA: ALERTAS_REABASTECIMIENTO (Monitoreo de Quiebre de Stock)
CREATE TABLE IF NOT EXISTS alertas_reabastecimiento (
    id BIGINT PRIMARY KEY,
    insumo_id BIGINT NOT NULL,
    fecha_generacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    existencia_alerta NUMERIC(12, 3) NOT NULL,
    umbral_alerta NUMERIC(12, 3) NOT NULL,
    deficit_calculado NUMERIC(12, 3) NOT NULL,
    atendida BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_ale_insumo FOREIGN KEY (insumo_id) 
        REFERENCES articulos (id)
);

-- 8. TABLA: TANDAS_PRODUCCION (Registro de Horneadas del Maestro Panadero)
CREATE TABLE IF NOT EXISTS tandas_produccion (
    id BIGINT PRIMARY KEY,
    fecha DATE NOT NULL,
    articulo_producto_id BIGINT NOT NULL,
    harina_base_kg NUMERIC(10, 2) NOT NULL,
    unidades_obtenidas INTEGER NOT NULL,
    maestro_panadero VARCHAR(100) NOT NULL,
    observaciones VARCHAR(255),
    CONSTRAINT fk_tanda_producto FOREIGN KEY (articulo_producto_id) 
        REFERENCES articulos (id)
);

-- 9. TABLA: DETALLES_TANDA_INSUMO (Insumos Consumidos por Tanda)
CREATE TABLE IF NOT EXISTS detalles_tanda_insumo (
    id BIGINT PRIMARY KEY,
    tanda_id BIGINT NOT NULL,
    insumo_id BIGINT NOT NULL,
    cantidad_consumida NUMERIC(10, 3) NOT NULL,
    CONSTRAINT fk_dti_tanda FOREIGN KEY (tanda_id) 
        REFERENCES tandas_produccion (id) ON DELETE CASCADE,
    CONSTRAINT fk_dti_insumo FOREIGN KEY (insumo_id) 
        REFERENCES articulos (id)
);
