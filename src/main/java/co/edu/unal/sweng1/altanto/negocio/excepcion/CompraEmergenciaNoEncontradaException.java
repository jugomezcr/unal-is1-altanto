package co.edu.unal.sweng1.altanto.negocio.excepcion;

/** Se lanza al pedir una compra de emergencia con un {@code id} que no existe. */
public class CompraEmergenciaNoEncontradaException extends ReglaDeNegocioException {

    public CompraEmergenciaNoEncontradaException(Long id) {
        super("No existe la compra de emergencia con id " + id + ".");
    }
}
