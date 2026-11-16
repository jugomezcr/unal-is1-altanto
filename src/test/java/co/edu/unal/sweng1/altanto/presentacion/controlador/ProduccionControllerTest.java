package co.edu.unal.sweng1.altanto.presentacion.controlador;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import co.edu.unal.sweng1.altanto.persistencia.repositorio.InsumoRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

/**
 * Prueba WEB del CU-03: MockMvc simula el navegador (como el TestClient de FastAPI) y
 * revisa la respuesta: código HTTP, plantilla usada y HTML generado.
 *
 * <p>La prueba {@code registrarTandaDescuentaYMuestraElResumen} recorre el flujo COMPLETO
 * de punta a punta: pide el formulario, lo envía, sigue la redirección hasta el resumen y
 * comprueba en la base que el insumo quedó descontado. Es nuestra prueba "end to end"
 * usando solo lo que el proyecto ya usa (sin navegador real ni dependencias nuevas).</p>
 *
 * <p>{@code @Transactional}: al terminar cada prueba se deshace lo guardado, así no se
 * afectan entre sí. En data.sql la harina (id 1) tiene 15 kg y el Pan Francés (id 10) es
 * un producto elaborado.</p>
 */
@SpringBootTest        // arranca la aplicación completa
@AutoConfigureMockMvc  // prepara el MockMvc
@Transactional
class ProduccionControllerTest {

    private static final Long HARINA = 1L;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private InsumoRepository articuloRepository;

    @Test
    void elFormularioMuestraLosInsumos() throws Exception {
        mockMvc.perform(get("/produccion/tandas"))
                .andExpect(status().isOk())                                               // HTTP 200
                .andExpect(view().name("produccion/nueva-tanda"))                         // plantilla
                .andExpect(content().string(containsString("Harina de Trigo Especial"))); // un insumo de data.sql
    }

    @Test
    void registrarTandaDescuentaYMuestraElResumen() throws Exception {
        // 1. GET del formulario.
        mockMvc.perform(get("/produccion/tandas")).andExpect(status().isOk());

        // 2. POST válido: hornear Pan Francés gastando 10 kg de harina (había 15).
        MvcResult resultado = mockMvc.perform(post("/produccion/tandas")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("fecha", "2026-11-03")
                        .param("articuloProductoId", "10")
                        .param("harinaBaseKg", "10")
                        .param("unidadesObtenidas", "120")
                        .param("maestroPanadero", "Leonardo Betancur")
                        .param("observaciones", "Tanda de prueba web")
                        .param("lineas[0].insumoId", "1")
                        .param("lineas[0].cantidad", "10"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/produccion/tandas/*"))
                .andReturn();

        // 3. Seguir la redirección hasta el resumen.
        String urlResumen = resultado.getResponse().getRedirectedUrl();
        mockMvc.perform(get(urlResumen))
                .andExpect(status().isOk())
                .andExpect(view().name("produccion/resumen"))
                .andExpect(content().string(containsString("Harina de Trigo Especial")));

        // 4. La harina quedó descontada: 15 - 10 = 5.
        assertThat(articuloRepository.findById(HARINA).orElseThrow().getExistenciaActual())
                .isEqualByComparingTo(new BigDecimal("5"));
    }

    @Test
    void siUnInsumoNoAlcanzaVuelveAlFormularioConElAviso() throws Exception {
        // La harina tiene 15 kg; se piden 20. No alcanza.
        mockMvc.perform(post("/produccion/tandas")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("fecha", "2026-11-03")
                        .param("articuloProductoId", "10")
                        .param("harinaBaseKg", "20")
                        .param("unidadesObtenidas", "120")
                        .param("maestroPanadero", "Leonardo Betancur")
                        .param("lineas[0].insumoId", "1")
                        .param("lineas[0].cantidad", "20"))
                .andExpect(status().isOk())                                      // vuelve al formulario
                .andExpect(view().name("produccion/nueva-tanda"))
                .andExpect(content().string(containsString("Harina de Trigo Especial")));

        // No se descontó nada: la harina sigue en 15.
        assertThat(articuloRepository.findById(HARINA).orElseThrow().getExistenciaActual())
                .isEqualByComparingTo(new BigDecimal("15"));
    }
}
