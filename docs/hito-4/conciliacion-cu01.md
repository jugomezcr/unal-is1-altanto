# CU-01 Conciliar el día (CU-01-HU-01 y CU-01-HU-02)

Responsable: Juan Pablo Gómez Cristancho · Rama: `feat/C1H1` · Issues: #2 y #3 (épica #1)

## 1. Objetivo

Implementar las dos historias del CU-01 de la Entrega 2:

- **CU-01-HU-01 (Digitar lo vendido y lo contado del día):** el formulario guarda la
  conciliación como **BORRADOR**.
- **CU-01-HU-02 (Ver las diferencias y confirmar la conciliación):** la vista de
  diferencias permite **confirmar** (pasa a CONFIRMADA) o **descartar** el borrador.

## 2. Flujo en la aplicación

```
GET  /conciliacion                 → formulario (fecha, total reporte Z, vendido y contado por artículo)
POST /conciliacion                 → guarda el BORRADOR y redirige a la vista de diferencias
GET  /conciliacion/{id}            → tabla: anterior + recibido − salida = esperado vs. contado → diferencia
POST /conciliacion/{id}/confirmar  → CONFIRMADA, lo contado pasa a existencia y se muestran las alertas
POST /conciliacion/{id}/descartar  → borra el borrador (no queda nada guardado)
GET  /conciliacion/historial       → cierres anteriores: fecha, total Z, estado, artículos y cuántos con diferencia
```

## 3. Criterios de aceptación y dónde se cumplen

| Historia | Criterio | Implementación | Prueba |
|---|---|---|---|
| HU-01 | 1. Digitar por artículo lo vendido y lo contado, y el total del día | `nueva.html`, `ConciliacionForm`, `ConciliacionService.registrarBorrador` | `registraElBorradorCalculandoEsperadoYDiferencia`, `guardarRedirigeALasDiferencias` |
| HU-01 | 2. Si ya existe la fecha, no crear otra y ofrecer la existente (ext. 1a) | `ConciliacionDuplicadaException` y el enlace "Ver la existente" | `noPermiteDosConciliacionesDeLaMismaFechaYOfreceLaExistente`, `fechaDuplicadaOfreceVerLaExistente` |
| HU-01 | 3. Si falla o se cancela, no se guarda nada y los días anteriores no cambian | `@Transactional` (rollback). "Cancelar" no envía nada. Solo se concilian fechas posteriores a la última confirmada | `siUnaLineaFallaNoQuedaGuardadaNiLaCabecera`, `noPermiteConciliarDiasYaCerradosNiFechasFuturas` |
| HU-02 | 1. Calcular esperado (anterior + recibido − vendido o consumido en tandas) y diferencia | `DetalleConciliacion.calcular()`, `ConciliacionService.crearDetalle` | `calculaEsperadaComoAnteriorMasRecibidoMenosSalida`, `restaLoConsumidoEnLasTandasDelDia` |
| HU-02 | 2. Señalar los artículos con diferencia | `DetalleConciliacion.tieneDiferencia()`, filas en rojo en `resumen.html` | `sinDiferenciaCuandoLoContadoCuadra` |
| HU-02 | 3. Al confirmar, se guarda la diferencia y lo contado queda como existencia de partida | `ConciliacionDiaria.confirmar()`, `ConciliacionService.confirmar` | `alConfirmarLoContadoQuedaComoExistenciaYSeMuestraLaAlerta` |
| HU-02 | 4. Si un insumo queda en su umbral o por debajo, mostrar la alerta | `InsumoConfigRepository.buscarInsumosEnAlerta`, recuadro rojo en `resumen.html` | ídem |
| HU-02 | Compromiso E4: "consultar cierres previos" | `ConciliacionRepository.buscarHistorial`, `ConciliacionService.historial`, `historial.html` | `historialListaLosCierresPreviosDelMasRecienteAlMasAntiguo`, `historialMuestraLosCierresPreviosConEnlaceAlDetalle` |

## 4. Qué se construyó

| Capa | Archivo | Rol |
|---|---|---|
| presentacion | `ConciliacionController` | Rutas de la sección 2. |
| presentacion | `ConciliacionForm` | Datos del formulario y validaciones de formato (`@NotNull`, `@PositiveOrZero`). |
| negocio | `ConciliacionService` | Reglas y transacciones del CU-01. |
| negocio | `EstadoConciliacion` | Enum `BORRADOR` / `CONFIRMADA` (antes era un `String`). |
| negocio | `ReglaDeNegocioException`, `ConciliacionDuplicadaException`, `ConciliacionNoEncontradaException` | Errores de negocio (la `BusinessRuleException` de la Entrega 3). |
| negocio | `ConteoArticulo`, `InsumoEnAlerta`, `CierrePrevio` | `record` que llevan datos entre capas. |
| negocio | `ConciliacionDiaria.confirmar()`, `DetalleConciliacion.calcular()` / `tieneDiferencia()` | Reglas que viven en las entidades. |
| persistencia | `ConciliacionRepository` (+2 métodos), `DetalleConciliacionRepository`, `DetalleTandaInsumoRepository`, `InsumoConfigRepository`, `InsumoRepository` (+1 método) | Consultas. |
| templates | `conciliacion/nueva.html`, `conciliacion/resumen.html`, `conciliacion/historial.html` | Vistas Thymeleaf con Tailwind. Campos grandes y teclado numérico (criterio UX de la Entrega 3). |

