package co.edu.unal.sweng1.altanto.negocio.excepcion;

/** Se lanza al intentar recibir un pedido que no existe. */
public class PedidoNoEncontradoException extends ReglaDeNegocioException {

    public PedidoNoEncontradoException(Long id) {
        super("No existe el pedido con id " + id + ".");
    }
}
