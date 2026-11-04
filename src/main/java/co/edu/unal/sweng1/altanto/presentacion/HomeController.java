package co.edu.unal.sweng1.altanto.presentacion;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

	@GetMapping("/")
	public String index(Model model) {
		model.addAttribute("titulo", "Al-Tanto — Panadería Leonardo Betancur");
		return "index";
	}

	// La ruta /conciliacion la atiende ConciliacionController (CU-01-HU-01 y HU-02).
	// Si estuviera aquí también, Spring no arrancaría por tener dos métodos para la misma URL.

	// Provisional route: CU-02-HU-01 will replace it with its own controller.
	@GetMapping("/inventario/alertas")
	public String inventarioAlertas() {
		return "inventario/alertas";
	}

}
