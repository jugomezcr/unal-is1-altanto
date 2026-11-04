# Guía de ejecución

Guía detallada para ejecutar y probar Al-Tanto. El resumen está en el [README](../README.md#inicio-rápido).

Estos pasos sirven en Windows, Linux y macOS. No hace falta instalar Maven ni una base de
datos: el proyecto trae Maven Wrapper (`mvnw`), que descarga Maven solo, y usa H2, una
base de datos que se guarda en un archivo dentro del proyecto.

## Paso 0. Requisitos

| Requisito | Cómo verificarlo | Resultado esperado |
|---|---|---|
| **Git** | `git --version` | `git version 2.x` o superior |
| **JDK 21 o superior** (por ejemplo, [Eclipse Temurin 21](https://adoptium.net/)) | `java -version` | `version "21..."` o un número mayor |
| **Conexión a internet** | (solo la primera vez) | Maven y las dependencias se descargan en el primer arranque |
| **Puerto 8080 libre** | | Ver *Problemas frecuentes* si está ocupado |

> Si `java -version` responde, pero el paso 2 dice que no encuentra Java, configure la
> variable de entorno `JAVA_HOME` con la carpeta de instalación del JDK.

## Paso 1. Clonar el repositorio y entrar a la carpeta

```bash
git clone https://github.com/jugomezcr/unal-is1-altanto.git
cd unal-is1-altanto
```

## Paso 2. Arrancar la aplicación

Use el comando de su sistema operativo:

| Sistema | Comando |
|---|---|
| Linux / macOS / Git Bash | `./mvnw spring-boot:run` |
| Windows (PowerShell) | `.\mvnw.cmd spring-boot:run` |
| Windows (Símbolo del sistema, `cmd`) | `mvnw.cmd spring-boot:run` |

La primera vez tarda unos minutos, mientras descarga Maven y las dependencias. La
aplicación está lista cuando la terminal muestra una línea como esta:

```
Started AltantoApplication in 4.1 seconds
```

**Deje esa terminal abierta**: mientras siga abierta, la aplicación sigue funcionando.
En el primer arranque se crea la carpeta `data/` con la base de datos y sus datos de
ejemplo (insumos, productos, proveedores y una conciliación ya confirmada del 01/11/2026).

## Paso 3. Abrir la aplicación

Abra en el navegador: **<http://localhost:8080>**

Aparece la página de inicio de Al-Tanto, con un módulo por cada caso de uso del Sprint 1.

## Paso 4. Probar el CU-01 Conciliar el día (CU-01-HU-01 y CU-01-HU-02)

1. En el menú, entre a **Conciliación (CU-01)**. También puede abrir <http://localhost:8080/conciliacion>.
2. Deje la fecha de hoy y escriba un **total de ventas del reporte Z**, por ejemplo `250000`.
3. En **Contado**, digite una cantidad para cada artículo. En **Vendido**, que solo
   aparece en los productos de reventa (kumis y gaseosa), digite lo vendido.
   Para ver una diferencia, cuente **14** de *Harina de Trigo Especial* (se esperaban 15).
4. Pulse **Guardar y ver diferencias**. Aparece la tabla *anterior + recibido − vendido o
   consumido = esperado*, comparada con lo contado. Las filas que no cuadran salen en rojo.
5. Pulse **Confirmar conciliación**. El estado pasa a **CONFIRMADA** y aparece la
   **alerta de reabastecimiento** de la harina (14 kg está por debajo de su umbral de 25 kg).
6. Extensión 1a: vuelva a **Conciliación (CU-01)** y cambie la fecha a **02/11/2026**
   (ya tiene un borrador) o a la fecha de hoy (ya quedó confirmada). Al guardar, el
   sistema no deja crear otra conciliación y ofrece **Ver la existente**.

> Los módulos **CU-02** (alertas) y **CU-03** (tandas de producción) los están
> construyendo sus responsables. Mientras tanto, sus páginas muestran "en construcción".

## Paso 5 (opcional). Ver la base de datos

1. Abra <http://localhost:8080/h2-console>.
2. Llene el formulario así y pulse **Connect**:
   - **JDBC URL:** `jdbc:h2:file:./data/altanto`
   - **User Name:** `sa`
   - **Password:** (vacía)
3. Ejemplo de consulta: `SELECT * FROM CONCILIACIONES_DIARIAS;`

## Paso 6. Detener la aplicación

En la terminal del paso 2, pulse **Ctrl + C**. Si pregunta "¿Desea terminar el trabajo
por lotes (S/N)?", responda `S`.

## Paso 7. Correr las pruebas automáticas

Con la aplicación detenida:

| Sistema | Comando |
|---|---|
| Linux / macOS / Git Bash | `./mvnw test` |
| Windows (PowerShell) | `.\mvnw.cmd test` |
| Windows (`cmd`) | `mvnw.cmd test` |

Debe terminar con `Failures: 0, Errors: 0` y `BUILD SUCCESS`. Las pruebas
usan una base de datos en memoria propia, así que no modifican la carpeta `data/`.

## Problemas frecuentes

| Síntoma | Solución |
|---|---|
| `Permission denied` al ejecutar `./mvnw` (Linux/macOS) | `chmod +x mvnw` y repetir el paso 2 |
| `JAVA_HOME not found` o `JAVA_HOME is set to an invalid directory` | Instalar el JDK 21+ y apuntar `JAVA_HOME` a su carpeta |
| `Port 8080 was already in use` | Cerrar el programa que usa ese puerto, o arrancar en otro puerto: `./mvnw spring-boot:run "-Dspring-boot.run.arguments=--server.port=8081"` y abrir <http://localhost:8081> |
| Quiero volver a los datos de ejemplo | Detener la aplicación (paso 6), borrar la carpeta `data/` y repetir el paso 2 |

