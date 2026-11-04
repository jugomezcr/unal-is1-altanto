package co.edu.unal.sweng1.altanto.persistencia.entidad;

import co.edu.unal.sweng1.altanto.negocio.excepcion.ReglaDeNegocioException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

/**
 * Pruebas UNITARIAS de las reglas que viven en las entidades del CU-01.
 *
 * <p>"Unitaria" = no arranca Spring ni la base de datos: crea los objetos con
 * {@code new} y revisa el resultado. Por eso corre en milisegundos.</p>
 *
 * <p>Cada método {@code @Test} es una prueba independiente. Su nombre describe lo que
 * se espera. {@code assertThat(x).isEqualTo(y)} (librería AssertJ) hace fallar la
 * prueba si {@code x} no es {@code y}.</p>
 */
class DetalleConciliacionTest {

    /** Crea una línea con los 4 datos de entrada y aplica la fórmula. */
    private DetalleConciliacion linea(String anterior, String recibida, String salida, String contada) {
        DetalleConciliacion d = new DetalleConciliacion();
        d.setExistenciaAnterior(new BigDecimal(anterior));
        d.setCantidadRecibida(new BigDecimal(recibida));
        d.setCantidadSalida(new BigDecimal(salida));
        d.setCantidadContada(new BigDecimal(contada));
        d.calcular();
        return d;
    }

    @Test
    void calculaEsperadaComoAnteriorMasRecibidoMenosSalida() {
        DetalleConciliacion d = linea("40", "50", "25", "60");

        // 40 + 50 − 25 = 65 esperados; se contaron 60 → faltan 5
        // compareTo == 0 compara el valor sin importar la escala (65 vs 65.000)
        assertThat(d.getExistenciaEsperada()).isEqualByComparingTo("65");
        assertThat(d.getDiferencia()).isEqualByComparingTo("-5");
        assertThat(d.tieneDiferencia()).isTrue();
    }

    @Test
    void sinDiferenciaCuandoLoContadoCuadra() {
        DetalleConciliacion d = linea("20.000", "0", "2", "18.000");

        assertThat(d.getDiferencia()).isEqualByComparingTo("0");
        assertThat(d.tieneDiferencia()).isFalse();
    }

    @Test
    void conciliacionNuevaEsBorradorYSePuedeConfirmarUnaSolaVez() {
        ConciliacionDiaria c = new ConciliacionDiaria();
        assertThat(c.esBorrador()).isTrue();

        c.confirmar();
        assertThat(c.getEstado()).isEqualTo(EstadoConciliacion.CONFIRMADA);

        // assertThatThrownBy: la prueba pasa solo si el código lanza esa excepción
        assertThatThrownBy(c::confirmar).isInstanceOf(ReglaDeNegocioException.class);
    }
}
