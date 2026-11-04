-- =============================================================================
-- DATOS SEMILLA — SISTEMA AL-TANTO (PANADERÍA DE LEONARDO BETANCUR)
-- Ingeniería de Software I · Equipo Hefesto · Grupo 4
-- =============================================================================
--
-- ¿Por qué cada INSERT tiene "WHERE NOT EXISTS (SELECT 1 FROM tabla)"?
-- La base H2 está en modo ARCHIVO (./data/altanto.mv.db) y Spring ejecuta este
-- script en CADA arranque (spring.sql.init.mode=always). Con un INSERT simple,
-- el segundo "./mvnw spring-boot:run" fallaría por llave primaria duplicada.
-- Con esta forma, cada bloque solo inserta si su tabla está vacía (primer
-- arranque). En los siguientes arranques no hace nada y respeta los datos que
-- la aplicación haya guardado (por ejemplo, existencias tras una conciliación).
-- No se usa MERGE porque sobrescribiría esos datos con los valores semilla.
-- Mismo patrón que el repositorio de referencia del curso.
-- Para volver a los datos semilla: detener la app y borrar la carpeta ./data/

-- 1. PROVEEDORES
INSERT INTO proveedores (id, nit, razon_social, nombre_contacto, telefono, categoria_suministro, activo)
SELECT * FROM (VALUES
(1, '890300120-1', 'Harinera del Valle S.A.', 'Carlos Restrepo', '3104567890', 'MATERIA_PRIMA', TRUE),
(2, '860000445-2', 'Levapan de Colombia S.A.', 'Marta Lucia Gomez', '3157891234', 'MATERIA_PRIMA', TRUE),
(3, '890900286-0', 'Cooperativa Colanta', 'Distribuidor Zona Centro', '3009876543', 'LACTEO', TRUE),
(4, '890903939-5', 'Postobón S.A.', 'Ruta Preventa 4', '3123456789', 'BEBIDA', TRUE)
) AS v (id, nit, razon_social, nombre_contacto, telefono, categoria_suministro, activo)
WHERE NOT EXISTS (SELECT 1 FROM proveedores);

-- 2. ARTICULOS (Insumos y Productos Terminados)
INSERT INTO articulos (id, codigo, nombre, descripcion, tipo_articulo, unidad_medida, existencia_actual, activo)
SELECT * FROM (VALUES
-- Insumos (Materia Prima del Obrador)
(1, 'INS-HAR-01', 'Harina de Trigo Especial', 'Bulto de 50 kg para panadería artesanal', 'INSUMO', 'KILOGRAMO', 15.000, TRUE),
(2, 'INS-LEV-01', 'Levadura Fresca en Bloque', 'Levadura viva de panificación en bloque de 500g', 'INSUMO', 'GRAMO', 1500.000, TRUE),
(3, 'INS-MAR-01', 'Margarina Industrial Hojaldre', 'Grasa vegetal para masa y hojaldre', 'INSUMO', 'KILOGRAMO', 12.000, TRUE),
(4, 'INS-AZU-01', 'Azúcar Blanca Refinada', 'Azúcar para formulación de pan dulce', 'INSUMO', 'KILOGRAMO', 8.500, TRUE),
(5, 'INS-QUE-01', 'Queso Campesino Rallado', 'Queso fresco para pan de queso y almojábanas', 'INSUMO', 'KILOGRAMO', 6.000, TRUE),
-- Productos Terminados (Venta de Mostrador)
(10, 'PRD-PAN-01', 'Pan Francés Tradicional', 'Pan baguette crocante por unidad', 'PRODUCTO_VENTA', 'UNIDAD', 45.000, TRUE),
(11, 'PRD-PAN-02', 'Pan Dulce Trenza', 'Pan especial con queso y bocadillo', 'PRODUCTO_VENTA', 'UNIDAD', 30.000, TRUE),
(12, 'PRD-BEB-01', 'Kumis Artesanal 250ml', 'Bebida fermentada en botella de vidrio', 'PRODUCTO_VENTA', 'UNIDAD', 18.000, TRUE)
) AS v (id, codigo, nombre, descripcion, tipo_articulo, unidad_medida, existencia_actual, activo)
WHERE NOT EXISTS (SELECT 1 FROM articulos);

