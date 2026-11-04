package co.edu.unal.sweng1.altanto.negocio;

/**
 * Error que se lanza cuando una operación viola una regla del negocio (por ejemplo,
 * "no se puede confirmar dos veces la misma conciliación").
 *
 * <p>Corresponde a la {@code BusinessRuleException} que propone el diseño de la
 * Entrega 3: los errores de negocio se expresan con excepciones con nombre propio, no
 * con códigos de error.</p>
 *
 * <p><b>¿Por qué extiende {@link RuntimeException}?</b> En Java hay dos clases de
 * excepciones: las "verificadas" ({@code Exception}), que obligan a escribir
 * {@code throws} en cada método, y las "no verificadas" ({@code RuntimeException}), que
 * no. Además, Spring deshace automáticamente (rollback) la transacción de un método
 * {@code @Transactional} cuando sale de él una {@code RuntimeException}. Así se cumple
 * la garantía mínima del CU-01: "si la operación falla, no se guarda nada".</p>
 */
public class ReglaDeNegocioException extends RuntimeException {

    /**
     * @param mensaje texto en español, pensado para mostrárselo al Administrador.
     */
    public ReglaDeNegocioException(String mensaje) {
        // "super(...)" llama al constructor de la clase padre (RuntimeException),
        // que guarda el mensaje para que luego se lea con getMessage().
        super(mensaje);
    }
}
