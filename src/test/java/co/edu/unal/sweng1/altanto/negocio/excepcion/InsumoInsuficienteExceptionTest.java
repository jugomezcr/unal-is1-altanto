package co.edu.unal.sweng1.altanto.negocio.excepcion;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

/**
 * Prueba UNITARIA de {@link InsumoInsuficienteException}: no arranca Spring ni la base de
 * datos, solo crea el objeto con {@code new}. Verifica que la excepción lleva la
 * información que CU-03-HU-02 exige mostrar: de qué insumo se trata y cuánto hay frente a
 * cuánto se necesita.
 */
class InsumoInsuficienteExceptionTest {

    @Test
    void guardaElInsumoYLasCantidadesYArmaUnMensajeClaro() {
        // Preparar y actuar: la levadura tiene 1500 g y se querían gastar 2000 g.
        InsumoInsuficienteException e = new InsumoInsuficienteException(
                "Levadura Fresca en Bloque", new BigDecimal("1500"), new BigDecimal("2000"));

        // Verificar: los datos quedan accesibles para el controlador y la pantalla.
        assertThat(e.getNombreInsumo()).isEqualTo("Levadura Fresca en Bloque");
        assertThat(e.getDisponible()).isEqualByComparingTo("1500");
        assertThat(e.getRequerido()).isEqualByComparingTo("2000");

        // Y el mensaje para el Administrador nombra el insumo.
        assertThat(e.getMessage()).contains("Levadura Fresca en Bloque");

        // Es una regla de negocio: así Spring hace rollback automático al lanzarla.
        assertThat(e).isInstanceOf(ReglaDeNegocioException.class);
    }
}
