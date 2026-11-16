# CU-03 Registrar una tanda de producción (CU-03-HU-01 y CU-03-HU-02)

Responsable: Brayan Nikolás Gallo León (`NikkoWebDev`) · Issues: #8 y #9 (épica #7)

> Este documento sigue el mismo formato que [conciliacion-cu01.md](../hito-4/conciliacion-cu01.md)
> y la receta de la sección 12 de [guia-desde-cero.md](../guia-desde-cero.md).

## 1. Objetivo

- **CU-03-HU-01 (Registrar la harina pesada, las unidades obtenidas y los insumos
  gastados):** el formulario registra la tanda en una sola acción.
- **CU-03-HU-02 (Descontar los insumos y avisar si no alcanzan):** al registrar, el
  sistema verifica que cada insumo alcance, descuenta lo gastado de las existencias y, si
  alguno no alcanza, indica cuál y no registra nada.

A diferencia del CU-01 (que guarda un borrador y luego se confirma), aquí **registrar y
descontar es una sola acción**, dentro de una misma transacción.

## 2. Flujo en la aplicación

```
GET  /produccion/tandas        → formulario (fecha, producto, harina, unidades, maestro,
                                  observaciones y una línea por insumo gastado)
POST /produccion/tandas        → verifica existencia, registra la tanda y descuenta los
                                  insumos; si uno no alcanza, vuelve al formulario con el aviso
GET  /produccion/tandas/{id}   → resumen de la tanda y de los insumos descontados
```

## 3. Criterios de aceptación y dónde se cumplen

| Historia | Criterio | Implementación | Prueba |
|---|---|---|---|
| HU-01 | 1. Digitar harina, unidades e insumos gastados | `TandaForm`, `nueva-tanda.html`, `ProduccionController.registrar` | `elFormularioMuestraLosInsumos` |
| HU-01 | 2. Al confirmar, queda registrada la tanda | `TandaProduccionService.registrarTanda` (guarda cabecera y líneas) | `registrarTandaGuardaCabeceraYLineas` |
| HU-01 | 3. Si se cancela, no se registra ni se descuenta nada | "Cancelar" es un enlace: no envía nada. `@Transactional` hace rollback si algo falla | — |
| HU-02 | 1. Antes de registrar, verificar existencia suficiente | `registrarTanda` revisa cada insumo **antes** de guardar o descontar | `siUnInsumoNoAlcanzaNoRegistraNiDescuenta` |
| HU-02 | 2. Si un insumo no alcanza, indicar cuál y no registrar | `InsumoInsuficienteException` (guarda el nombre) → aviso en pantalla | `siUnInsumoNoAlcanzaVuelveAlFormularioConElAviso` |
| HU-02 | 3. Al registrar, los insumos quedan descontados | `insumo.setExistenciaActual(actual − gastado)` (dirty checking) | `registrarTandaDescuentaLosInsumos`, `registrarTandaDescuentaYMuestraElResumen` |
| HU-02 | 4. Si la operación falla, no se descuenta nada | Verificar todo antes de descontar + rollback de `@Transactional` | `siUnInsumoNoAlcanzaNoRegistraNiDescuenta` |

`ProduccionControllerTest.registrarTandaDescuentaYMuestraElResumen` es además la prueba
**end to end** (con MockMvc): recorre GET del formulario → POST → seguir la redirección al
resumen → verificar en la base que el insumo quedó descontado.

## 4. Qué se construyó

| Capa | Archivo | Rol |
|---|---|---|
| presentacion/controlador | `ProduccionController` | Las 3 rutas de la sección 2. |
| presentacion/formulario | `TandaForm` | Datos del formulario y validaciones de formato. |
| negocio/servicio | `TandaProduccionService` | Verificar existencia, registrar y descontar. |
| negocio/dto | `InsumoGastado` | `record` que lleva cada línea del controlador al servicio. |
| negocio/excepcion | `InsumoInsuficienteException` | Error de negocio cuando un insumo no alcanza. |
| persistencia/entidad | `TandaProduccion`, `DetalleTandaInsumo` | Tablas de la tanda y sus líneas (ya existían desde la Fase 2). |
| persistencia/repositorio | `TandaProduccionRepository`, `DetalleTandaInsumoRepository` | Consultas de tandas y de sus líneas. |
| templates | `produccion/nueva-tanda.html`, `produccion/resumen.html` | HTML plano con Thymeleaf. |

## 5. Decisiones de diseño

### 5.1 La harina es una línea de insumo más
`harina_base_kg` queda en la cabecera de la tanda como dato informativo (sirve para el
cálculo de rendimiento más adelante), pero **el descuento de la harina entra como una línea
de insumo gastado más**. Así no se descuenta dos veces: todo lo que se descuenta sale de
`detalles_tanda_insumo`.

### 5.2 Registrar y descontar en una sola acción
El CU-01 separa borrador y confirmación porque primero hay que ver las diferencias. En una
tanda no hay ese paso intermedio: el maestro pesa, hornea y registra. Por eso
`registrarTanda` guarda y descuenta en el mismo método `@Transactional`.

### 5.3 Si un insumo no alcanza, se bloquea
Si `existenciaActual < cantidad`, se lanza `InsumoInsuficienteException` y no se registra
nada. No se permite existencia negativa. Se verifica **todo** antes de guardar o descontar,
para que un fallo no deje la base a medias (reforzado por el rollback de la transacción).

### 5.4 No se suma a la existencia del producto horneado
HU-02 solo pide descontar insumos. El pan es `PRODUCTO_ELABORADO` y no se concilia
(Entrega 2), así que la tanda no aumenta la existencia del producto. Queda *fuera por ahora*.

## 6. Cómo probar

```bash
./mvnw test              # incluye las pruebas del CU-03 (repositorio, excepción, servicio y web)
./mvnw spring-boot:run   # http://localhost:8080/produccion/tandas
```

Prueba manual con los datos de ejemplo: registrar una tanda de Pan Francés que gaste
**10 kg** de harina (había 15). Al guardar, el resumen muestra la tanda y la harina queda
en **5 kg**. Si en vez de 10 se piden **20 kg**, el sistema no registra la tanda y avisa que
la harina no alcanza.
