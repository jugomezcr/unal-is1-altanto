# Al-Tanto desde cero: cómo se construyó hasta el primer caso de uso

Esta guía es para alguien que **nunca ha programado en Java ni ha usado Spring Boot**.
Cuenta todo el camino: el problema de la panadería, las herramientas, cómo arranca la
aplicación, cómo están organizadas sus capas y, al final, cómo se construyó el **CU-01
Conciliar el día**, siguiendo un caso real de principio a fin.

> **Cómo leerla.** Siga las secciones en orden: cada una se apoya en la anterior. Tenga el
> proyecto abierto en VS Code o IntelliJ para ir viendo los archivos que se nombran. Las
> palabras nuevas se explican la primera vez que aparecen y también están en el
> [glosario](#13-glosario).

## Contenido

1. [El problema: de una entrevista a un caso de uso](#1-el-problema-de-una-entrevista-a-un-caso-de-uso)
2. [Las herramientas y para qué sirve cada una](#2-las-herramientas-y-para-qué-sirve-cada-una)
3. [Anatomía del proyecto: qué hay en cada carpeta](#3-anatomía-del-proyecto-qué-hay-en-cada-carpeta)
4. [Qué pasa cuando se ejecuta `./mvnw spring-boot:run`](#4-qué-pasa-cuando-se-ejecuta-mvnw-spring-bootrun)
5. [La arquitectura por capas](#5-la-arquitectura-por-capas)
6. [Capa de persistencia: la base de datos vista desde Java](#6-capa-de-persistencia-la-base-de-datos-vista-desde-java)
7. [Capa de negocio: las reglas](#7-capa-de-negocio-las-reglas)
8. [Capa de presentación: lo que ve el usuario](#8-capa-de-presentación-lo-que-ve-el-usuario)
9. [El CU-01 de punta a punta, con un ejemplo real](#9-el-cu-01-de-punta-a-punta-con-un-ejemplo-real)
10. [Las pruebas automáticas](#10-las-pruebas-automáticas)
11. [Cómo se trabajó con Git y GitHub](#11-cómo-se-trabajó-con-git-y-github)
12. [Receta: cómo construir la siguiente historia](#12-receta-cómo-construir-la-siguiente-historia)
13. [Glosario](#13-glosario)

---

## 1. El problema: de una entrevista a un caso de uso

Antes de escribir una línea de código, el equipo pasó por tres entregas de pura
documentación. Cada una dejó una decisión que el código respeta.

```
 Hito 1                 Hito 2                   Hito 3               Hito 4
 Definición del         Especificación de        Diseño               Primer incremento
 producto               requisitos               arquitectónico       funcional (código)
 ─────────────          ─────────────────        ────────────         ─────────────────
 Entrevista al dueño →  6 casos de uso       →   Capas, clases,   →   Proyecto Spring Boot
 Visión y alcance       12 historias             modelo de datos      CU-01 terminado
                        Backlog en GitHub
```

### 1.1 Hito 1: qué le duele al dueño
En la entrevista (19/09/2026), Leonardo Betancur, dueño y maestro panadero, contó cómo
trabaja: controla los insumos *"al cálculo visual"*, cuenta producto por producto a mano y
cierra el mes en un cuaderno. Su frase clave:

> *"Lo más complicado es contar producto por producto manualmente y conciliar la materia
> prima que se gasta versus la producción."*

De ahí salió la **visión**: una aplicación web que compare lo vendido y lo producido con lo
que debería quedar, para ver las diferencias **el mismo día**. También salió la lista de lo
que queda **fuera** de la primera versión: recetas, mermas, medios de pago, varias
sucursales…

### 1.2 Hito 2: casos de uso e historias de usuario
- Un **caso de uso** describe una meta completa del usuario, paso a paso. El CU-01 es
  *"Conciliar el día"*: al cierre, el Administrador digita lo vendido según el **reporte Z**
  (el cierre de la caja registradora) y lo que contó. El sistema calcula qué debería haber y
  muestra las diferencias.
- Cada caso de uso se partió en **2 historias de usuario**: pedazos pequeños que se pueden
  entregar por separado. Siguen el formato *"Como … quiero … para …"* y tienen **criterios
  de aceptación**, que son las condiciones para decir que la historia está terminada.

| Historia | Lo que pide |
|---|---|
| **CU-01-HU-01** | Digitar lo vendido y lo contado del día. No permitir dos conciliaciones de la misma fecha. Si algo falla, no guardar nada. |
| **CU-01-HU-02** | Calcular lo esperado y la diferencia, señalar lo que no cuadra, confirmar (lo contado pasa a ser la existencia) y mostrar la alerta de reabastecimiento. |

La fórmula que define todo el CU-01 sale de la Entrega 2:

```
existencia esperada = existencia anterior + lo recibido − lo vendido o consumido en tandas
diferencia          = lo contado − la existencia esperada
```

### 1.3 Hito 3: el diseño
Propuso una **arquitectura por capas** (sección 5), un modelo de datos normalizado y
excepciones de negocio con nombre propio. El código la adaptó para mantenerla simple:
usa 3 capas en vez de 4 y H2 en vez de PostgreSQL. Las diferencias están en
[hito-4/plan-y-trazabilidad.md](hito-4/plan-y-trazabilidad.md#3-trazabilidad-entre-los-hitos-y-el-código).

### 1.4 Hito 4: el plan de construcción
El equipo se comprometió a 4 fases: infraestructura (Julián), persistencia (Pedro y
Leonardo), historias (Juan Pablo, Pedro y Nikko) y verificación. Esta guía sigue ese
mismo orden.

---

## 2. Las herramientas y para qué sirve cada una

| Herramienta | Qué es | Analogía | Dónde se ve en el proyecto |
|---|---|---|---|
| **Java 21** | Lenguaje de programación. El código (`.java`) se *compila* a un formato que ejecuta la **JVM** (máquina virtual de Java) | El idioma en que se escriben las instrucciones | `src/main/java/` |
| **JDK** | Kit que trae el compilador (`javac`) y la JVM. Hay que instalarlo | El diccionario y el traductor de ese idioma | `java -version` |
| **Maven** | Herramienta de *construcción*: descarga librerías, compila, corre las pruebas y empaqueta | El jefe de obra que consigue los materiales y sigue la receta | `pom.xml` |
| **Maven Wrapper** (`mvnw`) | Script que descarga la versión exacta de Maven, así nadie tiene que instalarlo | Un jefe de obra que trae su propia caja de herramientas | `mvnw`, `mvnw.cmd`, `.mvn/` |
| **Spring Boot 4** | *Framework*: un esqueleto de aplicación que ya resuelve el servidor web, la conexión a la base de datos, las transacciones, etc. | Una casa prefabricada: usted solo pone los muebles | `AltantoApplication.java` |
| **Tomcat embebido** | Servidor web que viene **dentro** de la aplicación. Escucha en el puerto 8080 | El mostrador que atiende a los clientes | Arranca solo |
| **H2** | Base de datos relacional que vive en un **archivo** dentro del proyecto. No hay que instalar nada | Un cuaderno contable guardado en un cajón del proyecto | `./data/altanto.mv.db` |
| **JPA / Hibernate** | Traducen entre **objetos Java** y **filas de tablas**, para no escribir SQL a mano | Un intérprete entre el código y la base de datos | `@Entity`, repositorios |
| **Spring Data JPA** | Crea solos los repositorios (consultas) a partir de una interfaz | Un asistente que adivina la consulta por su nombre | `persistencia/` |
| **Thymeleaf** | Motor de *plantillas*: HTML con huecos que el servidor llena con datos | Un formulario impreso al que se le escriben los datos | `templates/*.html` |
| **Tailwind CSS** | Estilos visuales con clases como `bg-white` o `text-lg`. Se carga por internet (CDN) | La pintura y el acabado | `class="..."` en el HTML |
| **JUnit + AssertJ** | Librerías para escribir pruebas automáticas | Un inspector que revisa la obra cada vez | `src/test/java/` |
| **Git + GitHub** | Control de versiones y trabajo en equipo (issues, tablero, PR) | Un historial de cada cambio, con autor y fecha | `.git/`, github.com |

### 2.1 El `pom.xml`: la lista de materiales
`pom.xml` le dice a Maven qué proyecto es y qué necesita. Las partes importantes:

```xml
<parent>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>4.1.2</version>              <!-- versión de Spring Boot -->
</parent>
<properties>
    <java.version>21</java.version>       <!-- versión de Java -->
</properties>
<dependencies>
    spring-boot-starter-webmvc            <!-- web: controladores y Tomcat -->
    spring-boot-starter-thymeleaf         <!-- plantillas HTML -->
    spring-boot-starter-data-jpa          <!-- JPA/Hibernate y repositorios -->
    spring-boot-starter-validation        <!-- @NotNull, @PositiveOrZero... -->
    h2                                    <!-- el motor de la base de datos -->
    spring-boot-h2console                 <!-- la consola web /h2-console -->
    ...-test                              <!-- lo mismo, para las pruebas -->
</dependencies>
```

Un **starter** es un paquete que trae varias librerías que se usan juntas. Con un solo
`spring-boot-starter-webmvc` llegan Spring MVC, Tomcat y el manejo de JSON. Maven descarga
todo en `~/.m2/repository` la primera vez; por eso el primer arranque tarda.

---

## 3. Anatomía del proyecto: qué hay en cada carpeta

```
unal-is1-altanto/
├── pom.xml                          ← lista de materiales (sección 2.1)
├── mvnw, mvnw.cmd, .mvn/            ← Maven Wrapper (Linux/macOS y Windows)
├── src/
│   ├── main/
│   │   ├── java/co/edu/unal/sweng1/altanto/
│   │   │   ├── AltantoApplication.java   ← punto de arranque (método main)
│   │   │   ├── presentacion/             ← CAPA 1: controladores web y formularios
│   │   │   ├── negocio/                  ← CAPA 2: entidades, servicios y reglas
│   │   │   └── persistencia/             ← CAPA 3: repositorios (consultas)
│   │   └── resources/
│   │       ├── application.properties    ← configuración (base de datos, consola H2)
│   │       ├── schema.sql                ← crea las tablas
│   │       ├── data.sql                  ← datos de ejemplo
│   │       └── templates/                ← vistas Thymeleaf (.html)
│   └── test/
│       ├── java/...                      ← pruebas automáticas
│       └── resources/application.properties ← configuración SOLO para pruebas
├── data/                            ← se crea al arrancar; es la base H2 (no va a git)
├── docs/                            ← documentación por hito y estas guías
└── .github/                         ← plantillas de issues/PR y la integración continua (CI)
```

**¿Por qué `co/edu/unal/sweng1/altanto`?** En Java, los **paquetes** se nombran con el
dominio al revés (`unal.edu.co` → `co.edu.unal`) para que no choquen con los de otros
proyectos. Cada punto del nombre es una carpeta.

### 3.1 `application.properties`: la configuración

```properties
spring.datasource.url=jdbc:h2:file:./data/altanto;DB_CLOSE_ON_EXIT=FALSE  # base de datos en archivo
spring.datasource.username=sa
spring.sql.init.mode=always            # ejecutar schema.sql y data.sql en CADA arranque
spring.jpa.hibernate.ddl-auto=validate # Hibernate NO crea tablas, solo revisa que coincidan
spring.h2.console.enabled=true         # consola web en /h2-console
```

En las pruebas, `src/test/resources/application.properties` cambia la URL a
`jdbc:h2:mem:...`: una base **en memoria** que se borra al terminar. Así las pruebas nunca
tocan sus datos reales.

### 3.2 `schema.sql` y `data.sql`
- **`schema.sql`** crea 9 tablas con `CREATE TABLE IF NOT EXISTS`, es decir, solo si no
  existen. Para el CU-01 importan `articulos`, `insumos_config`, `conciliaciones_diarias`,
  `detalles_conciliacion` y `detalles_tanda_insumo`.
- **`data.sql`** carga los datos de ejemplo: 5 insumos, 4 productos, 4 proveedores, una
  conciliación **confirmada** del 01/11/2026 y un **borrador** del 02/11/2026. Cada `INSERT`
  termina en `WHERE NOT EXISTS (SELECT 1 FROM tabla)`.

> **Un error real que se corrigió.** Al principio `data.sql` tenía `INSERT` simples. Como
> la base es un archivo y el script corre en cada arranque, **la segunda vez** la
> aplicación fallaba con *"Unique index or primary key violation"*. La condición
> `WHERE NOT EXISTS` hace que cada bloque solo inserte si su tabla está vacía. Es el mismo
> patrón del repositorio de referencia del curso.

---

## 4. Qué pasa cuando se ejecuta `./mvnw spring-boot:run`

```
 1. mvnw              → si no está, descarga Maven 3.10 (una sola vez)
 2. Maven             → lee pom.xml y descarga las dependencias que falten
 3. Maven             → compila src/main/java (.java → .class)
 4. JVM               → ejecuta AltantoApplication.main(...)
 5. Spring Boot       → "autoconfiguración": como ve H2, JPA y Thymeleaf en el proyecto,
                        configura cada uno solo
 6. Spring Boot       → abre la base ./data/altanto y ejecuta schema.sql y data.sql
 7. Hibernate         → valida que cada @Entity coincida con su tabla (ddl-auto=validate).
                        Si una columna no coincide, la aplicación NO arranca
 8. Spring            → escanea los paquetes, crea los @Controller, @Service y repositorios,
                        y los conecta entre sí (inyección de dependencias, sección 7.2)
 9. Tomcat            → empieza a escuchar en http://localhost:8080
10. Consola           → "Started AltantoApplication in 4.4 seconds"
```

Todo eso lo dispara esta clase:

```java
@SpringBootApplication                 // "esta es una aplicación Spring Boot: autoconfigúrate
public class AltantoApplication {      //  y busca componentes en este paquete y sus subpaquetes"
    public static void main(String[] args) {
        SpringApplication.run(AltantoApplication.class, args);   // arranca todo
    }
}
```

**¿Qué es una anotación?** Las palabras que empiezan con `@` (`@SpringBootApplication`,
`@Entity`, `@Service`…) son **anotaciones**: etiquetas que no hacen nada por sí solas,
pero que Spring y Hibernate leen al arrancar para saber qué hacer con cada clase. Gran
parte de Spring funciona así: usted pone la etiqueta y el framework se encarga del resto.

---

## 5. La arquitectura por capas

El código está separado en **tres capas**. Cada una solo habla con la de abajo:

```
            Navegador (el Administrador)
                 │  HTTP: GET / POST
                 ▼
 ┌──────────────────────────────────────────────┐
 │ PRESENTACIÓN   presentacion/                 │  Recibe la petición, valida el formato,
 │   ConciliacionController, ConciliacionForm   │  llama al negocio y elige qué vista mostrar.
 │   templates/conciliacion/*.html              │  NO calcula nada ni toca la base de datos.
 └───────────────────────┬──────────────────────┘
                         │ llama a métodos Java
                         ▼
 ┌──────────────────────────────────────────────┐
 │ NEGOCIO        negocio/                      │  Las reglas: la fórmula, "no dos por
 │   ConciliacionService (@Service)             │  fecha", confirmar, alertas.
 │   ConciliacionDiaria, DetalleConciliacion... │  Abre y cierra las transacciones.
 └───────────────────────┬──────────────────────┘
                         │ llama a repositorios
                         ▼
 ┌──────────────────────────────────────────────┐
 │ PERSISTENCIA   persistencia/                 │  Guarda y consulta. Interfaces que
 │   ConciliacionRepository, ...                │  Spring Data JPA convierte en SQL.
 └───────────────────────┬──────────────────────┘
                         ▼
                Base de datos H2 (./data/altanto.mv.db)
```

**¿Por qué separar?** Si mañana cambia la pantalla, el cálculo no se toca. Si se cambia H2
por PostgreSQL, las reglas tampoco. Además, cada capa se puede probar por separado: las
reglas del negocio se prueban sin abrir un navegador (sección 10).

> **Regla de oro:** un controlador **nunca** usa un repositorio directamente. Siempre pasa
> por un servicio.

La sección 6 empieza por abajo, por la persistencia: es lo que se construyó primero (Fase 2).

---

## 6. Capa de persistencia: la base de datos vista desde Java

### 6.1 Entidades: una clase = una tabla
Una **entidad** es una clase Java cuyos objetos son filas de una tabla. Así quedó
`ConciliacionDiaria` (resumida):

```java
@Entity                                        // "esta clase es una tabla"
@Table(name = "conciliaciones_diarias")        // ...y esta es la tabla
public class ConciliacionDiaria {

    @Id                                        // llave primaria
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "conciliacion_seq")
    @SequenceGenerator(name = "conciliacion_seq", sequenceName = "conciliacion_seq", allocationSize = 1)
    private Long id;                           // el número lo pone la base de datos

    @Column(name = "fecha_conciliacion", nullable = false, unique = true)
    private LocalDate fechaConciliacion;       // una sola conciliación por fecha

    @Column(name = "total_venta_reporte_z", precision = 12, scale = 2)
    private BigDecimal totalVentaReporteZ;     // dinero → BigDecimal, nunca double

    @Enumerated(EnumType.STRING)               // guarda "BORRADOR" o "CONFIRMADA" como texto
    private EstadoConciliacion estado = EstadoConciliacion.BORRADOR;

    public ConciliacionDiaria() { }            // JPA exige un constructor vacío
    // getters y setters...
}
```

| Concepto | Por qué |
|---|---|
| `@Column(name = "...")` | Debe coincidir **letra por letra** con `schema.sql`. Si no, el paso 7 del arranque falla. |
| `BigDecimal` | `double` tiene errores de redondeo (0.1 + 0.2 ≠ 0.3). Para kilos y pesos se necesita exactitud. Por eso se suma con `.add()` y se resta con `.subtract()`, no con `+` y `−`. |
| Secuencias que empiezan en 100 | Los datos de ejemplo usan los ids 1, 2, 3… y los registros nuevos empiezan en 100, así nunca chocan. |
| `private` + getters/setters | Los campos se protegen y se leen o cambian con métodos. JPA y Thymeleaf usan esos métodos. |

### 6.2 Relaciones: `@ManyToOne`
Una conciliación tiene **muchas** líneas, una por artículo, y cada línea pertenece a **una**
conciliación:

```
 conciliaciones_diarias (1) ────< detalles_conciliacion (muchas) >──── (1) articulos
```

```java
public class DetalleConciliacion {
    @ManyToOne                                    // muchas líneas → una conciliación
    @JoinColumn(name = "conciliacion_id")         // la columna de la llave foránea
    private ConciliacionDiaria conciliacion;      // en Java se trabaja con el OBJETO

    @ManyToOne
    @JoinColumn(name = "articulo_id")
    private Articulo articulo;
    ...
}
```

La relación es **unidireccional**: la línea conoce a su conciliación, pero la conciliación
no tiene una lista de líneas. Así es más simple, y las líneas se piden a un repositorio
cuando se necesitan.

### 6.3 Repositorios: consultas sin escribir SQL
Un **repositorio** es una **interfaz**: una lista de métodos sin código. Spring Data JPA
escribe la implementación al arrancar.

```java
public interface ConciliacionRepository extends JpaRepository<ConciliacionDiaria, Long> {
    //                                       entidad ─┘         tipo del id ─┘
    // Se heredan solos: save, findById, findAll, delete, count...

    Optional<ConciliacionDiaria> findByFechaConciliacion(LocalDate fecha);
    //  Spring "lee" el nombre:  find By FechaConciliacion
    //  y genera:  SELECT * FROM conciliaciones_diarias WHERE fecha_conciliacion = ?

    Optional<ConciliacionDiaria> findFirstByEstadoOrderByFechaConciliacionDesc(EstadoConciliacion e);
    //  "la primera, filtrando por estado, ordenadas por fecha de mayor a menor"
}
```

Esto se llama **consulta derivada**: la consulta sale del **nombre** del método. Cuando
el nombre quedaría demasiado largo, se escribe la consulta a mano con `@Query` en
**JPQL**. JPQL se parece a SQL, pero usa nombres de **clases y atributos Java**, no de
tablas:

```java
@Query("""
        SELECT COALESCE(SUM(d.cantidadConsumida), 0)
        FROM DetalleTandaInsumo d
        WHERE d.insumo.id = :insumoId AND d.tanda.fecha = :fecha
        """)
BigDecimal sumarConsumoDelDia(@Param("insumoId") Long insumoId, @Param("fecha") LocalDate fecha);
```

**`Optional<T>`** es una "caja" que puede traer un valor o venir vacía. Sirve para no
devolver `null`, que es la causa del error más común en Java (`NullPointerException`).

Repositorios que usa el CU-01:

| Repositorio | Para qué |
|---|---|
| `ConciliacionRepository` | Buscar por fecha y la última conciliación confirmada |
| `DetalleConciliacionRepository` | Las líneas de una conciliación y el último conteo de un artículo |
| `DetalleTandaInsumoRepository` | Cuánto insumo se gastó en las tandas del día |
| `InsumoRepository` | Los artículos que se concilian |
| `InsumoConfigRepository` | Los insumos en su umbral o por debajo (la alerta) |

---

## 7. Capa de negocio: las reglas

### 7.1 El servicio
`ConciliacionService` tiene la lógica del CU-01. Está marcado con `@Service`:

```java
@Service
public class ConciliacionService {
    private final ConciliacionRepository conciliacionRepository;
    // ...otros 4 repositorios

    public ConciliacionService(ConciliacionRepository conciliacionRepository, /* ... */) {
        this.conciliacionRepository = conciliacionRepository;
    }
}
```

### 7.2 Inyección de dependencias: nadie escribe `new`
Fíjese en que **nadie** escribe `new ConciliacionService(...)`. Al arrancar (paso 8 de la
sección 4), Spring:

1. encuentra las clases marcadas (`@Service`, `@Controller`, los repositorios),
2. crea **un** objeto de cada una (se les llama *beans*),
3. ve que el constructor del servicio pide 5 repositorios y se los pasa,
4. ve que el constructor del controlador pide el servicio y se lo pasa.

```
 Spring crea:  [Repositorios] ──se inyectan en──▶ [ConciliacionService] ──se inyecta en──▶ [ConciliacionController]
```

La ventaja es que en las pruebas se puede pedir el mismo servicio ya armado, con
`@Autowired`.

### 7.3 Transacciones: todo o nada
```java
@Transactional
public ConciliacionDiaria registrarBorrador(LocalDate fecha, BigDecimal totalVentasZ, List<ConteoArticulo> conteos) {
    validarFecha(fecha);                                   // ¿hay errores? → excepción
    ConciliacionDiaria conciliacion = conciliacionRepository.save(nueva);   // 1. cabecera
    for (ConteoArticulo conteo : conteos) {
        detalleRepository.save(crearDetalle(conciliacion, conteo));         // 2. una línea por artículo
    }
    return conciliacion;
}
```

Una **transacción** agrupa varias operaciones en la base de datos para que pasen **todas**
o **ninguna**. Si en la línea 5 de 7 se lanza una excepción, Spring hace **rollback**:
deshace la cabecera y las 4 líneas ya guardadas. Así se cumple el criterio *"si la
operación falla, no se guarda nada"*, y hay una prueba que lo demuestra (sección 10).

`@Transactional(readOnly = true)` se usa en los métodos que solo consultan. Es más
liviano y deja claro que no modifican nada.

### 7.4 Excepciones de negocio
Cuando una regla no se cumple, el servicio **lanza** una excepción con nombre propio:

```
 RuntimeException                       (de Java)
   └── ReglaDeNegocioException          "algo viola una regla"  (mensaje para el usuario)
         ├── ConciliacionDuplicadaException    ya existe esa fecha; guarda el id existente
         └── ConciliacionNoEncontradaException el id no existe
```

Extienden `RuntimeException` por dos razones: no obligan a escribir `throws` en cada
método y hacen que `@Transactional` deshaga todo automáticamente. El controlador las
**atrapa** (`try/catch`) y muestra el mensaje en pantalla.

### 7.5 Reglas que viven en las entidades
Algunas reglas son tan propias de un objeto que viven **dentro** de él:

```java
// DetalleConciliacion: la fórmula del CU-01
public void calcular() {
    this.existenciaEsperada = existenciaAnterior.add(cantidadRecibida).subtract(cantidadSalida);
    this.diferencia = cantidadContada.subtract(existenciaEsperada);
}

public boolean tieneDiferencia() {
    return diferencia.compareTo(BigDecimal.ZERO) != 0;   // compareTo, no equals: 0.000 vs 0
}

// ConciliacionDiaria: solo se confirma una vez
public void confirmar() {
    if (!esBorrador()) throw new ReglaDeNegocioException("...ya está confirmada.");
    this.estado = EstadoConciliacion.CONFIRMADA;
}
```

### 7.6 Las decisiones de negocio del CU-01
| Decisión | Por qué |
|---|---|
| Solo se concilian **insumos** y **productos de reventa**, no el pan | La Entrega 2: *"la conciliación cubre insumos y productos de reventa"*. El pan no vendido no se registra. |
| **Existencia anterior** = lo contado en la última conciliación confirmada | La Entrega 2: *"lo contado queda como existencia de partida del día siguiente"*. |
| **Salida** = vendido (reventa) + consumido en tandas del día (insumos) | La fórmula del CU-01. Los insumos no se venden por caja; los productos de reventa no van a tandas. |
| **Recibido** = 0 por ahora | Las recepciones son del CU-04 (Sprint 2). La columna ya existe. |
| Se guarda un **borrador** y luego se confirma | El plan de la Entrega 4 (paso 8) y la Entrega 3 (*"persistencia de borradores"*). |
| No se concilian fechas futuras ni días anteriores a uno ya confirmado | Garantía mínima: *"los días anteriores no cambian"*. |

### 7.7 `record`: cajas de datos
`ConteoArticulo` e `InsumoEnAlerta` son **records**: clases que solo cargan datos y que
Java completa solas (constructor, getters, etc.):

```java
public record ConteoArticulo(Long articuloId, BigDecimal vendido, BigDecimal contado) { }
// uso: conteo.articuloId(), conteo.vendido(), conteo.contado()
```

---

## 8. Capa de presentación: lo que ve el usuario

### 8.1 El controlador: una puerta por URL
```java
@Controller
@RequestMapping("/conciliacion")          // todas las URLs empiezan así
public class ConciliacionController {

    @GetMapping                           // GET /conciliacion → mostrar el formulario
    public String nueva(Model model) {
        model.addAttribute("form", form); // datos que la vista podrá usar como ${form}
        return "conciliacion/nueva";      // → templates/conciliacion/nueva.html
    }

    @PostMapping                          // POST /conciliacion → guardar
    public String registrar(@Valid @ModelAttribute("form") ConciliacionForm form,
                            BindingResult errores, Model model) { ... }

    @GetMapping("/{id}")                  // GET /conciliacion/100 → ver diferencias
    public String resumen(@PathVariable Long id, Model model) { ... }
}
```

| Pieza | Qué es |
|---|---|
| **GET** | Pedir una página (escribir una URL, hacer clic en un enlace). No cambia datos. |
| **POST** | Enviar un formulario. Sí cambia datos. |
| `Model` | La "bolsa" de datos que va del controlador a la vista. |
| `return "carpeta/vista"` | El nombre de la plantilla que se va a mostrar. |
| `return "redirect:/..."` | Le dice al navegador que vaya a otra URL. |
| `@PathVariable` | Toma un pedazo de la URL (el `100` de `/conciliacion/100`). |

### 8.2 Del formulario HTML al objeto Java (*data binding*)
`ConciliacionForm` es la forma en Java del formulario. Spring lo llena solo: cada
`<input name="...">` va al setter que tiene el mismo nombre.

```
 HTML enviado por el navegador                Objeto Java que arma Spring
 ─────────────────────────────                ───────────────────────────
 fecha=2026-11-03                       →     form.setFecha(LocalDate 2026-11-03)
 totalVentasZ=250000                    →     form.setTotalVentasZ(250000)
 lineas[0].articuloId=1                 →     form.getLineas().get(0).setArticuloId(1)
 lineas[0].contado=14                   →     form.getLineas().get(0).setContado(14)
```

### 8.3 Dos clases de validación
| | Validación de **formato** | Validación de **negocio** |
|---|---|---|
| Dónde | Anotaciones en `ConciliacionForm` | `ConciliacionService` |
| Ejemplos | `@NotNull` "Digite lo contado", `@PositiveOrZero` | Fecha duplicada, fecha futura, día ya cerrado |
| Cómo se activa | `@Valid` en el controlador; los errores quedan en `BindingResult` | Excepción → `catch` en el controlador |
| Se muestra | Junto al campo (`th:errors`) | Recuadro rojo o ámbar arriba |

### 8.4 Post/Redirect/Get y los mensajes "flash"
Después de guardar, el controlador **no** muestra la página directamente: redirige.

```
 POST /conciliacion  ──guarda──▶  "redirect:/conciliacion/100"  ──▶  GET /conciliacion/100
```

Si el usuario recarga la página, el navegador repite el **GET** (inofensivo) y no el POST,
que crearía otra conciliación. Para pasar un mensaje a través de la redirección
("Conciliación confirmada…") se usa `RedirectAttributes.addFlashAttribute`: el dato
sobrevive **a una** redirección y luego desaparece.

### 8.5 Thymeleaf en 6 atributos
```html
<form th:action="@{/conciliacion}" th:object="${form}" method="post">   <!-- el formulario trabaja sobre ${form} -->
  <input type="date" th:field="*{fecha}">                               <!-- enlaza con form.fecha -->
  <tr th:each="linea, st : *{lineas}">                                 <!-- repite por cada línea -->
     <td th:text="${linea.nombre}">Harina</td>                          <!-- escribe el valor -->
     <input th:field="*{lineas[__${st.index}__].contado}">              <!-- lineas[0].contado, lineas[1]... -->
  </tr>
  <div th:if="${error}" th:text="${error}"></div>                      <!-- solo si hay error -->
</form>
```
El texto dentro de las etiquetas ("Harina") es un ejemplo que solo se ve si se abre el
HTML sin servidor. Thymeleaf lo reemplaza por el dato real.

---

## 9. El CU-01 de punta a punta, con un ejemplo real

Este es un caso real con los datos de ejemplo. Puede repetirlo con
`./mvnw spring-boot:run`.

**Situación:** el 01/11/2026 quedó **confirmado** con *15 kg de harina* contados. Hoy,
03/11/2026, el Administrador cuenta **14 kg**. No hubo tandas ni recepciones.

### Paso A. Abrir el formulario (HU-01)
```
 Navegador ── GET /conciliacion ──▶ ConciliacionController.nueva()
                                       ├─ service.articulosAConciliar()
                                       │     └─ InsumoRepository: activos de tipo INSUMO o PRODUCTO_REVENTA
                                       │        (7 artículos; el pan queda fuera)
                                       ├─ service.buscarPorFecha(hoy)  → ¿ya hay una hoy? (extensión 1a)
                                       └─ return "conciliacion/nueva"  → formulario con 7 filas
```

### Paso B. Guardar el borrador (HU-01)
```
 Navegador ── POST /conciliacion (fecha, totalZ, lineas[...]) ──▶ ConciliacionController.registrar()
   ├─ @Valid revisa el formato ──── ¿error? → vuelve a pintar el formulario con los mensajes
   └─ service.registrarBorrador(...)            ┐
        ├─ validarFecha: no futura, no repetida,│ UNA transacción:
        │   posterior al 01/11 (última confirm.)│ si algo falla aquí,
        ├─ save(cabecera)  → id 100             │ se deshace todo
        └─ por cada artículo: crearDetalle()    ┘
   └─ "redirect:/conciliacion/100"
```

El cálculo de **la harina** dentro de `crearDetalle()`:

| Dato | De dónde sale | Valor |
|---|---|---|
| Existencia anterior | `buscarUltimoConteo`: lo contado el 01/11 (confirmada) | **15** |
| + Recibido | CU-04 todavía no existe | **0** |
| − Vendido | Es un insumo: no se vende por caja | 0 |
| − Consumido en tandas | `sumarConsumoDelDia(harina, 03/11)` | 0 |
| **= Esperado** | `calcular()`: 15 + 0 − 0 | **15** |
| Contado | Lo digitó el Administrador | **14** |
| **Diferencia** | 14 − 15 | **−1** → falta 1 kg, la fila sale en rojo |

En la base queda:
```
 conciliaciones_diarias:  id=100 | fecha=2026-11-03 | total=250000 | estado=BORRADOR
 detalles_conciliacion :  conciliacion=100 | articulo=Harina | anterior=15 | recibida=0 | salida=0 | esperada=15 | contada=14 | diferencia=-1
                          ...una fila por cada uno de los 7 artículos
 articulos (harina)    :  existencia_actual = 15   ← todavía NO cambia
```

### Paso C. Ver las diferencias (HU-02)
`GET /conciliacion/100` → `resumen()` carga la cabecera y sus líneas, cuenta cuántas
`tieneDiferencia()` y pinta `resumen.html`: la tabla con la fila de la harina en rojo y
los botones **Confirmar** y **Descartar**.

### Paso D. Confirmar (HU-02)
```
 POST /conciliacion/100/confirmar ──▶ service.confirmar(100)        (una transacción)
   ├─ conciliacion.confirmar()                 → estado = CONFIRMADA
   ├─ por cada línea: articulo.setExistenciaActual(contado)   → harina = 14
   │     (sin save(): Hibernate detecta el cambio solo, por "dirty checking")
   └─ insumoConfigRepository.buscarInsumosEnAlerta()
         → harina: 14 ≤ umbral 25  →  ALERTA
 ◀── redirect + flash: "Conciliación confirmada" + lista de alertas
```

El resultado en pantalla: estado **CONFIRMADA** y el recuadro **🚨 Alerta de
reabastecimiento: Harina de Trigo Especial, 14 (umbral 25) KILOGRAMO**, con un enlace al
CU-02. Mañana, la existencia anterior de la harina será **14**.

### Paso E. Los caminos alternativos
| Si… | El sistema… | Dónde |
|---|---|---|
| Elige el 02/11 (ya tiene un borrador) | No crea otra conciliación y muestra "Ver la existente" | `ConciliacionDuplicadaException` (extensión 1a) |
| Deja vacío un "Contado" | Muestra "Digite lo contado." junto al campo | `@NotNull` en `ConciliacionForm` |
| Pulsa **Cancelar** en el formulario | No envía nada, así que no se guarda nada | Es solo un enlace a `/` |
| Pulsa **Descartar borrador** | Borra la cabecera y sus líneas | `service.descartar()` |
| Intenta confirmar dos veces | Muestra "ya está confirmada" | `ConciliacionDiaria.confirmar()` |

### Mapa de archivos del CU-01
```
 presentacion/ConciliacionController.java   las 5 rutas
 presentacion/ConciliacionForm.java         el formulario y sus validaciones
 templates/conciliacion/nueva.html          pantalla HU-01
 templates/conciliacion/resumen.html        pantalla HU-02
 negocio/ConciliacionService.java           las reglas y transacciones
 negocio/ConciliacionDiaria.java            cabecera + confirmar()
 negocio/DetalleConciliacion.java           línea + calcular() + tieneDiferencia()
 negocio/EstadoConciliacion.java            BORRADOR / CONFIRMADA
 negocio/*Exception.java                    errores de negocio
 negocio/ConteoArticulo.java, InsumoEnAlerta.java   records
 persistencia/*Repository.java              las consultas
```

---

## 10. Las pruebas automáticas

Una prueba es un método que ejecuta el código y **verifica** el resultado. Si algo se
daña, la prueba falla y avisa antes de que el problema llegue al usuario. Se corren con
`./mvnw test`, y también corren solas en GitHub en cada pull request
(`.github/workflows/ci.yml`).

| Tipo | Archivo | Qué arranca | Qué prueba |
|---|---|---|---|
| **Unitaria** | `negocio/DetalleConciliacionTest` | Nada: solo `new` | La fórmula, `tieneDiferencia()`, confirmar dos veces |
| **Integración** | `negocio/ConciliacionServiceTest` | Spring + H2 en memoria | Todos los criterios con datos reales |
| **Rollback** | `negocio/ConciliacionRollbackTest` | Spring + H2, **sin** `@Transactional` | Que si una línea falla no quede **nada** guardado |
| **Web** | `presentacion/ConciliacionControllerTest` | Spring + MockMvc | Las pantallas: formulario, redirección, 404, mensajes |
| Repositorio | `InsumoRepositoryTest` | Solo JPA (`@DataJpaTest`) | Una consulta |

La estructura de una prueba (*preparar → actuar → verificar*):

```java
@Test
void alConfirmarLoContadoQuedaComoExistenciaYSeMuestraLaAlerta() {
    // preparar: un borrador con 14 kg de harina
    ConciliacionDiaria c = service.registrarBorrador(DIA, ..., conteos(Map.of(HARINA, "14"), Map.of()));
    // actuar
    List<InsumoEnAlerta> alertas = service.confirmar(c.getId());
    // verificar
    assertThat(articuloRepository.findById(HARINA).orElseThrow().getExistenciaActual()).isEqualByComparingTo("14");
    assertThat(alertas).extracting(InsumoEnAlerta::nombre).contains("Harina de Trigo Especial");
}
```

**¿Por qué las pruebas no se estorban entre sí?** `@Transactional` en una clase de prueba
deshace todo al terminar **cada** prueba, así cada una empieza con los datos de ejemplo
intactos. La prueba de rollback **no** lo lleva a propósito: necesita ver que el servicio,
por sí solo, deshizo lo suyo.

**MockMvc** simula el navegador sin abrir uno real:
```java
mockMvc.perform(post("/conciliacion").param("fecha", "2026-11-02") ...)
       .andExpect(content().string(containsString("Ver la existente")));
```

---

## 11. Cómo se trabajó con Git y GitHub

### 11.1 Los conceptos
| Concepto | Qué es |
|---|---|
| **Commit** | Una "foto" del proyecto con un mensaje. En este repo el mensaje va en español y en infinitivo: "Implementar…", "Corregir…". |
| **Rama** (*branch*) | Una línea de trabajo paralela. `main` es la oficial; `feat/C1H1` fue la del CU-01. |
| **Pull Request (PR)** | Una solicitud para unir una rama a `main`, con revisión y pruebas automáticas. |
| **Issue** | Una tarea en GitHub. Hay uno por épica (caso de uso) y uno por historia. |
| **Milestone** | Agrupa issues por sprint, con fecha: Sprint 1 vence el 04/11/2026. |
| **GitHub Project** | El tablero Kanban: *To Do → In Progress → In Review / Testing → Done*. |

### 11.2 La historia real del repositorio
```
 07/10  Juan Pablo  Inicializar el repo: README y plantillas de issues y PR
 02/11  Julián      FASE 1: Spring Boot, H2 en archivo, Thymeleaf y Tailwind
 02/11  Leonardo    FASE 2: una entidad por commit (Proveedor, Articulo, InsumoConfig,
                    ConciliacionDiaria, DetalleConciliacion, TandaProduccion,
                    DetalleTandaInsumo), repositorios y su prueba          → PR #19
 03/11  Juan Pablo  Coherencia: docs por hito, data.sql idempotente, textos de inicio
 03/11  Juan Pablo  FASE 3: tipos de artículo + CU-01-HU-01 y CU-01-HU-02 + pruebas + docs
 03/11  Juan Pablo  README paso a paso, permiso de ejecución de mvnw, responsables
```
Para ver la historia completa: `git log --oneline`.

### 11.3 El ciclo para cada cambio
```
 git switch -c feat/mi-historia     # 1. rama nueva desde main
 (programar)                        # 2. cambiar el código
 ./mvnw test                        # 3. si falla, NO hacer commit
 git add <archivos>                 # 4. elegir qué va en la foto
 git commit -m "Implementar ..."    # 5. tomar la foto
 git push -u origin feat/mi-historia
 (abrir un PR en GitHub → revisión → merge a main → mover el issue en el tablero)
```

---

## 12. Receta: cómo construir la siguiente historia

Es el mismo camino que siguió el CU-01. Sirve para CU-02-HU-01 (Pedro) y CU-03-HU-01 (Nikko).

1. **Leer la historia** en la Entrega 2 o en su issue: los criterios de aceptación son la
   lista de lo que debe funcionar.
2. **Revisar las tablas** en `schema.sql`. ¿Existe la entidad? Si no, crearla en `negocio/`
   con `@Entity`, copiando los nombres de columnas **exactos** (sección 6.1).
3. **Consultas:** agregar métodos al repositorio en `persistencia/` (consulta derivada o
   `@Query`).
4. **Reglas:** un `@Service` en `negocio/` con métodos `@Transactional`. Cada regla que no
   se cumpla lanza una `ReglaDeNegocioException`.
5. **Pantalla:** un `@Controller` en `presentacion/` (y quitar la ruta provisional de
   `HomeController`), un objeto de formulario si hace falta y la plantilla en `templates/`.
6. **Pruebas:** al menos una por criterio de aceptación (sección 10).
7. **Comentar** el código para quien no sabe Java: qué hace cada clase, método y anotación.
8. **Documentar** en `docs/hito-N/`, con una tabla de criterio → código → prueba como la de
   [conciliacion-cu01.md](hito-4/conciliacion-cu01.md).
9. **Actualizar** el recorrido en `docs/guia-de-ejecucion.md` (el profesor lo sigue) y la
   tabla de estado del README.
10. `./mvnw test` → commit → PR → mover el issue en el tablero.

Piezas que ya existen y se pueden reutilizar:
- `InsumoConfigRepository.buscarInsumosEnAlerta()`: los insumos bajo **su** umbral (CU-02).
- `DetalleTandaInsumoRepository`: los consumos por tanda (CU-03).
- `Articulo.TIPO_PRODUCTO_ELABORADO`: el producto de una tanda es un pan (CU-03).

---

## 13. Glosario

| Término | Significado |
|---|---|
| **Anotación** | Etiqueta `@Algo` que Spring o Hibernate leen para saber qué hacer con una clase, campo o método. |
| **Bean** | Objeto que Spring crea y administra (servicios, controladores, repositorios). |
| **`BigDecimal`** | Número decimal exacto. Se usa para kilos y pesos. Se opera con `.add()`, `.subtract()` y se compara con `compareTo`. |
| **Borrador** | Conciliación guardada pero sin confirmar. Todavía no cambia las existencias. |
| **CDN** | Servidor en internet del que se cargan archivos (aquí, Tailwind). |
| **Consulta derivada** | Consulta que Spring Data genera a partir del nombre del método (`findByFecha...`). |
| **Controlador** | Clase `@Controller` que atiende las URLs y elige qué vista mostrar. |
| **Dirty checking** | Hibernate detecta solo los cambios en los objetos leídos dentro de una transacción y los guarda al final. |
| **Entidad** | Clase `@Entity` cuyos objetos son filas de una tabla. |
| **Excepción** | Objeto que se "lanza" (`throw`) para avisar un error y que se "atrapa" con `try/catch`. |
| **Existencia** | Cantidad que hay de un artículo. *Anterior*: punto de partida. *Esperada*: la que debería haber. *Actual*: la que guarda el sistema. |
| **Flash attribute** | Dato que sobrevive a una redirección y luego desaparece. |
| **H2** | Base de datos que vive en un archivo dentro del proyecto. |
| **Hibernate / JPA** | Traducen entre objetos Java y tablas. JPA es el estándar y Hibernate su implementación. |
| **Inyección de dependencias** | Spring le entrega a cada objeto lo que pide en su constructor. Nadie escribe `new`. |
| **Interfaz** | Lista de métodos sin código. Spring Data escribe el código de los repositorios. |
| **JPQL** | Lenguaje de consultas parecido a SQL, pero con nombres de clases y atributos Java. |
| **Maven / mvnw** | Herramienta que descarga librerías, compila, prueba y empaqueta. `mvnw` la descarga sola. |
| **MockMvc** | Simulador de navegador para las pruebas de las pantallas. |
| **`Optional`** | Caja que trae un valor o viene vacía. Evita los `null`. |
| **Paquete** | Carpeta de clases Java (`co.edu.unal.sweng1.altanto.negocio`). |
| **Post/Redirect/Get** | Después de un POST se redirige a un GET, para que recargar la página no repita el envío. |
| **`record`** | Clase de solo datos que Java completa sola. |
| **Reporte Z** | Cierre definitivo de ventas del día que saca la caja registradora. |
| **Repositorio** | Interfaz para guardar y consultar una entidad. |
| **Rollback** | Deshacer todos los cambios de una transacción que falló. |
| **Servicio** | Clase `@Service` con las reglas de negocio. |
| **Spring Boot** | Framework que arma la aplicación (servidor, base de datos, transacciones) con poca configuración. |
| **Starter** | Paquete de Maven que trae varias librerías que se usan juntas. |
| **Thymeleaf** | Motor que llena plantillas HTML con datos en el servidor. |
| **Transacción** | Grupo de operaciones en la base de datos que pasan todas o ninguna. |
| **Umbral mínimo** | Existencia por debajo de la cual hay que pedir un insumo (harina: 25 kg). |
