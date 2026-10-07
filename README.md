# Al-Tanto

> Aplicación web de control de inventario, pedidos y cierre mensual para una panadería de barrio.

**Ingeniería de Software I** · SIA 2016701 · Grupo 4 · 2026-II · Universidad Nacional de Colombia
**Equipo Hefesto:** Julián Santiago Sánchez Castro · Leonardo Felipe Betancur Diaz · Brayan Nikolas Gallo León · Juan Pablo Gómez Cristancho · Pedro Santiago Correa Bolaños

---

## Visión del producto

Para el dueño-administrador de una panadería de barrio que hoy controla sus insumos a ojo, cuenta el inventario a mano y cierra el mes en un cuaderno, **Al-Tanto** es una aplicación web de control de inventario y pedidos que compara lo vendido y lo producido con lo que debería quedar en bodega, avisa cuándo pedir cada insumo antes de quedar *"varado"* y calcula el cierre del mes.

A diferencia del cálculo visual, el conteo manual y el cuaderno con facturas en papel que usa hoy, Al-Tanto deja cada día registrado y conciliado, de modo que un faltante o una diferencia se vea **el mismo día** y no al final del mes.

## Actor

| Actor | Descripción |
|---|---|
| **Administrador** | Dueño de la panadería y maestro panadero. Único usuario de la primera versión. |

## Casos de uso

En orden de prioridad (ver *Entrega 2 – Especificación de requisitos*):

| ID | Caso de uso | Módulo |
|---|---|---|
| CU-01 | Conciliar el día | Conciliación |
| CU-02 | Generar el pedido ante una alerta de reabastecimiento | Pedidos |
| CU-03 | Registrar una tanda de producción | Producción |
| CU-04 | Registrar la recepción de un pedido o una compra de emergencia | Recepción |
| CU-05 | Registrar una factura y un pago a un proveedor | Finanzas |
| CU-06 | Calcular el cierre del mes | Finanzas |

Cada caso de uso se descompone en 2 historias de usuario (12 en total). El backlog vive en el **GitHub Project** del repositorio (tablero Kanban: *To Do → In Progress → In Review / Testing → Done*).

## Alcance de la primera versión

**Dentro**
- Catálogo de artículos (insumos y productos de reventa) y proveedores.
- Conteo diario de existencias en bodega y nevera.
- Ventas diarias digitadas desde el reporte Z.
- Tandas de producción con descuento de insumos.
- Conciliación diaria (esperado vs. contado).
- Alertas de reabastecimiento por umbral mínimo, con sugerencia de pedido.
- Pedidos, recepción y compras de emergencia con su sobrecosto.
- Facturas, pagos a proveedores y cierre mensual con historial.

**Fuera por ahora**
- Conexión con la caja registradora y envío del pedido al proveedor.
- Alertas de vencimiento por lote, recetas y rendimiento calculado.
- Mermas, pan sobrante e inventario de producto terminado.
- Efectivo vs. billeteras virtuales, fiado, facturación electrónica y APIs bancarias.
- Varias sucursales, códigos de barras/QR, IA y usuarios adicionales.

## Flujo de trabajo

1. Toma una historia de la columna **To Do** y asígnatela.
2. Crea una rama `feature/CU-XX-HU-YY-descripcion-corta`.
3. Abre un Pull Request que referencie la historia (`Closes #N`) y muévela a **In Review / Testing**.
4. Al aprobar y fusionar, la historia pasa a **Done**.
