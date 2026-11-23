package co.edu.unal.sweng1.altanto.negocio.calculo;

import co.edu.unal.sweng1.altanto.negocio.dto.Sobrecosto;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Fórmula del sobrecosto de una compra de emergencia (CU-04-HU-02).
 * Cálculo puro: sin Spring ni base de datos, así se prueba con un simple {@code new}.
 */
public class CalculadoraSobrecosto {

    private static final BigDecimal CIEN = BigDecimal.valueOf(100);

    /**
     * Con precios por unidad:
     * <pre>
     *   pesos      = (pagado − habitual) × cantidad
     *   porcentaje = (pagado − habitual) / habitual × 100
     * </pre>
     * Ambos con 2 decimales. Si se pagó menos que el habitual, salen negativos.
     *
     * @throws IllegalArgumentException si el precio habitual no es mayor que cero
     *                                  (sería dividir por cero; el servicio lo valida antes).
     */
    public Sobrecosto calcular(BigDecimal cantidad, BigDecimal precioPagado, BigDecimal precioHabitual) {
        if (precioHabitual.signum() <= 0) {
            throw new IllegalArgumentException("El precio habitual debe ser mayor que cero.");
        }
        BigDecimal diferenciaPorUnidad = precioPagado.subtract(precioHabitual);
        BigDecimal pesos = diferenciaPorUnidad.multiply(cantidad).setScale(2, RoundingMode.HALF_UP);
        BigDecimal porcentaje = diferenciaPorUnidad.multiply(CIEN)
                .divide(precioHabitual, 2, RoundingMode.HALF_UP);
        return new Sobrecosto(pesos, porcentaje);
    }
}
