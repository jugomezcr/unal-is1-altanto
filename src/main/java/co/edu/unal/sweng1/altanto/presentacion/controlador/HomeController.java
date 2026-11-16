package co.edu.unal.sweng1.altanto.presentacion.controlador;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Página de inicio de la aplicación (GET /).
 *
 * <p>Regla de Spring: cada URL la atiende UN solo método en toda la aplicación. Si dos
 * controladores declaran la misma ruta, la aplicación no arranca. Por eso cada módulo
 * tiene su propio controlador (conciliación, inventario, producción).</p>
 */
@Controller
public class HomeController {

	/** GET / → {@code templates/index.html}, con el título en {@code ${titulo}}. */
	@GetMapping("/")
	public String index(Model model) {
		model.addAttribute("titulo", "Al-Tanto — Panadería Leonardo Betancur");
		return "index";
	}

	// /conciliacion la atiende ConciliacionController (CU-01), /inventario/alertas,
	// InventarioAlertasController (CU-02), y /produccion/tandas, ProduccionController
	// (CU-03). Por la regla de arriba, ya no están aquí.

}