## 5. Decisiones de diseño

### 5.1 Qué artículos se concilian
La Entrega 2 (tabla 2, HEFESTO-02) dice que *"la conciliación cubre insumos y productos de
reventa"*: el pan no vendido no se registra. Por eso `tipo_articulo` ahora tiene tres
valores (constantes en `Articulo`):

| Valor | Ejemplos | ¿Se concilia? |
|---|---|---|
| `INSUMO` | harina, levadura, margarina | Sí. Se resta lo consumido en las tandas del día. |
| `PRODUCTO_REVENTA` | kumis, gaseosa | Sí. Se resta lo vendido según el reporte Z. |
| `PRODUCTO_ELABORADO` | pan francés, pan trenza | No. Es el producto de una tanda (CU-03). |

En `data.sql`, el antiguo `PRODUCTO_VENTA` se separó en esos dos valores y se agregó una
gaseosa de reventa (el ejemplo de las "50 gaseosas" de la entrevista HEFESTO-01).

### 5.2 Existencia anterior
Es lo **contado en la última conciliación confirmada** antes de la fecha. Si el artículo
nunca se ha conciliado, se usa `existencia_actual`. No se parte siempre de
`existencia_actual` porque desde el Sprint 2 las tandas (CU-03-HU-02) y las recepciones
(CU-04) la modificarán durante el día, y esos movimientos se contarían dos veces.

### 5.3 Recibido = 0 por ahora
Las recepciones de pedidos y las compras de emergencia son del CU-04 (Sprint 2). La
columna ya existe y la fórmula ya la suma. Cuando exista CU-04, solo hay que llenarla en
`ConciliacionService.crearDetalle`.

### 5.4 Se guarda un borrador antes de confirmar
Así lo pide el plan de la Entrega 4 (paso 8). La Entrega 3 también pide *"persistencia de
borradores ante pausas"*. Cancelar en el formulario no guarda nada. Una vez guardado, el
borrador se puede **descartar**. Ninguna de las dos acciones toca las existencias ni los
días anteriores (garantía mínima).

### 5.5 Solo fechas posteriores a la última confirmada, y nunca futuras
Confirmar un día anterior a uno ya cerrado reescribiría existencias de días anteriores,
y la garantía mínima del CU-01 lo prohíbe.

### 5.6 La alerta no se guarda en `alertas_reabastecimiento`
HU-02 solo pide **mostrar** la alerta, y por eso se calcula con
`InsumoConfigRepository.buscarInsumosEnAlerta()`. La pantalla y el manejo de alertas
vigentes son de CU-02-HU-01 (`/inventario/alertas`), a la que se enlaza.

## 6. Puntos de contacto con las otras historias del Sprint 1

- **CU-02-HU-01 (#5, Pedro):** puede reutilizar `InsumoConfigRepository.buscarInsumosEnAlerta()`,
  que ya compara cada insumo contra **su** umbral. Para el proveedor habitual hay que
  ampliar el `record` o la consulta. La ruta `/inventario/alertas` sigue provisional en
  `HomeController`.
- **CU-03-HU-01 (#8, Brayan Nikolás):** el producto de una tanda debe ser un artículo
  `PRODUCTO_ELABORADO`. `DetalleTandaInsumoRepository` ya existe y se le pueden agregar
  métodos. La ruta `/produccion/tandas` sigue provisional en `HomeController`.
- **Base local:** como `data.sql` cambió, hay que borrar la carpeta `./data/` para
  recargar los datos semilla (ver la sección 4 de `plan-y-trazabilidad.md`).

## 7. Cómo probar

```bash
./mvnw test              # 22 pruebas: 3 unitarias + integración del servicio + MockMvc
./mvnw spring-boot:run   # http://localhost:8080/conciliacion
```

Prueba manual con los datos semilla: conciliar **hoy**. Contar 14 kg de harina (se
esperaban 15) y confirmar. Debe aparecer la diferencia −1 en rojo y, al confirmar, la
alerta de la harina (14 ≤ umbral de 25). Probar también la fecha **02/11/2026**: ya
tiene un borrador, así que el sistema ofrece verlo en lugar de crear otro.
