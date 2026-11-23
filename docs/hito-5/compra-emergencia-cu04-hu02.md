# CU-04-HU-02: Registrar una compra de emergencia y ver su sobrecosto

Issue #12 (épica #10) · Se apoya en [recepcion-pedidos-cu04.md](recepcion-pedidos-cu04.md)
(HU-01), que ya trae el menú **Recepción (CU-04)**.

## 1. Qué hace

El Administrador compró algo fuera del pedido normal. Digita el artículo, lo que llegó, el
precio que pagó y el precio habitual del mayorista; el sistema muestra cuánto más pagó
(en pesos y en %) y, al confirmar, suma lo recibido a las existencias y deja la compra
marcada con su sobrecosto.

## 2. Flujo

```
GET  /recepcion-pedidos/emergencia          → formulario (también desde "Pedidos pendientes")
POST /recepcion-pedidos/emergencia/revisar  → valida y muestra la diferencia. NO guarda
POST /recepcion-pedidos/emergencia          → confirma: guarda la compra y suma existencias
GET  /recepcion-pedidos/emergencia/{id}     → resumen con el sobrecosto
```

Cancelar es un enlace: no envía nada, por eso no se guarda nada.

## 3. Criterios de aceptación y dónde se cumplen

| Criterio | Implementación | Prueba |
|---|---|---|
| 1. Digitar cantidades, precio pagado y precio habitual | `CompraEmergenciaForm`, `emergencia.html` | `CompraEmergenciaControllerTest` |
| 2. Mostrar la diferencia en pesos y en % | `CalculadoraSobrecosto`, `emergencia-revisar.html` | `CalculadoraSobrecostoTest`, `CompraEmergenciaControllerTest` |
| 3. Al confirmar, existencias suben y la compra queda con su sobrecosto | `CompraEmergenciaService.registrar` (`@Transactional`) | `CompraEmergenciaServiceTest` |
| 4. Si se cancela, no se registra | "Ver sobrecosto" no guarda; Cancelar es un enlace | `CompraEmergenciaControllerTest` (paso 2: no hay cambios) |

## 4. Qué se construyó

| Capa | Archivo |
|---|---|
| presentacion/controlador | `CompraEmergenciaController` |
| presentacion/formulario | `CompraEmergenciaForm` |
| negocio/servicio | `CompraEmergenciaService` |
| negocio/calculo | `CalculadoraSobrecosto` (la fórmula) |
| negocio/dto | `Sobrecosto` (pesos y porcentaje) |
| negocio/excepcion | `CompraEmergenciaNoEncontradaException` |
| persistencia/entidad | `CompraEmergencia` (tabla `compras_emergencia`, nueva en `schema.sql`) |
| persistencia/repositorio | `CompraEmergenciaRepository` |
| templates/recepcion | `emergencia.html`, `emergencia-revisar.html`, `emergencia-resumen.html` |

## 5. Decisiones

- **Fórmula** (precios **por unidad**): `pesos = (pagado − habitual) × cantidad` y
  `porcentaje = (pagado − habitual) / habitual × 100`, ambos con 2 decimales. Si se pagó
  menos que lo habitual, salen negativos (no se bloquea).
- **Una compra = un artículo.** La épica pide una compra puntual de emergencia; varias
  líneas en una sola compra quedan aplazadas.
- **Mostrar y confirmar son dos pasos** sin guardar entre ellos (los datos viajan en campos
  ocultos). El servicio **recalcula** el sobrecosto al confirmar; no confía en lo mostrado.
- **Artículos permitidos:** insumos y productos de reventa activos, igual que en HU-01.
- **Proveedor:** no se registra (la historia solo habla del "mayorista habitual").

## 6. Aplazado

- Evitar el **doble clic** en "Confirmar compra" (registraría dos compras). HU-01 lo evita
  con `@Version` en el pedido; aquí no hay un registro previo que proteger.
- Consultar un historial de compras de emergencia (hoy solo existe el resumen por `id`).

## 7. Cómo probar

```bash
./mvnw test              # CalculadoraSobrecostoTest, CompraEmergenciaServiceTest, CompraEmergenciaControllerTest
./mvnw spring-boot:run   # http://localhost:8080/recepcion-pedidos/emergencia
```

Prueba manual: ver el paso 4c de [guia-de-ejecucion.md](../guia-de-ejecucion.md). Con
*Harina de Trigo Especial*, cantidad `20`, precio pagado `4000` y habitual `3200`, la
diferencia es **16000.00** pesos y **25.00 %**; al confirmar, la existencia sube 20 kg.
Las pruebas automáticas usan 10 kg (8000.00 y 25.00 %) sobre los 15 kg de `data.sql`.
