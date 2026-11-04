package co.edu.unal.sweng1.altanto.presentacion;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Pruebas de las pantallas del CU-01 con <b>MockMvc</b>: simula peticiones del
 * navegador (GET y POST) sin abrir un navegador real y revisa la respuesta (código
 * HTTP, plantilla usada y HTML generado). También prueba que las plantillas Thymeleaf
 * se pintan sin errores.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ConciliacionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void formularioMuestraInsumosYReventaPeroNoPanElaborado() throws Exception {
        mockMvc.perform(get("/conciliacion"))
                .andExpect(status().isOk())
                .andExpect(view().name("conciliacion/nueva"))
                .andExpect(content().string(containsString("Harina de Trigo Especial")))
                .andExpect(content().string(containsString("Kumis Artesanal 250ml")))
                .andExpect(content().string(not(containsString("Pan Francés Tradicional"))));
    }

    @Test
    void guardarRedirigeALasDiferencias() throws Exception {
        // param(...) simula lo que el navegador envía al pulsar "Guardar".
        mockMvc.perform(post("/conciliacion")
                        .param("fecha", "2026-11-03")
                        .param("totalVentasZ", "120000")
                        .param("lineas[0].articuloId", "1")
                        .param("lineas[0].contado", "14"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/conciliacion/*"));
    }

    @Test
    void fechaDuplicadaOfreceVerLaExistente() throws Exception {
        mockMvc.perform(post("/conciliacion")
                        .param("fecha", "2026-11-02")
                        .param("totalVentasZ", "0")
                        .param("lineas[0].articuloId", "1")
                        .param("lineas[0].contado", "14"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("existenteId", 2L))
                .andExpect(content().string(containsString("Ver la existente")));
    }

    @Test
    void contadoVacioMuestraErrorDeValidacion() throws Exception {
        mockMvc.perform(post("/conciliacion")
                        .param("fecha", "2026-11-03")
                        .param("totalVentasZ", "0")
                        .param("lineas[0].articuloId", "1")
                        .param("lineas[0].contado", ""))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Digite lo contado.")));
    }

    @Test
    void historialMuestraLosCierresPreviosConEnlaceAlDetalle() throws Exception {
        mockMvc.perform(get("/conciliacion/historial"))
                .andExpect(status().isOk())
                .andExpect(view().name("conciliacion/historial"))
                .andExpect(content().string(containsString("01/11/2026")))
                .andExpect(content().string(containsString("02/11/2026")))
                .andExpect(content().string(containsString("/conciliacion/1\"")));
    }

    @Test
    void resumenMuestraLasLineasYUnaConciliacionInexistenteDa404() throws Exception {
        mockMvc.perform(get("/conciliacion/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("conciliacion/resumen"))
                .andExpect(content().string(containsString("CONFIRMADA")));

        mockMvc.perform(get("/conciliacion/999"))
                .andExpect(status().isNotFound());
    }
}
