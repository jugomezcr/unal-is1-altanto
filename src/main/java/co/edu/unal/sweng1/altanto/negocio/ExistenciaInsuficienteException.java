package co.edu.unal.sweng1.altanto.negocio;

import java.math.BigDecimal;

/**
 * Thrown when a production batch (CU-03-HU-01) would consume more of an input than
 * what is currently in stock (step 4a of the use case).
 *
 * <p>It extends {@link ReglaDeNegocioException} so the Spring transaction rolls back,
 * and it carries the input name plus the existing and required amounts: the controller
 * turns them into the "Insumo X insuficiente" banner without querying anything else.</p>
 */
public class ExistenciaInsuficienteException extends ReglaDeNegocioException {

    /** Article name of the input that ran out. */
    private final String nombreInsumo;

    /** Stock of that input at validation time. */
    private final BigDecimal existente;

    /** Amount the batch asked for (including duplicated rows of the same input). */
    private final BigDecimal requerido;

    public ExistenciaInsuficienteException(String nombreInsumo, BigDecimal existente, BigDecimal requerido) {
        super("Insumo " + nombreInsumo + " insuficiente: hay " + existente + " y se requieren " + requerido + ".");
        this.nombreInsumo = nombreInsumo;
        this.existente = existente;
        this.requerido = requerido;
    }

    public String getNombreInsumo() {
        return nombreInsumo;
    }

    public BigDecimal getExistente() {
        return existente;
    }

    public BigDecimal getRequerido() {
        return requerido;
    }
}
