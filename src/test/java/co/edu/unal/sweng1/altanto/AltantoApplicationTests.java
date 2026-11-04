package co.edu.unal.sweng1.altanto;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Pruebas "de humo": la aplicación arranca y cada módulo responde. Si alguna falla, hay
 * un problema de configuración (rutas duplicadas, entidad que no coincide con
 * schema.sql, plantilla rota...).
 */
@SpringBootTest        // arranca la aplicación completa con H2 en memoria
@AutoConfigureMockMvc  // prepara el MockMvc (navegador simulado)
class AltantoApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	/** Vacía a propósito: si Spring no logra arrancar, esta prueba falla. */
	@Test
	void contextLoads() {
	}

	/** La página de inicio muestra la marca y los tres módulos. */
	@Test
	void paginaInicioRespondeConBienvenidaAlTanto() throws Exception {
		mockMvc.perform(get("/"))
				.andExpect(status().isOk())
				.andExpect(view().name("index"))
				.andExpect(content().string(containsString("Al-Tanto")))
				.andExpect(content().string(containsString("Leonardo Betancur")))
				.andExpect(content().string(containsString("Conciliación Diaria")))
				.andExpect(content().string(containsString("Inventario & Alertas")))
				.andExpect(content().string(containsString("Registro de Producción")));
	}

	/** La ruta principal de cada módulo responde 200 con su plantilla. */
	@Test
	void rutasInicialesDeModulosRespondenExitosamente() throws Exception {
		mockMvc.perform(get("/conciliacion"))
				.andExpect(status().isOk())
				.andExpect(view().name("conciliacion/nueva"));

		mockMvc.perform(get("/inventario/alertas"))
				.andExpect(status().isOk())
				.andExpect(view().name("inventario/alertas"));

		mockMvc.perform(get("/produccion/tandas"))
				.andExpect(status().isOk())
				.andExpect(view().name("produccion/nueva-tanda"));
	}

}
