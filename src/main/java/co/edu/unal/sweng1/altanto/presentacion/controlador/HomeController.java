package co.edu.unal.sweng1.altanto.presentacion.controlador;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Página de inicio y rutas provisionales de módulos que todavía no tienen controlador.
 *
 * <p>Regla de Spring: cada URL la atiende UN solo método en toda la aplicación. Si dos
 * controladores declaran la misma ruta, la aplicación no arranca.</p>
 */
@Controller
public class HomeController {

	/** GET / → {@code templates/index.html}, con el título en {@code ${titulo}}. */
	@GetMapping("/")
	public String index(Model model) {
		model.addAttribute("titulo", "Al-Tanto — Panadería Leonardo Betancur");
		return "index";
	}

	// /conciliacion la atiende ConciliacionController (CU-01) y /inventario/alertas,
	// InventarioAlertasController (CU-02). Por la regla de arriba, ya no están aquí.

	/**
	 * GET /produccion/tandas: página "en construcción" de CU-03. Se borra de aquí cuando
	 * CU-03-HU-01 tenga su propio controlador.
	 */
	@GetMapping("/produccion/tandas")
	public String produccionTandas() {
		return "produccion/nueva-tanda";
	}

}
