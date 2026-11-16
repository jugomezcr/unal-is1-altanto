package co.edu.unal.sweng1.altanto.negocio.dto;

import java.math.BigDecimal;

/**
 * Un insumo gastado en una tanda, tal como lo digitó el Administrador (CU-03): qué insumo
 * y cuánto. La harina pesada también viaja como una línea de estas.
 *
 * <p>Es un {@code record}: una clase de Java pensada solo para transportar datos. Java
 * genera el constructor, los "getters" ({@code insumoId()}, {@code cantidad()}),
 * {@code equals}, {@code hashCode} y {@code toString}. Es inmutable: una vez creado, no
 * cambia.</p>
 *
 * <p>Sirve para que el controlador (capa de presentación) le pase las líneas del
 * formulario al servicio (capa de negocio) sin que el servicio dependa de clases web. Es
 * el equivalente de {@link ConteoArticulo} en el CU-01.</p>
 *
 * @param insumoId id del insumo gastado (tabla {@code articulos}).
 * @param cantidad cantidad gastada, en la unidad de medida del insumo.
 */
public record InsumoGastado(Long insumoId, BigDecimal cantidad) {
}
