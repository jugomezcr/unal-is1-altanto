package co.edu.unal.sweng1.altanto.presentacion.controlador;

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
 * Prueba WEB: MockMvc simula el navegador (como el TestClient de FastAPI) y revisa la
 * respuesta: código HTTP, plantilla usada y HTML generado.
 */
@SpringBootTest        // arranca la aplicación completa
@AutoConfigureMockMvc  // prepara el MockMvc
class ConciliacionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void elFormularioMuestraLosArticulosAConciliar() throws Exception {
        mockMvc.perform(get("/conciliacion"))
                .andExpect(status().isOk())                                             // HTTP 200
                .andExpect(view().name("conciliacion/nueva"))                           // plantilla
                .andExpect(content().string(containsString("Harina de Trigo Especial"))); // un insumo de data.sql
    }
}
