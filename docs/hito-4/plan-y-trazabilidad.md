# Hito 4: plan, responsables y trazabilidad

Este documento une el plan comprometido para la Entrega 4
([PDF](Proy.%20Al%20tanto%20-%20Entrega%204.pdf)) con el backlog de GitHub y con el
código. Si algo cambia en uno de esos tres lugares, se actualiza aquí.

Guía de implementación acordada: repositorio de referencia del curso
<https://github.com/LeonardoDelgado1905/sweng1-proyecto-referencia>.

---

## 1. Alcance del Sprint 1 (= Hito 4)

El milestone **Sprint 1** de GitHub corresponde a este hito. Incluye las 4 historias
comprometidas en la Entrega 2 (tabla 3, backlog):

| Issue | Historia | Responsable | GitHub |
|---|---|---|---|
| #2 | CU-01-HU-01: Digitar lo vendido y lo contado del día | Juan Pablo Gómez | `jugomezcr` |
| #3 | CU-01-HU-02: Ver las diferencias y confirmar la conciliación | Juan Pablo Gómez | `jugomezcr` |
| #5 | CU-02-HU-01: Ver los insumos en alerta | Brayan Nikolás Gallo | `NikkoWebDev` |
| #8 | CU-03-HU-01: Registrar la harina pesada, las unidades obtenidas y los insumos gastados | Brayan Nikolás Gallo | `NikkoWebDev` |

Las historias de los Sprint 2 y 3 siguen en *To Do*.

## 2. Plan por fases

| Fase | Pasos | Responsable | Estado |
|---|---|---|---|
| 1. Infraestructura base | 1–3: Spring Boot 4 / Java 21 / `mvnw`, H2 en archivo `./data/altanto.mv.db`, Thymeleaf + Tailwind (CDN) | Julián Sánchez (`Julsanchezc`) | Hecho |
| 2. Persistencia | 4–5: `schema.sql` y `data.sql` (Pedro Correa, `pedroerjes`). 6–7: entidades JPA y repositorios (Leonardo Betancur, `BETAPANDERETA`) | Pedro y Leonardo | Hecho (PR #19). Ver [persistencia-jpa-y-repositorios.md](persistencia-jpa-y-repositorios.md) |
| 3. Historias de usuario | 8–9: CU-01-HU-01 y CU-01-HU-02 (rama `feat/C1H1`). 10–11: CU-02-HU-01 y CU-03-HU-01 | Juan Pablo y Brayan Nikolás | En curso |
| 4. Verificación y cierre | 12: `./mvnw spring-boot:run` arranca sin errores. 13: mover #2, #3, #5 y #8 a *In Review* o *Done*. 14: todos con commits significativos | Todo el equipo | Pendiente |

> **Paso 12, ya corregido:** `data.sql` usaba `INSERT` simples y, como la base es un
> archivo, el segundo arranque fallaba por llave primaria duplicada. Ahora cada bloque
> usa `WHERE NOT EXISTS`, el mismo patrón del repositorio de referencia.

## 3. Trazabilidad entre los hitos y el código

La **Entrega 2** es la fuente de verdad de los requisitos: casos de uso, historias y
criterios de aceptación, copiados textualmente en los issues de GitHub. La **Entrega 3**
propuso un diseño más amplio. Para este incremento se adaptó al plan de la Entrega 4 y
al repositorio de referencia, como se ve en esta tabla:

| Entrega 3 (diseño) | Implementación (Hito 4) | Razón |
|---|---|---|
| PostgreSQL 3FN | H2 en modo archivo | El plan de la Entrega 4 (paso 2) fija H2. El esquema conserva 3FN, `NUMERIC` para cantidades y dinero, y llaves foráneas. |
| 4 capas (presentación, aplicación, dominio, persistencia) más una capa transversal | 3 paquetes: `presentacion`, `negocio` (entidades y servicios con `@Transactional`) y `persistencia` | Es la estructura del repositorio de referencia. Los servicios de `negocio` cumplen el papel de la capa de aplicación. |
| `ConciliacionTurno` con `TipoTurno` y efectivo contado | `ConciliacionDiaria`: una por fecha, con el total del reporte Z | La Entrega 2 define la conciliación **por día** (extensión 1a). Además, efectivo vs. billeteras queda *fuera por ahora* (Entregas 1 y 2). |
| Consumo teórico por `Receta` | La salida de cada artículo = lo vendido + lo consumido en tandas registradas | Recetas y rendimiento calculado quedan *fuera por ahora* (Entrega 1, tabla 1). |
| `AjusteInventario` (mermas) | No se implementa. La diferencia queda registrada por artículo | Las mermas quedan *fuera por ahora* (Entregas 1 y 2). |
| `Insumo` con herencia `MateriaPrima` / `ProductoReventa` (TPH) | `Articulo` con la columna `tipo_articulo` (`INSUMO` / `PRODUCTO_VENTA`) | Es la misma tabla única (TPH) que propone la Entrega 3, pero sin la herencia en Java. |
| `PedidoReabastecimiento`, `EstadoPedido`, `PackagingRounder` | Sprint 2 (CU-02-HU-02) | No pertenecen al Sprint 1. |
| `BusinessRuleException` | Excepciones de negocio propias en `negocio/` | Mismo principio de la Entrega 3: los errores de negocio se expresan con excepciones con nombre. |

Los nombres de entidades del plan de la Entrega 4 (`ReporteZ`, `Conciliacion`, `Insumo`)
eran ilustrativos. Los nombres reales son los de la tabla de la sección 2.1 de
[persistencia-jpa-y-repositorios.md](persistencia-jpa-y-repositorios.md).
