# CU-02 Generar el pedido ante una alerta de reabastecimiento (CU-02-HU-01)

Responsable: Pedro Santiago Correa Bolaños (`pedroerjes`) · Issues: #5 (épica #4)

> Este documento sigue la misma estructura de [conciliacion-cu01.md](conciliacion-cu01.md)
> y [produccion-cu03.md](../hito-5/produccion-cu03.md).

## 1. Objetivo

- **CU-02-HU-01 (Ver los insumos en alerta):** el panel web identifica en tiempo real
  los insumos cuya existencia física ha alcanzado o cruzado su umbral mínimo de seguridad,
  evaluando el consumo promedio diario y calculando los días de cobertura restante para
  priorizar qué insumos requieren abastecimiento urgente antes de que la producción se detenga.
- **CU-02-HU-02 (Generar el pedido al proveedor habitual):** de acuerdo con la planeación
  del proyecto (ver sección 3.2 de [plan-y-trazabilidad.md](plan-y-trazabilidad.md)), la
  generación y sugerencia automatizada de pedidos de compra se desacopló del panel de alertas.
  El ciclo de pedidos se formalizó en el **CU-04** (Recepción de pedidos y compras de emergencia).

## 2. Flujo en la aplicación

```
GET  /inventario/alertas  → consulta insumos en la base, calcula cobertura y déficit,
                            ordena por severidad y presenta el panel visual con semáforo
```

1. El usuario accede a la ruta `/inventario/alertas` (enlace **Alertas** en la barra superior).
2. `AlertaInsumosService` consulta los insumos activos en `FuenteInsumos` (`FuenteInsumosPersistencia` sobre `InsumoRepository` e `InsumoConfigRepository`).
3. Filtra los insumos configurados como visibles (`altanto.alertas.codigos-visibles`).
4. Para cada insumo, `CalculadoraAlerta` evalúa de forma determinista el nivel de alerta, el déficit y la cobertura estimada en días.
5. Los insumos se ordenan de mayor a menor urgencia (`CRÍTICO` → `ALERTA` → `NORMAL`, mayor déficit primero).
6. Thymeleaf renderiza `templates/inventario/alertas.html` resaltando las filas en riesgo.

## 3. Criterios de aceptación y dónde se cumplen

| Historia | Criterio | Implementación | Prueba |
|---|---|---|---|
| HU-01 | 1. Listar insumos en el umbral o por debajo con existencia, umbral y consumo | `alertas.html`, `InventarioAlertasController.mostrarAlertas`, `AlertaInsumosService.obtenerPanel` | `harinaEsCriticaConCoberturaDe043Dias`, `InventarioAlertasControllerTest` |
| HU-01 | 2. Alerta visible al consultar las alertas vigentes y tras la conciliación | Enlace en navbar `layout.html`, consulta en vivo sobre `Articulo.existenciaActual` actualizada por CU-01 o CU-03 | `CalculadoraAlertasTest`, `ServicioAlertasTest.alertaSiExistenciaEnUmbral` |
| HU-01 | 3. Si se consulta sin hacer pedido, la alerta sigue vigente | La consulta es de solo lectura (`read-only`), sin mutaciones de estado ni efectos colaterales | `CalculadoraAlertasTest.existenciaIgualAlUmbralCuentaComoAlerta` |

## 4. Qué se construyó

| Capa | Archivo | Rol |
|---|---|---|
| presentacion/controlador | `InventarioAlertasController` | Atiende `GET /inventario/alertas` y pasa el panel al modelo. |
| negocio/servicio | `AlertaInsumosService`, `FuenteInsumos` | Coordina la extracción de datos, filtrado de códigos visibles y ordenamiento. |
| negocio/calculo | `CalculadoraAlerta` | Reglas puras de cobertura, déficit y severidad (sin dependencias de Spring ni BD). |
| negocio/dto | `DatosInsumo`, `InsumoPanel`, `NivelAlerta` | Inmutables (`record` y `enum`) para transportar datos entre capas. |
| persistencia/entidad | `Articulo`, `InsumoConfig` | Mapeo relacional de existencias (`articulos`) y umbrales/consumos (`insumos_config`). |
| persistencia/repositorio | `InsumoRepository`, `InsumoConfigRepository`, `FuenteInsumosPersistencia` | Consultas Spring Data JPA para extraer insumos y configuraciones de alerta. |
| templates | `inventario/alertas.html` | Plantilla Thymeleaf con tabla de estados, unidades de medida y semáforo visual. |

## 5. Reglas de cálculo y decisiones de diseño

### 5.1 Fórmulas de alerta en `CalculadoraAlerta`

1. **Existencia efectiva:** Valores nulos o negativos se normalizan a `0`.
2. **Cobertura en días:**
   $$\text{Cobertura} = \frac{\text{Existencia}}{\text{Consumo Diario}}$$
   Calculada con redondeo simétrico a 2 decimales (`RoundingMode.HALF_UP`). Si el consumo diario no está registrado o es $\le 0$, se marca como no disponible.
3. **Nivel de alerta (`NivelAlerta`):**
   - **`NORMAL`:** Si la existencia es estrictamente mayor que el umbral mínimo de seguridad.
   - **`CRITICO`:** Si la existencia es $\le 0$ (quiebre de inventario) o si la cobertura en días es estrictamente menor al límite crítico configurado (1 día por defecto).
   - **`ALERTA`:** Si la existencia es menor o igual al umbral mínimo, pero supera el límite de cobertura crítico.
4. **Déficit:**
   $$\text{Déficit} = \max(0, \text{Umbral} - \text{Existencia})$$
   En estado `NORMAL`, el déficit es siempre 0.

### 5.2 Desacoplamiento de lógica pura
`CalculadoraAlerta` no utiliza anotaciones de Spring ni interactúa con la base de datos. Esto permite ejecutar pruebas unitarias instantáneas con combinaciones extremas de datos (valores nulos, negativos, decimales sin consumo registrado) sin levantar el contexto de Spring Boot.

### 5.3 Configuración desacoplada
Los códigos de insumos visibles en el panel se definen de forma extensible mediante la propiedad `altanto.alertas.codigos-visibles` (por defecto: `INS-AZU-01,INS-HAR-01,INS-LEV-01,INS-MAR-01,INS-QUE-01`), permitiendo incorporar nuevos insumos sin modificar el código fuente.
