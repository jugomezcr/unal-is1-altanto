# Capa de persistencia (Entidades jpa y repositorios)

## 1. Objetivo

Conectar el código Java con la base de datos H2 existente, de modo que la aplicación
pueda leer y escribir sin SQL manual. Esta capa es la base de las 4 historias del
Sprint 1 de la Entrega 4:

- **CU-01-HU-01** — Digitar lo vendido y lo contado del día.
- **CU-01-HU-02** — Ver diferencias y confirmar la conciliación.
- **CU-02-HU-01** — Ver insumos en alerta.
- **CU-03-HU-01** — Registrar tanda de producción.

Sin esta capa, los controladores de Fase 3 no tendrían de dónde obtener ni dónde
guardar datos.

---

## 2. Qué se construyó

### 2.1 Entidades JPA (`src/main/java/co/edu/unal/sweng1/altanto/negocio/`)

| Entidad | Tabla | Propósito |
|---|---|---|
| `Proveedor` | `proveedores` | Catálogo de proveedores habituales. |
| `Articulo` | `articulos` | Insumos y productos de venta. Núcleo del sistema. |
| `InsumoConfig` | `insumos_config` | Umbral mínimo y presentación de compra por insumo. |
| `ConciliacionDiaria` | `conciliaciones_diarias` | Cabecera del cierre diario (CU-01). |
| `DetalleConciliacion` | `detalles_conciliacion` | Línea por artículo: esperado vs. contado (CU-01). |
| `TandaProduccion` | `tandas_produccion` | Registro de horneadas (CU-03). |
| `DetalleTandaInsumo` | `detalles_tanda_insumo` | Insumos consumidos por tanda (CU-03). |

### 2.2 Repositorios (`src/main/java/co/edu/unal/sweng1/altanto/persistencia/`)

| Repositorio | Entidad | Consultas |
|---|---|---|
| `InsumoRepository` | `Articulo` | Heredados de `JpaRepository` + `findByExistenciaActualLessThanEqual(BigDecimal)` para CU-02-HU-01. |
| `ConciliacionRepository` | `ConciliacionDiaria` | Solo heredados (`save`, `findById`, `findAll`, `deleteById`, `count`). |
| `TandaProduccionRepository` | `TandaProduccion` | Solo heredados. |

> Con CU-01 se agregaron consultas a `ConciliacionRepository` e `InsumoRepository`, además de `DetalleConciliacionRepository`, `DetalleTandaInsumoRepository` e `InsumoConfigRepository`. Ver [conciliacion-cu01.md](conciliacion-cu01.md).

### 2.3 Test de integración (`src/test/java/co/edu/unal/sweng1/altanto/`)

| Test | Qué verifica |
|---|---|
| `InsumoRepositoryTest` | `findByExistenciaActualLessThanEqual(25)` devuelve al menos un artículo y entre ellos está la harina (`INS-HAR-01`), que `data.sql` deja con 15 kg. |

---

## 3. Decisiones de diseño

### 3.1 `BigDecimal` para cantidades y dinero

Todos los `NUMERIC` del `schema.sql` se mapean a `BigDecimal`, no a `double` ni
`float`. `double` tiene errores de redondeo binario (0.1 + 0.2 ≠ 0.3), inaceptables
para kilos o pesos. `BigDecimal` es de precisión arbitraria y respeta exactamente
la precisión y escala del SQL (`precision` y `scale` en `@Column`).Esta sugerencia fue dada por la IA, así que se decidió implementarla.

### 3.2 `@GeneratedValue(SEQUENCE)` con `allocationSize = 1`

Los IDs usan las secuencias del `schema.sql` (`articulo_seq`, `conciliacion_seq`,
`tanda_seq`, etc.), todas `START WITH 100`. Se usa `allocationSize = 1` para que
Hibernate pida los IDs de uno en uno y no choque con los datos semilla
(`data.sql` inserta IDs 1..4).

### 3.3 `InsumoConfig` sin `@GeneratedValue`

Su PK es `articulo_id`, que es a la vez FK de `articulos`. No tiene secuencia
propia. Se mapea como `@Id Long articuloId`, sin generación automática. Los FK
(`proveedor_habitual_id`) se mapean como `Long` simples por simplicidad; si en
Fase 3 se necesita navegar la relación, se agrega `@ManyToOne`.

### 3.4 Relaciones unidireccionales

`DetalleConciliacion` y `DetalleTandaInsumo` usan `@ManyToOne` hacia sus cabeceras
(`ConciliacionDiaria`, `TandaProduccion`) y hacia `Articulo`. Las cabeceras **no**
tienen `@OneToMany`. Para las historias del Sprint 1 no se necesita navegar de
cabecera a detalles; agregarlo ahora obligaría a manejar `mappedBy`, `cascade`,
`orphanRemoval` y recursión en `toString()`, sin beneficio para esta entrega.

### 3.5 `ddl-auto=validate`

Hibernate no crea ni modifica tablas. Solo valida que cada entidad coincida
exactamente con el `schema.sql`. Cualquier discrepancia (nombre de columna, tipo,
precisión) rompe el arranque con `Schema-validation: ...`. Por eso cada
`@Column(name = "...")` se copió letra por letra del SQL.

### 3.6 Spring Boot 4: import de `@DataJpaTest`

En Spring Boot 4, `@DataJpaTest` cambió de paquete. El import correcto es:

```java
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
```

El import antiguo (`org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest`)
ya no existe y hace fallar la compilación del test.
