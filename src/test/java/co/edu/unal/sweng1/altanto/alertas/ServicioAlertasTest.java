package co.edu.unal.sweng1.altanto.alertas;

import static co.edu.unal.sweng1.altanto.alertas.DatosAlertasPrueba.*;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import co.edu.unal.sweng1.altanto.negocio.AlertaInsumosService;
import co.edu.unal.sweng1.altanto.negocio.DatosInsumo;
import co.edu.unal.sweng1.altanto.negocio.InsumoPanel;
import co.edu.unal.sweng1.altanto.negocio.NivelAlerta;

class ServicioAlertasTest {

    private static final String VISIBLES_POR_DEFECTO = "INS-HAR-01,INS-LEV-01,INS-MAR-01";

    private AlertaInsumosService servicio(List<DatosInsumo> datos, String visibles) {
        return new AlertaInsumosService(new FuenteEnMemoria(datos), visibles, BigDecimal.ONE);
    }

    private List<String> nombres(List<InsumoPanel> panel) {
        return panel.stream().map(InsumoPanel::nombre).toList();
    }

    @Test
    void listaVaciaDevuelvePanelVacioYSinAlertas() {
        AlertaInsumosService servicio = servicio(List.of(), VISIBLES_POR_DEFECTO);
        List<InsumoPanel> panel = servicio.obtenerPanel();
        assertTrue(panel.isEmpty());
        assertFalse(servicio.hayAlertas(panel));
    }

    @Test
    void fuenteQueDevuelveNullDaPanelVacio() {
        // la interfaz tiene un solo método, así que una lambda hace de fuente
        AlertaInsumosService servicio = new AlertaInsumosService(() -> null, VISIBLES_POR_DEFECTO, BigDecimal.ONE);
        assertTrue(servicio.obtenerPanel().isEmpty());
    }

    @Test
    void ningunInsumoEnAlertaMuestraTodosNormalesYSinAlertas() {
        AlertaInsumosService servicio = servicio(List.of(levadura(), margarina()), VISIBLES_POR_DEFECTO);
        List<InsumoPanel> panel = servicio.obtenerPanel();
        assertEquals(2, panel.size());
        assertTrue(panel.stream().allMatch(fila -> fila.nivel() == NivelAlerta.NORMAL));
        assertFalse(servicio.hayAlertas(panel));
    }

    @Test
    void todosEnAlertaSeMuestranYHayAlertas() {
        List<DatosInsumo> datos = List.of(
                insumo("X1", "0", "10", "4"),    // crítico
                insumo("X2", "8", "10", "4"),    // alerta (cobertura 2)
                insumo("X3", "3", "10", "4"));   // crítico (cobertura 0.75)
        AlertaInsumosService servicio = servicio(datos, "X1,X2,X3");
        List<InsumoPanel> panel = servicio.obtenerPanel();
        assertEquals(3, panel.size());
        assertTrue(panel.stream().noneMatch(fila -> fila.nivel() == NivelAlerta.NORMAL));
        assertTrue(servicio.hayAlertas(panel));
    }

    @Test
    void datosSemillaMuestraSoloLosTresVisiblesYElAzucarQuedaFuera() {
        // el azúcar está en alerta, pero no está en la lista de visibles
        List<InsumoPanel> panel = servicio(datosSemilla(), VISIBLES_POR_DEFECTO).obtenerPanel();
        assertEquals(3, panel.size());
        assertTrue(nombres(panel).stream().noneMatch(nombre -> nombre.contains("Azúcar")));
        assertTrue(nombres(panel).stream().noneMatch(nombre -> nombre.contains("Queso")));
        assertEquals(NivelAlerta.CRITICO, panel.get(0).nivel());
        assertEquals("Harina de Trigo Especial", panel.get(0).nombre());
    }

    @Test
    void agregarElAzucarEsSoloAgregarSuCodigoALaLista() {
        String visibles = VISIBLES_POR_DEFECTO + ",INS-AZU-01";
        List<InsumoPanel> panel = servicio(datosSemilla(), visibles).obtenerPanel();
        assertEquals(4, panel.size());
        assertEquals("Azúcar Blanca Refinada", panel.get(1).nombre());
        assertEquals(NivelAlerta.ALERTA, panel.get(1).nivel());
    }

    @Test
    void listaDeVisiblesAceptaEspaciosYSeIgnoranVacios() {
        List<InsumoPanel> panel = servicio(datosSemilla(), " INS-HAR-01 , ,INS-LEV-01 ").obtenerPanel();
        assertEquals(2, panel.size());
    }

