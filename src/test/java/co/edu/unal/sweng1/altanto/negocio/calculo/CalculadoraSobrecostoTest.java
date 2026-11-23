package co.edu.unal.sweng1.altanto.negocio.calculo;

import static org.assertj.core.api.Assertions.assertThat;

import co.edu.unal.sweng1.altanto.negocio.dto.Sobrecosto;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

/** Prueba UNITARIA de la fórmula del sobrecosto: sin Spring ni base de datos, solo {@code new}. */
class CalculadoraSobrecostoTest {

    private final CalculadoraSobrecosto calculadora = new CalculadoraSobrecosto();

    @Test
    void calculaLaDiferenciaEnPesosYEnPorcentaje() {
        // 10 kg pagados a 4000 cuando lo habitual es 3200: 800 de más por kg.
        Sobrecosto s = calculadora.calcular(
                new BigDecimal("10"), new BigDecimal("4000"), new BigDecimal("3200"));

        // isEqualByComparingTo compara el valor sin importar la escala (8000 = 8000.00).
        assertThat(s.pesos()).isEqualByComparingTo("8000.00");   // 800 × 10
        assertThat(s.porcentaje()).isEqualByComparingTo("25.00"); // 800 / 3200 × 100
    }

}
