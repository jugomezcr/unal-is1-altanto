package co.edu.unal.sweng1.altanto.negocio;

import java.time.LocalDate;

/**
 * Se lanza cuando el Administrador intenta crear una conciliación para una fecha que
 * ya tiene una.
 *
 * <p>Implementa la extensión <b>1a</b> del CU-01 y el criterio 2 de CU-01-HU-01:
 * "Si ya existe una conciliación de esa fecha, el sistema no permite crear otra y
 * ofrece ver la existente". Por eso la excepción guarda el {@code id} de la
 * conciliación existente: el controlador lo usa para mostrar el enlace "Ver la
 * conciliación existente".</p>
 */
public class ConciliacionDuplicadaException extends ReglaDeNegocioException {

    /** Id de la conciliación que ya existe para esa fecha. */
    private final Long idExistente;

    public ConciliacionDuplicadaException(LocalDate fecha, Long idExistente) {
        super("Ya existe una conciliación para el " + fecha + ". No se puede crear otra.");
        this.idExistente = idExistente;
    }

    public Long getIdExistente() {
        return idExistente;
    }
}