    @Test
    void sinCodigosVisiblesElPanelQuedaVacio() {
        assertTrue(servicio(datosSemilla(), "").obtenerPanel().isEmpty());
    }

    @Test
    void ordenCriticosLuegoAlertaLuegoNormalesConMayorDeficitPrimero() {
        List<DatosInsumo> datos = List.of(
                insumo("A", "50", "10", "4"),  // normal
                insumo("B", "8", "10", "4"),   // alerta, déficit 2
                insumo("C", "3", "10", "4"),   // crítico, déficit 7
                insumo("D", "0", "10", "4"),   // crítico, déficit 10
                insumo("E", "5", "10", "1"));  // alerta, déficit 5
        List<InsumoPanel> panel = servicio(datos, "A,B,C,D,E").obtenerPanel();
        assertEquals(List.of("Insumo D", "Insumo C", "Insumo E", "Insumo B", "Insumo A"), nombres(panel));
    }

    @Test
    void conIgualDeficitDesempataPorNombre() {
        List<DatosInsumo> datos = List.of(
                insumo("G", "8", "10", "4"),
                insumo("F", "8", "10", "4"));
        List<InsumoPanel> panel = servicio(datos, "F,G").obtenerPanel();
        assertEquals(List.of("Insumo F", "Insumo G"), nombres(panel));
    }

    @Test
    void insumoConUmbralCeroQuedaNormalYAlFinal() {
        List<DatosInsumo> datos = List.of(
                insumo("A", "0", "0", "4"),   // sin umbral: normal
                insumo("B", "2", "10", "4")); // crítico
        List<InsumoPanel> panel = servicio(datos, "A,B").obtenerPanel();
        assertEquals(List.of("Insumo B", "Insumo A"), nombres(panel));
        assertEquals(NivelAlerta.NORMAL, panel.get(1).nivel());
    }

    @Test
    void valoresNulosONegativosNoRompenElPanel() {
        List<DatosInsumo> datos = new ArrayList<>();
        datos.add(null);                                  // registro nulo: se ignora
        datos.add(insumo(null, "5", "10", "4"));          // sin código: no es visible
        datos.add(insumo("A", null, "10", "4"));          // existencia nula: crítico
        datos.add(insumo("B", "-2", "10", "4"));          // existencia negativa: crítico
        datos.add(insumo("C", "5", null, "4"));           // umbral nulo: normal
        List<InsumoPanel> panel = servicio(datos, "A,B,C").obtenerPanel();
        assertEquals(3, panel.size());
        assertEquals(NivelAlerta.CRITICO, panel.get(0).nivel());
        assertEquals(NivelAlerta.CRITICO, panel.get(1).nivel());
        assertEquals(NivelAlerta.NORMAL, panel.get(2).nivel());
    }

    @Test
    void muchosRegistrosSeOrdenanBien() {
        List<DatosInsumo> datos = new ArrayList<>();
        StringBuilder codigos = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            String codigo = "INS-" + i;
            String existencia = switch (i % 3) {
                case 0 -> "50";  // normal
                case 1 -> "8";   // alerta (cobertura 2)
                default -> "0";  // crítico
            };
            datos.add(insumo(codigo, existencia, "10", "4"));
            if (i > 0) {
                codigos.append(",");
            }
            codigos.append(codigo);
        }
        List<InsumoPanel> panel = servicio(datos, codigos.toString()).obtenerPanel();

        assertEquals(1000, panel.size());
        // 333 críticos, 333 en alerta y 334 normales, en ese orden
        assertTrue(panel.subList(0, 333).stream().allMatch(fila -> fila.nivel() == NivelAlerta.CRITICO));
        assertTrue(panel.subList(333, 666).stream().allMatch(fila -> fila.nivel() == NivelAlerta.ALERTA));
        assertTrue(panel.subList(666, 1000).stream().allMatch(fila -> fila.nivel() == NivelAlerta.NORMAL));
    }

    @Test
    void elLimiteDeDiasDelServicioEsConfigurable() {
        List<DatosInsumo> datos = List.of(insumo("A", "10", "10", "8")); // cobertura 1.25
        AlertaInsumosService conLimite2 = new AlertaInsumosService(new FuenteEnMemoria(datos), "A", new BigDecimal("2"));
        assertEquals(NivelAlerta.CRITICO, conLimite2.obtenerPanel().get(0).nivel());
    }
}