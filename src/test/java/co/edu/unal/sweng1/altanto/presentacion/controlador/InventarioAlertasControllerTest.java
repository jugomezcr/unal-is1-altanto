package co.edu.unal.sweng1.altanto.presentacion.controlador;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Prueba web del panel de alertas (CU-02-HU-01): la ruta responde, usa la plantilla
 * correcta y le pasa los datos que necesita. MockMvc simula el navegador.
 */
@SpringBootTest        // arranca la aplicación completa con H2 en memoria
@AutoConfigureMockMvc  // prepara el MockMvc que se inyecta abajo
class InventarioAlertasControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void mostrarAlertasDebeCargarLaVistaYElModelo() throws Exception {
        mockMvc.perform(get("/inventario/alertas"))
                .andExpect(status().isOk())                                // HTTP 200
                .andExpect(view().name("inventario/alertas"))              // plantilla usada
                .andExpect(model().attributeExists("insumos", "hayAlertas")); // datos para la vista
    }
}
