package co.edu.unal.sweng1.altanto.negocio.dto;

import java.math.BigDecimal;

/**
 * Un insumo cuya existencia quedó en su umbral mínimo o por debajo.
 *
 * <p>Lo usa CU-01-HU-02 (criterio 4): al confirmar la conciliación, "si algún insumo
 * queda en su umbral o por debajo, el sistema muestra la alerta de reabastecimiento".
 * El detalle completo de las alertas (con proveedor habitual) es la pantalla de
 * CU-02-HU-01 ({@code /inventario/alertas}).</p>
 *
 * <p>Es un {@code record} (ver {@link ConteoArticulo}). La consulta que lo llena está
 * en {@code InsumoConfigRepository.buscarInsumosEnAlerta()}.</p>
 */
public record InsumoEnAlerta(String nombre, String unidadMedida, BigDecimal existencia, BigDecimal umbral) {
}
