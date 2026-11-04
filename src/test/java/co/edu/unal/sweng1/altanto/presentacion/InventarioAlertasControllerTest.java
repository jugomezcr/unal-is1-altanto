
package co.edu.unal.sweng1.altanto.presentacion;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class InventarioAlertasControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void mostrarAlertasDebeCargarLaVistaYElModelo() throws Exception {
        mockMvc.perform(get("/inventario/alertas"))
                .andExpect(status().isOk())
                .andExpect(view().name("inventario/alertas"))
                .andExpect(model().attributeExists("insumos", "hayAlertas"));
    }
}
