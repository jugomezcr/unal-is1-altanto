# CU-04-HU-01: Recibir pedido

## Flujo

```text
GET  /recepcion-pedidos/pendientes  → pedidos con estado PENDIENTE
GET  /recepcion-pedidos/{id}        → cantidades pedidas y formulario de recepción
POST /recepcion-pedidos/{id}        → valida, guarda lo recibido, suma existencias y cierra
```

La recepción conserva cada línea del pedido por separado, incluso cuando varias líneas
se refieren al mismo artículo. La cantidad digitada está en unidad de inventario y puede
diferir de la pedida. Se admite cero; se rechazan campos vacíos, cantidades negativas o
más de tres decimales. Solo se reciben insumos y productos de reventa.

La confirmación se ejecuta en una transacción: persiste `cantidad_recibida`, suma esa
cantidad a `Articulo.existenciaActual`, registra `fecha_recepcion` y cambia el pedido a
`CERRADO`. `Pedido.version` usa `@Version` para detectar una confirmación concurrente; un
pedido ya recibido muestra el mensaje «Este pedido ya fue recibido o está siendo
procesado». Cancelar es un enlace a la lista y no envía cambios.

## Archivos principales

- `RecepcionPedidoController` y `RecepcionPedidoForm`: rutas y binding web.
- `RecepcionPedidoService`: validación y transacción de recepción.
- `templates/recepcion-pedidos/`: lista de pendientes y formulario de recepción.
- `schema.sql` / `data.sql`: tablas y pedido de ejemplo definidos en la etapa A.

## Pruebas

```powershell
.\mvnw.cmd test
```

Las pruebas del servicio verifican suma por línea, cantidades válidas, cierre, rechazo
de doble recepción y ausencia de cambios ante errores. Las pruebas web verifican lista,
formulario, confirmación y cancelación sin mutación.
