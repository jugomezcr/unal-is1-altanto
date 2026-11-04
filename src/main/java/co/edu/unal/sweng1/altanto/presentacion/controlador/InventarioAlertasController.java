package co.edu.unal.sweng1.altanto.presentacion.controlador;

import co.edu.unal.sweng1.altanto.negocio.dto.InsumoPanel;
import co.edu.unal.sweng1.altanto.negocio.servicio.AlertaInsumosService;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class InventarioAlertasController {

    private final AlertaInsumosService alertaInsumosService;

    public InventarioAlertasController(
            AlertaInsumosService alertaInsumosService) {
        this.alertaInsumosService = alertaInsumosService;
    }

    @GetMapping("/inventario/alertas")
    public String mostrarAlertas(Model model) {
        List<InsumoPanel> panel = alertaInsumosService.obtenerPanel();

        model.addAttribute("insumos", panel);
        model.addAttribute("hayAlertas",
                alertaInsumosService.hayAlertas(panel));

        return "inventario/alertas";
    }
}
