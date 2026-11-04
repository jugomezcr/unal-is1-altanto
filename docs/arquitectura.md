# Arquitectura de Al-Tanto: cómo se conecta cada cosa

Esta guía es un **mapa**: dice dónde está cada pieza y cómo se habla con las demás. Está
escrita para quien ya hizo backends en Python (Flask, FastAPI, Django o Python puro) y se
encuentra con Spring Boot por primera vez. La explicación larga, concepto por concepto, está
en [Al-Tanto desde cero](guia-desde-cero.md).

## Contenido

1. [La idea en una frase](#1-la-idea-en-una-frase)
2. [Diccionario Python → Spring](#2-diccionario-python--spring)
3. [Las carpetas y qué va en cada una](#3-las-carpetas-y-qué-va-en-cada-una)
4. [Quién puede llamar a quién](#4-quién-puede-llamar-a-quién)
5. [El recorrido de una petición, archivo por archivo](#5-el-recorrido-de-una-petición-archivo-por-archivo)
6. [La "magia": quién crea los objetos](#6-la-magia-quién-crea-los-objetos)
7. [La base de datos: de `schema.sql` a un objeto Java](#7-la-base-de-datos-de-schemasql-a-un-objeto-java)
8. [Errores y transacciones](#8-errores-y-transacciones)
9. [Las pantallas: del `Model` al HTML](#9-las-pantallas-del-model-al-html)
10. [Las pruebas](#10-las-pruebas)
11. [Chuleta de anotaciones](#11-chuleta-de-anotaciones)
12. [¿Dónde pongo...?](#12-dónde-pongo)

---

## 1. La idea en una frase

> En Python **tú** creas los objetos y los conectas (`db = Database(); service = Service(db)`).
> En Spring **marcas** las clases con anotaciones (`@Service`, `@Controller`...) y Spring
> las crea y las conecta al arrancar.

Por eso parece magia: no se ve ningún `new ConciliacionService(...)` en todo el proyecto.
Spring lee las anotaciones, ve que `ConciliacionController` pide un `ConciliacionService` en
su constructor, y se lo pasa. Casi todo lo raro de Spring sale de ahí.

---

## 2. Diccionario Python → Spring

| Lo que harías en Python | En este proyecto | Dónde |
|---|---|---|
| `app = Flask(__name__)` + `app.run()` | `@SpringBootApplication` + `SpringApplication.run(...)` | `AltantoApplication.java` |
| `@app.get("/ruta")` / `@app.route` | `@GetMapping("/ruta")` / `@PostMapping` en una clase `@Controller` | `presentacion/controlador/` |
| `request.form["campo"]` o un modelo Pydantic | Una clase de formulario que Spring llena sola (*data binding*) | `presentacion/formulario/` |
| Validación de Pydantic (`Field(ge=0)`) | `@NotNull`, `@PositiveOrZero`, `@Digits` + `@Valid` | `ConciliacionForm.java` |
| `render_template("x.html", datos=...)` | `model.addAttribute("datos", ...)` + `return "x"` | Controladores + `templates/` |
| Jinja2 (`{{ x }}`, `{% for %}`, `{% extends %}`) | Thymeleaf (`th:text`, `th:each`, `th:replace`) | `src/main/resources/templates/` |
| Funciones de lógica en un `services.py` | Clases `@Service` | `negocio/servicio/` |
| `@dataclass(frozen=True)` / `NamedTuple` | `record` | `negocio/dto/` |
| `class MiError(Exception)` | `class MiError extends RuntimeException` | `negocio/excepcion/` |
| Modelo de SQLAlchemy / Django (`class X(Base)`) | Clase `@Entity` | `persistencia/entidad/` |
| `session.query(X).filter_by(...)` / funciones con SQL | Interfaz `JpaRepository` (Spring escribe el código) | `persistencia/repositorio/` |
| `with session.begin():` (todo o nada) | `@Transactional` en el método del servicio | `negocio/servicio/` |
| `typing.Protocol` / clase abstracta | `interface` | `FuenteInsumos.java` |
| `.env` / `settings.py` | `application.properties` | `src/main/resources/` |
| Migración / `CREATE TABLE` a mano | `schema.sql` (corre en cada arranque, sin borrar) | `src/main/resources/` |
| Script de datos semilla | `data.sql` (solo inserta si la tabla está vacía) | `src/main/resources/` |
| `pip` + `requirements.txt` | Maven + `pom.xml` (`./mvnw` lo descarga todo) | raíz del repo |
| `pytest` | JUnit 5 (`@Test`) + `./mvnw test` | `src/test/java/` |
| `TestClient` de FastAPI / `app.test_client()` | `MockMvc` | Pruebas de `presentacion/controlador/` |

---

## 3. Las carpetas y qué va en cada una

```
src/main/java/co/edu/unal/sweng1/altanto/
│
├── AltantoApplication.java          Arranque. Spring escanea desde aquí hacia abajo.
│
├── presentacion/                    CAPA 1: lo que toca el navegador
│   ├── controlador/                 Una clase por módulo: URL → método → plantilla
│   │   ├── HomeController           GET /  y la ruta provisional de CU-03
│   │   ├── ConciliacionController   CU-01: /conciliacion/...
│   │   └── InventarioAlertasController  CU-02: /inventario/alertas
│   └── formulario/                  Objetos que reciben un <form> y sus validaciones de formato
│       └── ConciliacionForm
│
├── negocio/                         CAPA 2: las reglas (no sabe nada de HTML)
│   ├── servicio/                    @Service: casos de uso, transacciones, reglas
│   │   ├── ConciliacionService      CU-01-HU-01 y HU-02
│   │   ├── AlertaInsumosService     CU-02-HU-01
│   │   └── FuenteInsumos            interfaz: "de dónde salen los insumos"
│   ├── calculo/                     Cálculos puros: sin Spring, sin base de datos
│   │   └── CalculadoraAlerta        nivel, déficit y cobertura de un insumo
│   ├── dto/                         records: datos que viajan entre capas
│   │   ├── ConteoArticulo           formulario → servicio (CU-01)
│   │   ├── CierrePrevio             consulta → historial (CU-01)
│   │   ├── InsumoEnAlerta           consulta → alerta al confirmar (CU-01)
│   │   ├── DatosInsumo              consulta → calculadora (CU-02)
│   │   └── InsumoPanel, NivelAlerta calculadora → pantalla (CU-02)
│   └── excepcion/                   Errores de negocio con nombre propio
│       ├── ReglaDeNegocioException  la base de todas
│       ├── ConciliacionDuplicadaException
│       └── ConciliacionNoEncontradaException
│
└── persistencia/                    CAPA 3: la base de datos
    ├── entidad/                     @Entity: una clase = una tabla de schema.sql
    │   ├── Articulo, Proveedor, InsumoConfig
    │   ├── ConciliacionDiaria, DetalleConciliacion, EstadoConciliacion (CU-01)
    │   └── TandaProduccion, DetalleTandaInsumo (CU-03)
    └── repositorio/                 Interfaces de consultas (Spring las implementa)
        ├── InsumoRepository         tabla articulos
        ├── ConciliacionRepository, DetalleConciliacionRepository
        ├── InsumoConfigRepository, DetalleTandaInsumoRepository, TandaProduccionRepository
        └── FuenteInsumosPersistencia   implementación real de FuenteInsumos

src/main/resources/
├── application.properties           configuración (base de datos, consola H2)
├── schema.sql                       crea las tablas (si no existen)
├── data.sql                         datos de ejemplo (si las tablas están vacías)
└── templates/                       HTML con Thymeleaf, una carpeta por módulo
    ├── fragments/layout.html        cabecera, menú y pie comunes
    ├── conciliacion/  inventario/  produccion/

src/test/java/...                    mismas carpetas que src/main: cada prueba junto a lo que prueba
src/test/resources/application.properties   configuración solo para pruebas (H2 en memoria)
```

**Por qué esta división.** Antes, `negocio/` mezclaba en una sola carpeta tablas, reglas,
excepciones y records. Ahora cada subcarpeta tiene **un solo tipo** de clase, y el nombre de
la carpeta dice qué es:

- ¿Es una tabla? → `persistencia/entidad/`
- ¿Es una consulta? → `persistencia/repositorio/`
- ¿Es una regla o un caso de uso? → `negocio/servicio/`
- ¿Es un cálculo que se puede probar sin base de datos? → `negocio/calculo/`
- ¿Es una caja de datos? → `negocio/dto/`
- ¿Es un error? → `negocio/excepcion/`
- ¿Recibe una URL? → `presentacion/controlador/`
- ¿Recibe un `<form>`? → `presentacion/formulario/`

> **Diferencia con el repositorio de referencia del curso:** allí las entidades están en
> `negocio/`. Aquí están en `persistencia/entidad/` para tener junto todo lo que define y
> consulta las tablas. Las tres capas (`presentacion`, `negocio`, `persistencia`) se
> mantienen. Ver la sección 3 de [plan-y-trazabilidad.md](hito-4/plan-y-trazabilidad.md).

---

## 4. Quién puede llamar a quién

```
   Navegador
       │  HTTP
       ▼
 ┌─────────────────────────┐
 │ presentacion            │  controlador ──usa──► formulario
 │                         │       │
 └───────────┬─────────────┘       │ llama a métodos del servicio,
             │                     │ le pasa dto (ConteoArticulo...)
             ▼                     ▼
 ┌─────────────────────────┐
 │ negocio                 │  servicio ──usa──► calculo, dto, excepcion
 │                         │       │
 └───────────┬─────────────┘       │ llama a repositorios,
             │                     │ recibe entidades o dto
             ▼                     ▼
 ┌─────────────────────────┐
 │ persistencia            │  repositorio ──devuelve──► entidad
 └───────────┬─────────────┘
             ▼
         Base H2 (./data/altanto.mv.db)
```

Reglas:

1. **Hacia abajo, nunca hacia arriba.** Un servicio no importa un controlador; un
   repositorio no importa un servicio.
2. **El controlador nunca toca un repositorio.** Siempre pasa por el servicio. Si una
   regla viviera en el controlador, otra pantalla podría saltársela.
3. **El servicio no conoce el HTML ni las clases web.** Por eso el controlador convierte
   `ConciliacionForm` (web) en `ConteoArticulo` (dto) antes de llamarlo.
4. **Excepción intencional: `FuenteInsumos`.** La interfaz está en `negocio/servicio/` y
   la implementa `persistencia/repositorio/FuenteInsumosPersistencia`. Así el servicio de
   alertas no depende de la base de datos, y en las pruebas se le pasa una fuente en
   memoria (`DatosAlertasPrueba.FuenteEnMemoria`). Esto se llama **inversión de
   dependencias**: en Python sería pasarle al servicio cualquier objeto que tenga el
   método `obtener_insumos()`.

Las entidades (`persistencia/entidad/`) sí suben hasta el controlador y la plantilla: el
servicio devuelve, por ejemplo, una `ConciliacionDiaria` y Thymeleaf lee sus getters.

---

## 5. El recorrido de una petición, archivo por archivo

Ejemplo: el Administrador llena el formulario de conciliación y pulsa **Guardar**
(CU-01-HU-01).

```
1. Navegador ── POST /conciliacion  (fecha=2026-11-03&totalVentasZ=...&lineas[0].contado=12 ...)
        │
2. Spring busca qué método tiene @PostMapping en una clase con @RequestMapping("/conciliacion")
        │      → ConciliacionController.registrar(...)
        │
3. Spring crea un ConciliacionForm y llama sus setters con cada campo del form   (data binding)
   @Valid revisa @NotNull, @PositiveOrZero...  → los errores quedan en BindingResult
        │      presentacion/formulario/ConciliacionForm.java
        │
4. registrar(): si hay errores de formato → return "conciliacion/nueva" (vuelve a pintar el form)
   si no: convierte cada fila en un ConteoArticulo y llama al servicio
        │      presentacion/controlador/ConciliacionController.java
        │
5. ConciliacionService.registrarBorrador(fecha, total, conteos)   ← @Transactional: abre transacción
   - valida la fecha (no futura, no duplicada, posterior a la última confirmada)
   - por cada artículo: lo busca, calcula existencia anterior, salida y diferencia
   - guarda la cabecera y las líneas
        │      negocio/servicio/ConciliacionService.java
        │
6. Repositorios: conciliacionRepository.save(...), detalleRepository.save(...),
   detalleTandaRepository.sumarConsumoDelDia(...)  → Hibernate genera INSERT/SELECT
        │      persistencia/repositorio/*.java   +   persistencia/entidad/*.java
        │
7. Fin del método → commit de la transacción. (Si hubo excepción → rollback: no queda nada.)
        │
8. El controlador devuelve "redirect:/conciliacion/105" → el navegador hace GET a esa URL
        │
9. GET /conciliacion/105 → ConciliacionController.resumen(...) llena el Model y devuelve
   "conciliacion/resumen" → Thymeleaf pinta templates/conciliacion/resumen.html
```

El panel de alertas (CU-02) es más corto:

```
GET /inventario/alertas
  → InventarioAlertasController.mostrarAlertas
  → AlertaInsumosService.obtenerPanel
      → FuenteInsumos.obtenerInsumos()  (= FuenteInsumosPersistencia → InsumoConfigRepository)
      → filtra los códigos visibles (application.properties o valor por defecto)
      → CalculadoraAlerta.armarPanel por cada insumo  → InsumoPanel
      → ordena: críticos, alerta, normales
  → model "insumos", "hayAlertas"
  → templates/inventario/alertas.html
```

---

## 6. La "magia": quién crea los objetos

Al arrancar (`./mvnw spring-boot:run`), Spring:

1. **Escanea** `co.edu.unal.sweng1.altanto` y todos sus subpaquetes. Por eso
   `AltantoApplication` está en la raíz: lo que quede fuera de ese paquete no se encuentra.
2. **Crea un objeto** de cada clase marcada con `@Controller`, `@Service` o `@Repository`,
   y una implementación de cada interfaz que extiende `JpaRepository`. A estos objetos se
   les llama **beans**. Hay **uno solo** de cada uno (como un módulo de Python importado).
3. **Los conecta por constructor.** Si el constructor de `ConciliacionService` pide cinco
   repositorios, Spring los busca entre los beans que creó y los pasa. Esto es la
   **inyección de dependencias**.

```java
@Service
public class ConciliacionService {
    private final ConciliacionRepository conciliacionRepository;

    // Nadie llama este constructor a mano: lo llama Spring al arrancar.
    public ConciliacionService(ConciliacionRepository conciliacionRepository, ...) {
        this.conciliacionRepository = conciliacionRepository;
    }
}
```

En Python sería algo así, solo que lo hace Spring:

```python
repo = ConciliacionRepository(db)
service = ConciliacionService(repo, ...)
controller = ConciliacionController(service)
```

**Lo que NO es bean** (y por eso sí se crea con `new`): entidades, records, formularios,
excepciones y `CalculadoraAlerta`. Son objetos de datos o de cálculo, no "servicios".

**Valores de configuración.** `@Value("${altanto.alertas.dias-critico:1}")` en el
constructor de `AlertaInsumosService` lee esa clave de `application.properties`; si no
existe, usa `1`. Es el `os.getenv("X", "1")` de Python.

---

## 7. La base de datos: de `schema.sql` a un objeto Java

```
 schema.sql ──crea──► tablas H2 ◄──valida── persistencia/entidad/*.java (@Entity)
 data.sql  ──llena──►    │                          ▲
                         │                          │ devuelve objetos
                         └──── SQL generado ◄─── persistencia/repositorio/*.java
```

1. **`schema.sql`** crea las tablas con `CREATE TABLE IF NOT EXISTS`. Corre en cada
   arranque, pero no borra nada.
2. **`data.sql`** inserta los datos de ejemplo solo si la tabla está vacía
   (`WHERE NOT EXISTS`). Para volver a los datos de ejemplo: detener la app y borrar `./data/`.
3. **Entidades (`@Entity`)**: cada clase describe una tabla (`@Table`), y cada campo, una
   columna (`@Column(name = "...")`). Con `spring.jpa.hibernate.ddl-auto=validate`, Hibernate
   **no crea tablas**: solo revisa que las entidades coincidan con `schema.sql`. Si una
   entidad pide una columna que no existe, la aplicación no arranca (y la prueba
   `contextLoads` falla).
4. **Repositorios**: interfaces sin código. Spring escribe la implementación:
   - **Heredados** de `JpaRepository<Entidad, TipoDeLlave>`: `save`, `findById`, `findAll`,
     `delete`...
   - **Por nombre del método** (*consultas derivadas*):
     `findByFechaConciliacion(fecha)` → `SELECT ... WHERE fecha_conciliacion = ?`.
   - **Escritos a mano** con `@Query("...")` en **JPQL**: parece SQL, pero usa nombres de
     clases y atributos Java (`Articulo a`, `a.existenciaActual`), no de tablas y columnas.
     `SELECT new ...dto.CierrePrevio(...)` arma un record por fila.

| Tabla (`schema.sql`) | Entidad | Repositorio | Lo usa |
|---|---|---|---|
| `articulos` | `Articulo` | `InsumoRepository` | CU-01, CU-02, CU-03 |
| `proveedores` | `Proveedor` | — | (CU-02, próximo sprint) |
| `insumos_config` | `InsumoConfig` | `InsumoConfigRepository` | CU-01 (alerta), CU-02 |
| `conciliaciones_diarias` | `ConciliacionDiaria` | `ConciliacionRepository` | CU-01 |
| `detalles_conciliacion` | `DetalleConciliacion` | `DetalleConciliacionRepository` | CU-01 |
| `tandas_produccion` | `TandaProduccion` | `TandaProduccionRepository` | CU-03 |
| `detalles_tanda_insumo` | `DetalleTandaInsumo` | `DetalleTandaInsumoRepository` | CU-01 (consumo), CU-03 |
| `productos_venta_config`, `alertas_reabastecimiento` | — | — | Todavía no se usan |

**Relaciones.** `@ManyToOne` + `@JoinColumn(name = "articulo_id")` hace que
`detalle.getArticulo()` devuelva el objeto `Articulo` completo, no el número. Hibernate
hace el `JOIN` por ti.

**Guardar sin `save`.** Dentro de un método `@Transactional`, si cambias un objeto que
leíste de la base (`articulo.setExistenciaActual(...)`), Hibernate hace el `UPDATE` solo al
terminar el método (*dirty checking*). Así funciona `ConciliacionService.confirmar`.

---

## 8. Errores y transacciones

```
servicio lanza ReglaDeNegocioException ("no se puede conciliar una fecha futura")
   │
   ├── @Transactional ve una RuntimeException → ROLLBACK (no queda nada guardado)
   │
   └── el controlador la atrapa con try/catch → model.addAttribute("error", e.getMessage())
                                              → vuelve a pintar el formulario con el mensaje
```

- **Errores de formato** (campo vacío, número negativo): las anotaciones del formulario +
  `@Valid`. Nunca llegan al servicio.
- **Errores de negocio** (fecha duplicada, confirmar dos veces): `ReglaDeNegocioException`
  o una hija (`negocio/excepcion/`). Se lanzan en el servicio o en la entidad
  (`ConciliacionDiaria.confirmar()`).
- **Por qué `RuntimeException`**: no obliga a escribir `throws` en cada método y Spring
  hace rollback automático con ella. Es como una excepción normal de Python.
- **No encontrado**: `ConciliacionNoEncontradaException` se traduce a HTTP 404 en
  `ConciliacionController.resumen`.

---

## 9. Las pantallas: del `Model` al HTML

```java
@GetMapping("/inventario/alertas")
public String mostrarAlertas(Model model) {
    model.addAttribute("insumos", panel);      // en el HTML: ${insumos}
    return "inventario/alertas";               // → templates/inventario/alertas.html
}
```

- El `String` que devuelve el controlador es la ruta de la plantilla, sin `.html`.
- `"redirect:/conciliacion/5"` en vez de una plantilla manda al navegador a otra URL
  (patrón Post/Redirect/Get: recargar no reenvía el formulario).
- `RedirectAttributes.addFlashAttribute(...)` guarda un mensaje que sobrevive a **una**
  redirección (como `flash()` de Flask).
- `fragments/layout.html` es el `base.html`: las páginas toman su cabecera, menú y pie con
  `th:replace="~{fragments/layout :: navbar}"`.
- En Thymeleaf, `${x.nombre}` llama `x.getNombre()` (o `x.nombre()` si es un record).

Cada plantilla tiene al inicio un comentario con los atributos `th:` que usa.

---

## 10. Las pruebas

Están en `src/test/java`, en el **mismo paquete** que la clase que prueban. `./mvnw test`
las corre todas (y GitHub las corre en cada pull request).

| Tipo | Anotación | Qué arranca | Ejemplo |
|---|---|---|---|
| Unitaria | ninguna | Nada: solo `new` | `negocio/calculo/CalculadoraAlertasTest`, `persistencia/entidad/DetalleConciliacionTest` |
| Unitaria con dobles | ninguna | Nada; la base se reemplaza por `FuenteEnMemoria` | `negocio/servicio/ServicioAlertasTest` |
| Repositorio | `@DataJpaTest` | Solo JPA + H2 | `persistencia/repositorio/InsumoRepositoryTest` |
| Integración | `@SpringBootTest` | Toda la app + H2 en memoria | `negocio/servicio/ConciliacionServiceTest` |
| Web | `@SpringBootTest` + `@AutoConfigureMockMvc` | Toda la app + navegador simulado | `presentacion/controlador/ConciliacionControllerTest` |

- `@Transactional` en una clase de prueba deshace lo guardado al final de cada prueba.
- Las pruebas usan `src/test/resources/application.properties`: una base H2 **en memoria**,
  así que nunca tocan `./data/`.

---

## 11. Chuleta de anotaciones

| Anotación | Dónde | Qué hace |
|---|---|---|
| `@SpringBootApplication` | `AltantoApplication` | Arranca Spring y escanea los subpaquetes |
| `@Controller` | controlador | Bean que atiende URLs y devuelve plantillas |
| `@RequestMapping("/x")` | clase controlador | Prefijo común de las URLs |
| `@GetMapping` / `@PostMapping` | método controlador | Atiende GET / POST de esa URL |
| `@PathVariable` | parámetro | Toma `{id}` de la URL |
| `@ModelAttribute` + `@Valid` | parámetro | Llena el formulario y lo valida |
| `@NotNull`, `@PositiveOrZero`, `@Digits` | campo del formulario | Reglas de formato |
| `@Service` | servicio | Bean de lógica de negocio |
| `@Transactional` | método del servicio | Todo o nada; rollback con `RuntimeException` |
| `@Transactional(readOnly = true)` | método del servicio | Transacción liviana de solo lectura |
| `@Value("${clave:defecto}")` | parámetro del constructor | Lee `application.properties` |
| `@Repository` | clase de persistencia | Bean de acceso a datos |
| `@Query("...")` + `@Param` | método del repositorio | Consulta JPQL escrita a mano |
| `@Entity` + `@Table` | entidad | Clase = tabla |
| `@Id` + `@GeneratedValue` + `@SequenceGenerator` | campo id | Llave primaria tomada de una secuencia |
| `@Column` | campo de la entidad | Campo = columna (nombre, tamaño, nulos) |
| `@ManyToOne` + `@JoinColumn` | campo de la entidad | Llave foránea como objeto |
| `@Enumerated(EnumType.STRING)` | campo enum | Guarda el nombre del valor ("BORRADOR") |
| `@Override` | método | Implementa un método de una interfaz o clase padre |
| `@Test` | prueba | Marca un método como prueba |
| `@SpringBootTest` / `@DataJpaTest` | clase de prueba | Arranca toda la app / solo la base |
| `@Autowired` | campo de prueba | Spring inyecta el bean en el campo |

---

## 12. ¿Dónde pongo...?

| Quiero... | Archivo(s) |
|---|---|
| Una tabla nueva | `schema.sql` + una `@Entity` en `persistencia/entidad/` + su repositorio en `persistencia/repositorio/` |
| Una columna nueva | `schema.sql` **y** el campo con `@Column` en la entidad |
| Una consulta | Un método en el repositorio de esa tabla |
| Una regla de negocio | El `@Service` del caso de uso (`negocio/servicio/`), o un método de la entidad si es una regla de ese objeto |
| Un cálculo complicado | Una clase en `negocio/calculo/` (sin Spring, fácil de probar) |
| Pasar datos entre capas sin usar una entidad | Un `record` en `negocio/dto/` |
| Un error con nombre propio | Una clase que extienda `ReglaDeNegocioException` en `negocio/excepcion/` |
| Una pantalla nueva | Un método en un controlador (`presentacion/controlador/`) + la plantilla en `templates/<módulo>/` |
| Un formulario | Una clase en `presentacion/formulario/` con sus validaciones |
| Un valor configurable | `application.properties` + `@Value` en el constructor |
| Datos de ejemplo | `data.sql`, siempre con `WHERE NOT EXISTS` |
| La prueba de algo | `src/test/java/` en el mismo paquete que la clase |

La receta completa para construir una historia nueva está en la
[sección 12 de Al-Tanto desde cero](guia-desde-cero.md#12-receta-cómo-construir-la-siguiente-historia).
