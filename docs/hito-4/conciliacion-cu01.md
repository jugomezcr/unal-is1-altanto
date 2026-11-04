# CU-01 Conciliar el día (CU-01-HU-01 y CU-01-HU-02)

Responsable: Juan Pablo Gómez Cristancho · Issues: #2 y #3 (épica #1)

> **Versión simplificada (2026-11-04).** Para que el equipo aprenda Spring Boot paso a
> paso, el CU-01 quedó en su núcleo. Lo aplazado está en la sección 3.2 de
> [plan-y-trazabilidad.md](plan-y-trazabilidad.md) y en la sección 5 de este documento.

## 1. Objetivo

- **CU-01-HU-01 (Digitar lo vendido y lo contado del día):** el formulario guarda la
  conciliación como **BORRADOR**.
- **CU-01-HU-02 (Ver las diferencias y confirmar):** la vista de diferencias permite
  **confirmar**; lo contado queda como existencia actual.

## 2. Flujo en la aplicación

```
GET  /conciliacion                 → formulario (fecha, total reporte Z, vendido y contado por artículo)
POST /conciliacion                 → guarda el BORRADOR y redirige a la vista de diferencias
GET  /conciliacion/{id}            → tabla: anterior − vendido = esperado vs. contado → diferencia
POST /conciliacion/{id}/confirmar  → CONFIRMADA; lo contado pasa a ser la existencia actual
GET  /conciliacion/historial       → cierres anteriores: fecha, total Z y estado
```

## 3. Criterios de aceptación y dónde se cumplen

| Historia | Criterio | Implementación | Prueba |
|---|---|---|---|
| HU-01 | 1. Digitar por artículo lo vendido y lo contado, y el total del día | `nueva.html`, `ConciliacionForm`, `ConciliacionService.registrarBorrador` | `registrarBorradorCalculaLaDiferencia` |
| HU-01 | 2. Si ya existe la fecha, no crear otra y ofrecer la existente | `ConciliacionDuplicadaException` y el enlace "Ver la conciliación existente" | `noPermiteDosConciliacionesDelMismoDia` |
| HU-01 | 3. Si falla o se cancela, no se guarda nada | `@Transactional` (rollback). "Cancelar" es un enlace: no envía nada. No se concilian fechas futuras | — (aplazada la prueba de rollback) |
| HU-02 | 1. Calcular esperado y diferencia | `DetalleConciliacion.calcular()`: esperado = anterior − vendido | `calculaLoEsperadoYLaDiferencia` |
| HU-02 | 2. Señalar los artículos con diferencia | `DetalleConciliacion.tieneDiferencia()`, filas en rojo en `resumen.html` | ídem |
| HU-02 | 3. Al confirmar, lo contado queda como existencia | `ConciliacionDiaria.confirmar()`, `ConciliacionService.confirmar` | `confirmarDejaLoContadoComoExistencia`, `unaConciliacionSoloSePuedeConfirmarUnaVez` |
| HU-02 | 4. Mostrar la alerta si un insumo queda en su umbral | **Aplazado.** Las alertas se ven en el panel de CU-02 (`/inventario/alertas`) | — |
| HU-02 | Compromiso E4: "consultar cierres previos" | `ConciliacionService.historial`, `historial.html` | — |

Además, `ConciliacionControllerTest` es el ejemplo de prueba web (MockMvc).

## 4. Qué se construyó

| Capa | Archivo | Rol |
|---|---|---|
| presentacion/controlador | `ConciliacionController` | Las 5 rutas de la sección 2. |
| presentacion/formulario | `ConciliacionForm` | Datos del formulario y validaciones de formato. |
| negocio/servicio | `ConciliacionService` | Reglas y transacciones del CU-01. |
| negocio/dto | `ConteoArticulo` | `record` que lleva lo digitado del controlador al servicio. |
| negocio/excepcion | `ReglaDeNegocioException`, `ConciliacionDuplicadaException`, `ConciliacionNoEncontradaException` | Errores de negocio. |
| persistencia/entidad | `ConciliacionDiaria`, `DetalleConciliacion`, `EstadoConciliacion` | Tablas y reglas que viven en las entidades (`confirmar()`, `calcular()`). |
| persistencia/repositorio | `ConciliacionRepository`, `DetalleConciliacionRepository`, `InsumoRepository` | Solo consultas derivadas (por el nombre del método). |
| templates | `conciliacion/nueva.html`, `resumen.html`, `historial.html` | HTML plano con Thymeleaf. |

## 5. Decisiones de diseño

### 5.1 Qué artículos se concilian
La Entrega 2 (HEFESTO-02) dice que *"la conciliación cubre insumos y productos de
reventa"*: el pan no vendido no se registra.

| `tipo_articulo` | Ejemplos | ¿Se concilia? |
|---|---|---|
| `INSUMO` | harina, levadura | Sí. Por ahora sin restar lo consumido en tandas (aplazado). |
| `PRODUCTO_REVENTA` | kumis, gaseosa | Sí. Se resta lo vendido según el reporte Z. |
| `PRODUCTO_ELABORADO` | pan francés | No. Es el producto de una tanda (CU-03). |

### 5.2 Existencia anterior = existencia actual
Es lo más simple y sirve mientras nada más cambie la existencia durante el día. Cuando
CU-03 (tandas) o CU-04 (recepciones) modifiquen la existencia, habrá que partir de lo
contado en la última conciliación confirmada para no contar dos veces esos movimientos.

### 5.3 Columnas que existen pero no se usan todavía
`cantidad_recibida` (CU-04), `fecha_hora_registro` y `observaciones` siguen en `schema.sql`,
pero no están en las entidades: la base de datos les pone su valor por defecto. Se mapean
cuando haga falta.

### 5.4 Se guarda un borrador antes de confirmar
Así lo pide el plan de la Entrega 4 (paso 8). Mientras sea borrador, las existencias no
cambian.

## 6. Cómo probar

```bash
./mvnw test              # incluye las 7 pruebas del CU-01 (unitaria, integración, web y arranque)
./mvnw spring-boot:run   # http://localhost:8080/conciliacion
```

Prueba manual con los datos de ejemplo: conciliar **hoy**, contar 14 kg de harina (había
15) y guardar. La harina sale en rojo con diferencia −1. Al confirmar, la existencia de la
harina queda en 14. Con la fecha **01/11/2026** el sistema no deja crear otra y ofrece ver
la existente.
