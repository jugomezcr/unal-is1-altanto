package co.edu.unal.sweng1.altanto.negocio.excepcion;

import java.math.BigDecimal;

/**
 * Se lanza cuando, al registrar una tanda de producción (CU-03), un insumo no tiene
 * existencia suficiente para lo que se quiere gastar.
 *
 * <p>Implementa los criterios 1 y 2 de CU-03-HU-02: "antes de registrar, verificar que
 * haya existencia suficiente de cada insumo" y "si la existencia de un insumo es menor
 * que lo gastado, indicar de cuál se trata y no registrar la tanda". Por eso la excepción
 * guarda el nombre del insumo y las cantidades disponible y requerida: el controlador las
 * muestra en un mensaje claro para el Administrador.</p>
 *
 * <p>Como hereda de {@link ReglaDeNegocioException} (que a su vez es una
 * {@code RuntimeException}), Spring deshace la transacción automáticamente (rollback), así
 * que si un insumo no alcanza no queda nada guardado (criterio 4 de HU-02).</p>
 */
public class InsumoInsuficienteException extends ReglaDeNegocioException {

    /** Nombre del insumo que no alcanzó (para mostrárselo al Administrador). */
    private final String nombreInsumo;

    /** Existencia que hay del insumo. */
    private final BigDecimal disponible;

    /** Cantidad que se quería gastar en la tanda. */
    private final BigDecimal requerido;

    public InsumoInsuficienteException(String nombreInsumo, BigDecimal disponible, BigDecimal requerido) {
        super("El insumo \"" + nombreInsumo + "\" no alcanza: hay " + disponible
                + " y se necesitan " + requerido + ".");
        this.nombreInsumo = nombreInsumo;
        this.disponible = disponible;
        this.requerido = requerido;
    }

    public String getNombreInsumo() {
        return nombreInsumo;
    }

    public BigDecimal getDisponible() {
        return disponible;
    }

    public BigDecimal getRequerido() {
        return requerido;
    }
}
