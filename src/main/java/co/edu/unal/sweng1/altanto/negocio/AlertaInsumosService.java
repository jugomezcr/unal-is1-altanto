package co.edu.unal.sweng1.altanto.negocio;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

// Arma el panel: pide los insumos a la fuente, deja los visibles, calcula y ordena.
@Service
public class AlertaInsumosService {

    // críticos primero, luego alerta, luego normales; dentro de cada nivel,
    // mayor déficit primero y después por nombre
    private static final Comparator<InsumoPanel> ORDEN = Comparator
            .comparing(InsumoPanel::nivel)
            .thenComparing(InsumoPanel::deficit, Comparator.<BigDecimal>reverseOrder())
            .thenComparing(InsumoPanel::nombre, Comparator.nullsLast(Comparator.<String>naturalOrder()));

    private final FuenteInsumos fuente;               // de dónde salen los insumos
    private final Set<String> codigosVisibles;        // códigos que se muestran en el panel
    private final CalculadoraAlerta calculadora;      // reglas de nivel, déficit y cobertura

    public AlertaInsumosService(
            FuenteInsumos fuente,
            @Value("${altanto.alertas.codigos-visibles:INS-HAR-01,INS-LEV-01,INS-MAR-01}") String codigosVisibles,
            @Value("${altanto.alertas.dias-critico:1}") BigDecimal diasCritico) {
        this.fuente = fuente;
        this.codigosVisibles = Arrays.stream(codigosVisibles.split(","))
                .map(String::trim)
                .filter(codigo -> !codigo.isEmpty())
                .collect(Collectors.toSet());
        this.calculadora = new CalculadoraAlerta(diasCritico);
    }

    public List<InsumoPanel> obtenerPanel() {
        List<DatosInsumo> todos = fuente.obtenerInsumos();
        if (todos == null) {
            return List.of();
        }
        // TODO-COMENTARIO: explica por qué el filtro de visibles se hace aquí y no en la fuente
        return todos.stream()
                .filter(datos -> datos != null && codigosVisibles.contains(datos.codigo()))
                .map(calculadora::armarPanel)
                .sorted(ORDEN)
                .toList();
    }

    // true si al menos un insumo del panel no está NORMAL
    public boolean hayAlertas(List<InsumoPanel> panel) {
        return panel.stream().anyMatch(insumo -> insumo.nivel() != NivelAlerta.NORMAL);
    }
}
