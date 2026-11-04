package co.edu.unal.sweng1.altanto.negocio;

/**
 * Se lanza cuando se pide una conciliación por un {@code id} que no existe en la base
 * de datos (por ejemplo, si alguien escribe a mano la URL {@code /conciliacion/999}).
 */
public class ConciliacionNoEncontradaException extends ReglaDeNegocioException {

    public ConciliacionNoEncontradaException(Long id) {
        super("No existe la conciliación con id " + id + ".");
    }
}
