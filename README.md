<div align="center">

# Al-Tanto

**Control de inventario, pedidos y cierre mensual para una panadería de barrio**

[![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.2-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Maven](https://img.shields.io/badge/Maven%20Wrapper-3.10-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)](https://maven.apache.org/wrapper/)
[![H2](https://img.shields.io/badge/H2-modo%20archivo-1F4E79?style=for-the-badge)](https://www.h2database.com/)
[![Thymeleaf](https://img.shields.io/badge/Thymeleaf-vistas-005F0F?style=for-the-badge&logo=thymeleaf&logoColor=white)](https://www.thymeleaf.org/)
[![Tailwind CSS](https://img.shields.io/badge/Tailwind%20CSS-CDN-06B6D4?style=for-the-badge&logo=tailwindcss&logoColor=white)](https://tailwindcss.com/)

Ingeniería de Software I · SIA 2016701 · Grupo 4 · 2026-II · Universidad Nacional de Colombia

</div>

Al-Tanto es una aplicación web para el dueño de una panadería de barrio que hoy controla sus
insumos a ojo, cuenta el inventario a mano y cierra el mes en un cuaderno. La aplicación
compara lo vendido y lo producido con lo que debería quedar en bodega, avisa cuándo pedir
cada insumo antes de quedar *"varado"* y calcula el cierre del mes. Así, un faltante se ve
**el mismo día** y no al final del mes.

---

## Requisitos

| Elemento | Versión | Notas |
|:---|:---|:---|
| **JDK** | 21 o superior | Obligatorio. Verificar con `java -version` |
| **Git** | 2.x | Para clonar el repositorio |
| **Maven** | No hace falta | El proyecto trae Maven Wrapper (`mvnw`), que lo descarga solo la primera vez |
| **Base de datos** | No hace falta | H2 embebida. Se crea sola en `./data/altanto.mv.db` |
| **Puerto** | `8080` libre | Se puede cambiar (ver la [guía de ejecución](docs/guia-de-ejecucion.md#problemas-frecuentes)) |
| **Internet** | Solo en el primer arranque | Para descargar Maven y las dependencias |

## Inicio rápido

**1. Clonar el repositorio**

```bash
git clone https://github.com/jugomezcr/unal-is1-altanto.git
cd unal-is1-altanto
```

**2. Arrancar la aplicación**

| Sistema | Comando |
|:---|:---|
| Linux / macOS / Git Bash | `./mvnw spring-boot:run` |
| Windows (PowerShell) | `.\mvnw.cmd spring-boot:run` |
| Windows (`cmd`) | `mvnw.cmd spring-boot:run` |

La aplicación está lista cuando la terminal muestra `Started AltantoApplication`. Deje la
terminal abierta mientras la usa y deténgala con **Ctrl + C**.

**3. Abrir la aplicación:** <http://localhost:8080>

**4. Correr las pruebas** (con la aplicación detenida)

| Sistema | Comando |
|:---|:---|
| Linux / macOS / Git Bash | `./mvnw test` |
| Windows (PowerShell) | `.\mvnw.cmd test` |
| Windows (`cmd`) | `mvnw.cmd test` |

El resultado esperado es `BUILD SUCCESS`.

> **Guía detallada:** el [recorrido para probar cada historia](docs/guia-de-ejecucion.md#paso-4-probar-el-cu-01-conciliar-el-día-cu-01-hu-01-y-cu-01-hu-02),
> el acceso a la [consola de la base de datos](docs/guia-de-ejecucion.md#paso-5-opcional-ver-la-base-de-datos)
> y los [problemas frecuentes](docs/guia-de-ejecucion.md#problemas-frecuentes) están en
> [`docs/guia-de-ejecucion.md`](docs/guia-de-ejecucion.md).

## Estado del Sprint 1 (Hito 4)

| Historia | Descripción | Responsable | Estado |
|:---|:---|:---|:---|
| [CU-01-HU-01](https://github.com/jugomezcr/unal-is1-altanto/issues/2) | Digitar lo vendido y lo contado del día | Juan Pablo Gómez | **Implementada** |
| [CU-01-HU-02](https://github.com/jugomezcr/unal-is1-altanto/issues/3) | Ver las diferencias y confirmar la conciliación | Juan Pablo Gómez | **Implementada** |
| [CU-02-HU-01](https://github.com/jugomezcr/unal-is1-altanto/issues/5) | Ver los insumos en alerta | Pedro Correa | Pendiente |
| [CU-03-HU-01](https://github.com/jugomezcr/unal-is1-altanto/issues/8) | Registrar la harina pesada, las unidades obtenidas y los insumos gastados | Brayan Nikolás Gallo | En desarrollo |

El backlog completo (6 casos de uso y 12 historias) está en el
[GitHub Project *Al-Tanto · Backlog*](https://github.com/users/jugomezcr/projects/2).

## Estructura del repositorio

```
src/main/java/co/edu/unal/sweng1/altanto/
├── presentacion/
│   ├── controlador/  # controladores web (@Controller): URL → vista
│   └── formulario/   # objetos que reciben los datos de un <form>
├── negocio/
│   ├── servicio/     # reglas de negocio y transacciones (@Service)
│   ├── calculo/      # cálculos puros, sin Spring ni base de datos
│   ├── dto/          # records que solo transportan datos entre capas
│   └── excepcion/    # errores de negocio con nombre propio
└── persistencia/
    ├── entidad/      # clases @Entity: una clase = una tabla
    └── repositorio/  # consultas (Spring Data JPA)
src/main/resources/
├── templates/        # vistas Thymeleaf (una carpeta por módulo)
├── schema.sql        # tablas de la base de datos
├── data.sql          # datos de ejemplo (solo se cargan si las tablas están vacías)
└── application.properties
src/test/java/        # pruebas unitarias, de integración y web (MockMvc)
docs/                 # documentación del proyecto, organizada por hito
```

**La arquitectura es por capas** y sigue el repositorio de referencia del curso: la
presentación llama al negocio, y el negocio a la persistencia. Ninguna capa se salta a la
otra.

## Casos de uso

Un solo actor, el **Administrador** (dueño y maestro panadero). Los casos de uso están en
orden de prioridad, según la Entrega 2:

| ID | Caso de uso | Módulo |
|:---|:---|:---|
| CU-01 | Conciliar el día | Conciliación |
| CU-02 | Generar el pedido ante una alerta de reabastecimiento | Pedidos |
| CU-03 | Registrar una tanda de producción | Producción |
| CU-04 | Registrar la recepción de un pedido o una compra de emergencia | Recepción |
| CU-05 | Registrar una factura y un pago a un proveedor | Finanzas |
| CU-06 | Calcular el cierre del mes | Finanzas |

**Fuera de la primera versión:** conexión con la caja registradora, envío del pedido al
proveedor, recetas y rendimiento calculado, mermas e inventario de pan, medios de pago y
fiado, facturación electrónica, varias sucursales y usuarios adicionales.

## Documentación

- **[Arquitectura](docs/arquitectura.md):** mapa de carpetas y de cómo se conecta cada pieza
  (controlador → servicio → repositorio → tabla), con equivalencias para quien viene de Python.
- **[Al-Tanto desde cero](docs/guia-desde-cero.md):** cómo está construido el proyecto,
  explicado para quien nunca ha usado Java ni Spring Boot, hasta el CU-01 terminado.
- **[Guía de ejecución](docs/guia-de-ejecucion.md):** cómo ejecutar y probar la aplicación
  paso a paso.

| Hito | Entrega | Documentos |
|:---|:---|:---|
| 1 | Definición del producto | [Entrega 1 (PDF)](docs/hito-1/Proy.%20Al%20tanto%20-%20Entrega%201.pdf) |
| 2 | Especificación de requisitos | [Entrega 2 (PDF)](docs/hito-2/Proy.%20%20Altanto%20-%20Entrega%202.pdf) |
| 3 | Diseño arquitectónico y detallado | [Entrega 3 (PDF)](docs/hito-3/Proy.%20Al%20tanto%20-%20Entrega%203.pdf) |
| 4 | Primer incremento funcional | [Compromiso (PDF)](docs/hito-4/Proy.%20Al%20tanto%20-%20Entrega%204.pdf) · [Plan de trabajo (PDF)](docs/hito-4/Plan%20-%20Entrega%204.pdf) · [Plan y trazabilidad](docs/hito-4/plan-y-trazabilidad.md) · [Persistencia](docs/hito-4/persistencia-jpa-y-repositorios.md) · [CU-01 Conciliar el día](docs/hito-4/conciliacion-cu01.md) |

## Equipo Hefesto

| Integrante | GitHub |
|:---|:---|
| Julián Santiago Sánchez Castro | [@Julsanchezc](https://github.com/Julsanchezc) |
| Leonardo Felipe Betancur Diaz | [@BETAPANDERETA](https://github.com/BETAPANDERETA) |
| Brayan Nikolas Gallo León | [@NikkoWebDev](https://github.com/NikkoWebDev) |
| Juan Pablo Gómez Cristancho | [@jugomezcr](https://github.com/jugomezcr) |
| Pedro Santiago Correa Bolaños | [@pedroerjes](https://github.com/pedroerjes) |
