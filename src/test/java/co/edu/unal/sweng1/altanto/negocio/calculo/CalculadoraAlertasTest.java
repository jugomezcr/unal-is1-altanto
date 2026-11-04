package co.edu.unal.sweng1.altanto.negocio.calculo;

import co.edu.unal.sweng1.altanto.negocio.dto.DatosInsumo;
import co.edu.unal.sweng1.altanto.negocio.dto.InsumoPanel;
import co.edu.unal.sweng1.altanto.negocio.dto.NivelAlerta;
import static co.edu.unal.sweng1.altanto.negocio.servicio.DatosAlertasPrueba.*;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;


// Pruebas unitarias de las reglas puras de CalculadoraAlerta (nivel, déficit y cobertura).
// Sin Spring ni base de datos: solo new. Los datos vienen de DatosAlertasPrueba.
class CalculadoraAlertasTest {

    // límite crítico de 1 día, como en la configuración por defecto
    private final CalculadoraAlerta calculadora = new CalculadoraAlerta(BigDecimal.ONE);

    // compara números sin que importe la escala (2.0 y 2.00 son iguales)
    private void assertNumero(String esperado, BigDecimal real) {
        assertNotNull(real);
        assertEquals(0, new BigDecimal(esperado).compareTo(real), "esperado " + esperado + " y fue " + real);
    }

    @Test
    void harinaEsCriticaConCoberturaDe043Dias() {
        InsumoPanel fila = calculadora.armarPanel(harina());
        assertEquals(NivelAlerta.CRITICO, fila.nivel());
        assertNumero("0.43", fila.coberturaDias());
        assertNumero("10", fila.deficit());
    }

    @Test
    void levaduraYMargarinaSonNormalesYNoTienenDeficit() {
        InsumoPanel levadura = calculadora.armarPanel(levadura());
        InsumoPanel margarina = calculadora.armarPanel(margarina());
        assertEquals(NivelAlerta.NORMAL, levadura.nivel());
        assertEquals(NivelAlerta.NORMAL, margarina.nivel());
        assertNumero("2.00", levadura.coberturaDias());
        assertNumero("1.50", margarina.coberturaDias());
        assertNumero("0", levadura.deficit());
    }

    @Test
    void existenciaIgualAlUmbralCuentaComoAlerta() {
        DatosInsumo datos = insumo("X", "10", "10", "8"); // cobertura 1.25
        assertEquals(NivelAlerta.ALERTA, calculadora.calcularNivel(datos));
        assertNumero("0", calculadora.calcularDeficit(datos));
    }

    @Test
    void existenciaApenasPorEncimaDelUmbralEsNormal() {
        assertEquals(NivelAlerta.NORMAL, calculadora.calcularNivel(insumo("X", "10.001", "10", "8")));
    }

    @Test
    void existenciaCeroEsCriticaAunSinConsumo() {
        DatosInsumo datos = insumo("X", "0", "10", null);
        assertEquals(NivelAlerta.CRITICO, calculadora.calcularNivel(datos));
        assertNumero("10", calculadora.calcularDeficit(datos));
    }

    @Test
    void consumoCeroNuloONegativoNoTieneCoberturaYQuedaEnAlerta() {
        for (String consumo : new String[] { "0", null, "-4" }) {
            DatosInsumo datos = insumo("X", "5", "10", consumo);
            assertNull(calculadora.calcularCobertura(datos.existencia(), datos.consumoDiario()));
            assertEquals(NivelAlerta.ALERTA, calculadora.calcularNivel(datos));
        }
    }

    @Test
    void decimalesDeMedioKiloFuncionan() {
        DatosInsumo datos = insumo("X", "0.5", "2", "0.4"); // cobertura 1.25
        assertEquals(NivelAlerta.ALERTA, calculadora.calcularNivel(datos));
        assertNumero("1.5", calculadora.calcularDeficit(datos));
        assertNumero("1.25", calculadora.calcularCobertura(datos.existencia(), datos.consumoDiario()));
    }

    @Test
    void existenciaNulaSeTrataComoCero() {
        DatosInsumo datos = insumo("X", null, "10", "4");
        assertEquals(NivelAlerta.CRITICO, calculadora.calcularNivel(datos));
        assertNumero("10", calculadora.calcularDeficit(datos));
        assertNumero("0", calculadora.armarPanel(datos).existencia());
    }

    @Test
    void existenciaNegativaSeTrataComoCero() {
        DatosInsumo datos = insumo("X", "-3", "10", "4");
        assertEquals(NivelAlerta.CRITICO, calculadora.calcularNivel(datos));
        assertNumero("10", calculadora.calcularDeficit(datos));
    }

    @Test
    void umbralCeroNuloONegativoSignificaSinUmbralYEsNormal() {
        for (String umbral : new String[] { "0", null, "-5" }) {
            DatosInsumo datos = insumo("X", "0", umbral, "4");
            assertEquals(NivelAlerta.NORMAL, calculadora.calcularNivel(datos));
            assertNumero("0", calculadora.calcularDeficit(datos));
        }
    }

    @Test
    void coberturaExactamenteEnElLimiteNoEsCritica() {
        DatosInsumo datos = insumo("X", "8", "10", "8"); // cobertura 1.00
        assertEquals(NivelAlerta.ALERTA, calculadora.calcularNivel(datos));
    }

    @Test
    void laCoberturaSeCompararedondeada() {
        // 9.95 / 10 = 0.995, que se redondea a 1.00 y por eso no es crítica
        DatosInsumo datos = insumo("X", "9.95", "10", "10");
        assertNumero("1.00", calculadora.calcularCobertura(datos.existencia(), datos.consumoDiario()));
        assertEquals(NivelAlerta.ALERTA, calculadora.calcularNivel(datos));
    }

    @Test
    void elLimiteDeDiasEsConfigurable() {
        DatosInsumo datos = insumo("X", "10", "10", "8"); // cobertura 1.25
        assertEquals(NivelAlerta.ALERTA, new CalculadoraAlerta(BigDecimal.ONE).calcularNivel(datos));
        assertEquals(NivelAlerta.CRITICO, new CalculadoraAlerta(new BigDecimal("2")).calcularNivel(datos));
    }
}