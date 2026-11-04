package co.edu.unal.sweng1.altanto.persistencia.entidad;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.unal.sweng1.altanto.negocio.excepcion.ReglaDeNegocioException;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

/**
 * Prueba UNITARIA: no arranca Spring ni la base de datos, solo crea objetos con
 * {@code new}. Es la prueba más simple posible; buen punto de partida para aprender.
 *
 * <p>Estructura de toda prueba: preparar → actuar → verificar.
 * {@code @Test} marca el método como prueba (como una función {@code test_...} de pytest).</p>
 */
class DetalleConciliacionTest {

    @Test
    void calculaLoEsperadoYLaDiferencia() {
        // Preparar: había 20, se vendieron 2, se contaron 17
        DetalleConciliacion d = new DetalleConciliacion();
        d.setExistenciaAnterior(new BigDecimal("20"));
        d.setCantidadSalida(new BigDecimal("2"));
        d.setCantidadContada(new BigDecimal("17"));

        // Actuar
        d.calcular();

        // Verificar: se esperaban 18 y falta 1.
        // isEqualByComparingTo compara el valor sin importar decimales (18 = 18.000).
        assertThat(d.getExistenciaEsperada()).isEqualByComparingTo("18");
        assertThat(d.getDiferencia()).isEqualByComparingTo("-1");
        assertThat(d.tieneDiferencia()).isTrue();
    }

    @Test
    void unaConciliacionSoloSePuedeConfirmarUnaVez() {
        ConciliacionDiaria c = new ConciliacionDiaria();
        c.confirmar();

        assertThat(c.getEstado()).isEqualTo(EstadoConciliacion.CONFIRMADA);
        // assertThatThrownBy: pasa solo si el código lanza esa excepción (como pytest.raises)
        assertThatThrownBy(c::confirmar).isInstanceOf(ReglaDeNegocioException.class);
    }
}
