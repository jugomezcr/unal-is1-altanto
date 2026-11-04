package co.edu.unal.sweng1.altanto.negocio.calculo;

import co.edu.unal.sweng1.altanto.negocio.dto.DatosInsumo;
import co.edu.unal.sweng1.altanto.negocio.dto.InsumoPanel;
import co.edu.unal.sweng1.altanto.negocio.dto.NivelAlerta;
import java.math.BigDecimal;
import java.math.RoundingMode;

// Reglas puras de alerta. No usa Spring ni base de datos, por eso es fácil de probar.
public class CalculadoraAlerta {

    // límite de días de cobertura por debajo del cual un insumo en alerta es crítico
    private final BigDecimal limiteDiasCritico;

    public CalculadoraAlerta(BigDecimal limiteDiasCritico) {
        this.limiteDiasCritico = (limiteDiasCritico == null) ? BigDecimal.ONE : limiteDiasCritico;
    }

    // Existencia nula o negativa se trata como 0 (no hay nada en bodega).
    public BigDecimal existenciaEfectiva(BigDecimal existencia) {
        if (existencia == null || existencia.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }
        return existencia;
    }

    // Un umbral nulo, 0 o negativo significa "sin umbral definido": nunca hay alerta.
    public boolean tieneUmbral(BigDecimal umbral) {
        return umbral != null && umbral.compareTo(BigDecimal.ZERO) > 0;
    }

    // Cobertura en días = existencia / consumo diario, con 2 decimales.
    // Sin consumo (nulo, 0 o negativo) no se puede calcular y devuelve null.
    public BigDecimal calcularCobertura(BigDecimal existencia, BigDecimal consumoDiario) {
        if (consumoDiario == null || consumoDiario.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }
        return existenciaEfectiva(existencia).divide(consumoDiario, 2, RoundingMode.HALF_UP);
    }

    public NivelAlerta calcularNivel(DatosInsumo datos) {
        if (!tieneUmbral(datos.umbral())) {
            return NivelAlerta.NORMAL;
        }
        BigDecimal existencia = existenciaEfectiva(datos.existencia());

        // TODO-COMENTARIO: explica por qué se usa compareTo y no equals con BigDecimal
        boolean enAlerta = existencia.compareTo(datos.umbral()) <= 0;
        if (!enAlerta) {
            return NivelAlerta.NORMAL;
        }
        if (existencia.compareTo(BigDecimal.ZERO) <= 0) {
            return NivelAlerta.CRITICO;
        }

        // TODO-COMENTARIO: explica por qué se compara la cobertura ya redondeada con "<" estricto
        BigDecimal cobertura = calcularCobertura(existencia, datos.consumoDiario());
        if (cobertura != null && cobertura.compareTo(limiteDiasCritico) < 0) {
            return NivelAlerta.CRITICO;
        }
        return NivelAlerta.ALERTA;
    }

    // Déficit = umbral - existencia solo si hay alerta; en NORMAL es 0 (nunca negativo).
    public BigDecimal calcularDeficit(DatosInsumo datos) {
        if (calcularNivel(datos) == NivelAlerta.NORMAL) {
            return BigDecimal.ZERO;
        }
        BigDecimal deficit = datos.umbral().subtract(existenciaEfectiva(datos.existencia()));
        return deficit.max(BigDecimal.ZERO);
    }

    // Convierte los datos crudos en la fila que ve la vista.
    public InsumoPanel armarPanel(DatosInsumo datos) {
        BigDecimal umbral = (datos.umbral() == null) ? BigDecimal.ZERO : datos.umbral();
        return new InsumoPanel(
                datos.nombre(),
                datos.unidad(),
                existenciaEfectiva(datos.existencia()),
                umbral,
                calcularDeficit(datos),
                calcularCobertura(datos.existencia(), datos.consumoDiario()),
                calcularNivel(datos));
    }
}