-- 3. INSUMOS_CONFIG (Umbrales y presentación)
INSERT INTO insumos_config (articulo_id, proveedor_habitual_id, umbral_minimo, consumo_promedio_diario, presentacion_compra, factor_conversion_presentacion)
SELECT * FROM (VALUES
(1, 1, 25.000, 35.000, 'BULTO_50KG', 50.000), -- Umbral de media caneca (25 kg), existencia actual 15 kg (en alerta!)
(2, 2, 500.000, 750.000, 'PAQUETE', 500.000),
(3, 3, 10.000, 8.000, 'CAJA_CERRADA', 15.000),
(4, 1, 10.000, 7.000, 'BULTO_50KG', 50.000),
(5, 3, 5.000, 6.000, 'UNIDAD_SUELTA', 1.000)
) AS v (articulo_id, proveedor_habitual_id, umbral_minimo, consumo_promedio_diario, presentacion_compra, factor_conversion_presentacion)
WHERE NOT EXISTS (SELECT 1 FROM insumos_config);

-- 4. PRODUCTOS_VENTA_CONFIG (PLU y precios de mostrador)
INSERT INTO productos_venta_config (articulo_id, codigo_plu, precio_venta_unitario)
SELECT * FROM (VALUES
(10, 101, 800.00),
(11, 102, 1200.00),
(12, 201, 3500.00)
) AS v (articulo_id, codigo_plu, precio_venta_unitario)
WHERE NOT EXISTS (SELECT 1 FROM productos_venta_config);

-- 5. CONCILIACIONES_DIARIAS (Histórico y borrador)
INSERT INTO conciliaciones_diarias (id, fecha_conciliacion, total_venta_reporte_z, fecha_hora_registro, estado, observaciones)
SELECT * FROM (VALUES
(1, '2026-11-01', 345000.00, CURRENT_TIMESTAMP, 'CONFIRMADA', 'Cierre dominical sin novedades significativas.'),
(2, '2026-11-02', 180000.00, CURRENT_TIMESTAMP, 'BORRADOR', 'Turno matutino en revisión de conteo físico.')
) AS v (id, fecha_conciliacion, total_venta_reporte_z, fecha_hora_registro, estado, observaciones)
WHERE NOT EXISTS (SELECT 1 FROM conciliaciones_diarias);

-- 6. DETALLES_CONCILIACION
INSERT INTO detalles_conciliacion (id, conciliacion_id, articulo_id, existencia_anterior, cantidad_recibida, cantidad_salida, existencia_esperada, cantidad_contada, diferencia)
SELECT * FROM (VALUES
(1, 1, 10, 20.000, 80.000, 75.000, 25.000, 25.000, 0.000),
(2, 1, 1, 65.000, 0.000, 50.000, 15.000, 15.000, 0.000)
) AS v (id, conciliacion_id, articulo_id, existencia_anterior, cantidad_recibida, cantidad_salida, existencia_esperada, cantidad_contada, diferencia)
WHERE NOT EXISTS (SELECT 1 FROM detalles_conciliacion);

-- 7. ALERTAS_REABASTECIMIENTO
INSERT INTO alertas_reabastecimiento (id, insumo_id, fecha_generacion, existencia_alerta, umbral_alerta, deficit_calculado, atendida)
SELECT * FROM (VALUES
(1, 1, CURRENT_TIMESTAMP, 15.000, 25.000, 10.000, FALSE)
) AS v (id, insumo_id, fecha_generacion, existencia_alerta, umbral_alerta, deficit_calculado, atendida)
WHERE NOT EXISTS (SELECT 1 FROM alertas_reabastecimiento);

-- 8. TANDAS_PRODUCCION
INSERT INTO tandas_produccion (id, fecha, articulo_producto_id, harina_base_kg, unidades_obtenidas, maestro_panadero, observaciones)
SELECT * FROM (VALUES
(1, '2026-11-01', 10, 25.00, 120, 'Leonardo Betancur', 'Tanda matutina de pan francés crujiente.'),
(2, '2026-11-02', 11, 15.00, 70, 'Leonardo Betancur', 'Tanda de pan dulce trenza con queso.')
) AS v (id, fecha, articulo_producto_id, harina_base_kg, unidades_obtenidas, maestro_panadero, observaciones)
WHERE NOT EXISTS (SELECT 1 FROM tandas_produccion);

-- 9. DETALLES_TANDA_INSUMO
INSERT INTO detalles_tanda_insumo (id, tanda_id, insumo_id, cantidad_consumida)
SELECT * FROM (VALUES
(1, 1, 1, 25.000),
(2, 1, 2, 350.000),
(3, 2, 1, 15.000),
(4, 2, 4, 3.500)
) AS v (id, tanda_id, insumo_id, cantidad_consumida)
WHERE NOT EXISTS (SELECT 1 FROM detalles_tanda_insumo);
