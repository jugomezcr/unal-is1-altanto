package co.edu.unal.sweng1.altanto.negocio.servicio;

import co.edu.unal.sweng1.altanto.negocio.calculo.CalculadoraAlerta;
import co.edu.unal.sweng1.altanto.negocio.dto.DatosInsumo;
import co.edu.unal.sweng1.altanto.negocio.dto.InsumoPanel;
import co.edu.unal.sweng1.altanto.negocio.dto.NivelAlerta;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

// Arma el panel de alertas (CU-02-HU-01): pide los insumos a la fuente, deja los visibles,
// calcula el nivel de cada uno y los ordena por urgencia.
// @Service: Spring crea un solo objeto de esta clase y lo inyecta en el controlador.
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

    // Spring llama este constructor y le pasa:
    //  - fuente: el objeto que implementa FuenteInsumos (FuenteInsumosPersistencia).
    //  - @Value("${clave:valorPorDefecto}"): lee la clave de application.properties;
    //    si no está, usa lo que va después de los dos puntos.
    public AlertaInsumosService(
            FuenteInsumos fuente,
            @Value("${altanto.alertas.codigos-visibles:INS-HAR-01,INS-LEV-01,INS-MAR-01}") String codigosVisibles,
            @Value("${altanto.alertas.dias-critico:1}") BigDecimal diasCritico) {
        this.fuente = fuente;
        // "A,B, C" -> {"A","B","C"}: separa por comas, quita espacios y vacíos.
        this.codigosVisibles = Arrays.stream(codigosVisibles.split(","))
                .map(String::trim)
                .filter(codigo -> !codigo.isEmpty())
                .collect(Collectors.toSet());
        // La calculadora no es un bean de Spring: es una clase normal que se crea con new.
        this.calculadora = new CalculadoraAlerta(diasCritico);
    }

    // Filas del panel, ya calculadas y ordenadas. Lista vacía si la fuente no trae nada.
    public List<InsumoPanel> obtenerPanel() {
        List<DatosInsumo> todos = fuente.obtenerInsumos();
        if (todos == null) {
            return List.of();
        }
        // El filtro de visibles va aquí y no en la fuente: es una regla de negocio (qué se
        // muestra), no de acceso a datos. Así cualquier fuente (BD o memoria en pruebas)
        // recibe el mismo filtro y la consulta SQL no depende de la configuración.
        // stream(): recorre la lista como un pipeline (filter = if, map = transformar).
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
