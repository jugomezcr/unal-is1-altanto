package co.edu.unal.sweng1.altanto.presentacion.controlador;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import co.edu.unal.sweng1.altanto.negocio.dto.InsumoPanel;
import co.edu.unal.sweng1.altanto.negocio.servicio.AlertaInsumosService;

/**
 * Pantalla del panel de alertas de insumos (CU-02-HU-01).
 *
 * <p>{@code @Controller}: atiende peticiones del navegador y devuelve el nombre de una
 * plantilla Thymeleaf. No calcula nada: le pide el panel al servicio y lo pasa a la vista.</p>
 */
@Controller
public class InventarioAlertasController {

    private final AlertaInsumosService alertaInsumosService;

    /** Spring inyecta el servicio por constructor. */
    public InventarioAlertasController(
            AlertaInsumosService alertaInsumosService) {
        this.alertaInsumosService = alertaInsumosService;
    }

    /**
     * GET /inventario/alertas → {@code templates/inventario/alertas.html}.
     * En la plantilla, {@code ${insumos}} es la lista de filas y {@code ${hayAlertas}}
     * decide si se muestra el aviso.
     */
    @GetMapping("/inventario/alertas")
    public String mostrarAlertas(Model model) {
        List<InsumoPanel> panel = alertaInsumosService.obtenerPanel();

        model.addAttribute("insumos", panel);
        model.addAttribute("hayAlertas",
                alertaInsumosService.hayAlertas(panel));

        return "inventario/alertas";
    }
}
