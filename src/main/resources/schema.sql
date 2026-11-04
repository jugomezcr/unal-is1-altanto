-- =============================================================================
-- ESQUEMA RELACIONAL H2 — SISTEMA AL-TANTO (PANADERÍA DE LEONARDO BETANCUR)
-- Ingeniería de Software I · Equipo Hefesto · Grupo 4
-- =============================================================================
-- Cuándo corre: Spring lo ejecuta al arrancar (spring.sql.init.mode=always), ANTES de
-- data.sql. "IF NOT EXISTS" hace que no falle ni borre nada si las tablas ya existen.
-- Relación con Java: cada tabla con clase @Entity está en persistencia/entidad/.
-- Hibernate NO crea tablas (ddl-auto=validate): solo revisa que coincidan con las
-- entidades: si una entidad pide una columna que no existe aquí, la app no arranca.
-- =============================================================================

-- Secuencias para generación de identificadores.
-- Las usa @GeneratedValue(SEQUENCE) en cada entidad. Empiezan en 100 para que los ids
-- no choquen con los ids fijos (1, 2, 3...) de los datos de ejemplo de data.sql.
CREATE SEQUENCE IF NOT EXISTS proveedor_seq START WITH 100;
CREATE SEQUENCE IF NOT EXISTS articulo_seq START WITH 100;
CREATE SEQUENCE IF NOT EXISTS conciliacion_seq START WITH 100;
CREATE SEQUENCE IF NOT EXISTS detalle_conciliacion_seq START WITH 100;
CREATE SEQUENCE IF NOT EXISTS alerta_seq START WITH 100;
CREATE SEQUENCE IF NOT EXISTS tanda_seq START WITH 100;
CREATE SEQUENCE IF NOT EXISTS detalle_tanda_seq START WITH 100;

-- 1. TABLA: PROVEEDORES
-- Entidad: Proveedor
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
-- Entidad: Articulo. tipo_articulo = INSUMO | PRODUCTO_REVENTA | PRODUCTO_ELABORADO
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
-- Entidad: InsumoConfig. Relación 1 a 1 con articulos (misma llave primaria).
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
-- Todavía sin entidad: se usará cuando se integre el reporte Z por producto.
CREATE TABLE IF NOT EXISTS productos_venta_config (
    articulo_id BIGINT PRIMARY KEY,
    codigo_plu INTEGER NOT NULL UNIQUE,
    precio_venta_unitario NUMERIC(10, 2) NOT NULL,
    CONSTRAINT fk_pvc_articulo FOREIGN KEY (articulo_id) 
        REFERENCES articulos (id) ON DELETE CASCADE
);

-- 5. TABLA: CONCILIACIONES_DIARIAS (Cierres de Caja y Turnos Diarios)
-- Entidad: ConciliacionDiaria (CU-01). UNIQUE en la fecha: una conciliación por día.
CREATE TABLE IF NOT EXISTS conciliaciones_diarias (
    id BIGINT PRIMARY KEY,
    fecha_conciliacion DATE NOT NULL UNIQUE,
    total_venta_reporte_z NUMERIC(12, 2) NOT NULL,
    fecha_hora_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    estado VARCHAR(20) NOT NULL DEFAULT 'BORRADOR',
    observaciones VARCHAR(500)
);

-- 6. TABLA: DETALLES_CONCILIACION (Líneas de Balance Físico vs Teórico)
-- Entidad: DetalleConciliacion (CU-01). Una línea por artículo conciliado.
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
-- Todavía sin entidad: hoy las alertas se calculan al vuelo (CU-02); no se guardan.
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
-- Entidad: TandaProduccion (CU-03).
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
-- Entidad: DetalleTandaInsumo (CU-03). ON DELETE CASCADE: borrar la tanda borra sus líneas.
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
