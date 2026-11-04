package co.edu.unal.sweng1.altanto.negocio.dto;

import java.math.BigDecimal;

/**
 * Lo que el Administrador digitó para UN artículo al cierre del día (CU-01-HU-01).
 *
 * <p>Es un {@code record}: una clase de Java pensada solo para transportar datos.
 * Java genera solo el constructor, los "getters" ({@code articuloId()},
 * {@code vendido()}, {@code contado()}), {@code equals}, {@code hashCode} y
 * {@code toString}. Es inmutable: una vez creado, no cambia.</p>
 *
 * <p>Sirve para que el controlador (capa de presentación) le pase los datos del
 * formulario al servicio (capa de negocio) sin que el servicio dependa de clases web.</p>
 *
 * @param articuloId id del artículo (tabla {@code articulos}).
 * @param vendido    cantidad vendida según el reporte Z. Solo aplica a productos de
 *                   reventa; para insumos llega {@code null} o cero.
 * @param contado    cantidad contada físicamente en bodega y nevera.
 */
public record ConteoArticulo(Long articuloId, BigDecimal vendido, BigDecimal contado) {
}
