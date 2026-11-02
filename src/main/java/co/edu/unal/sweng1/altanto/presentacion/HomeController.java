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

	@GetMapping("/conciliacion")
	public String conciliacion() {
		return "conciliacion/nueva";
	}

	@GetMapping("/inventario/alertas")
	public String inventarioAlertas() {
		return "inventario/alertas";
	}

	@GetMapping("/produccion/tandas")
	public String produccionTandas() {
		return "produccion/nueva-tanda";
	}

}
