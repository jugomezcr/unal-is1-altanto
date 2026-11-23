package co.edu.unal.sweng1.altanto.presentacion.controlador;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import co.edu.unal.sweng1.altanto.persistencia.repositorio.CompraEmergenciaRepository;
import co.edu.unal.sweng1.altanto.persistencia.repositorio.InsumoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

/**
 * Prueba WEB de CU-04-HU-02 (MockMvc simula el navegador, como el TestClient de FastAPI).
 * Recorre el flujo completo: formulario → ver sobrecosto → confirmar → resumen, y comprueba
 * que "ver el sobrecosto" no guarda nada (equivale a cancelar).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CompraEmergenciaControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private InsumoRepository articuloRepository;
    @Autowired
    private CompraEmergenciaRepository compraRepository;

    @Test
    void recorreElFlujoYSoloConfirmarGuarda() throws Exception {
        long antes = compraRepository.count();

        // 1. El formulario carga (y la ruta literal /emergencia gana a /{pedidoId}).
        mockMvc.perform(get("/recepcion-pedidos/emergencia"))
                .andExpect(status().isOk())
                .andExpect(view().name("recepcion/emergencia"))
                .andExpect(content().string(containsString("Harina de Trigo Especial")));

        // 2. Ver el sobrecosto: muestra 8000.00 y 25.00 %, pero NO guarda ni suma nada.
        mockMvc.perform(post("/recepcion-pedidos/emergencia/revisar")
                        .param("articuloId", "1")
                        .param("cantidadRecibida", "10")
                        .param("precioPagado", "4000")
                        .param("precioHabitual", "3200"))
                .andExpect(status().isOk())
                .andExpect(view().name("recepcion/emergencia-revisar"))
                .andExpect(content().string(containsString("8000.00")))
                .andExpect(content().string(containsString("25.00 %")));
        assertThat(compraRepository.count()).isEqualTo(antes);
        assertThat(articuloRepository.findById(1L).orElseThrow().getExistenciaActual())
                .isEqualByComparingTo("15");

        // 3. Confirmar: guarda, suma existencias y redirige al resumen.
        MvcResult resultado = mockMvc.perform(post("/recepcion-pedidos/emergencia")
                        .param("articuloId", "1")
                        .param("cantidadRecibida", "10")
                        .param("precioPagado", "4000")
                        .param("precioHabitual", "3200"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/recepcion-pedidos/emergencia/*"))
                .andReturn();

        // 4. El resumen muestra el sobrecosto y la harina quedó en 25 kg.
        mockMvc.perform(get(resultado.getResponse().getRedirectedUrl()))
                .andExpect(status().isOk())
                .andExpect(view().name("recepcion/emergencia-resumen"))
                .andExpect(content().string(containsString("8000.00")));
        assertThat(compraRepository.count()).isEqualTo(antes + 1);
        assertThat(articuloRepository.findById(1L).orElseThrow().getExistenciaActual())
                .isEqualByComparingTo("25");
    }
}
