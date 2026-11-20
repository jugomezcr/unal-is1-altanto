package co.edu.unal.sweng1.altanto.presentacion.controlador;

import static org.hamcrest.Matchers.containsString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import co.edu.unal.sweng1.altanto.persistencia.entidad.EstadoPedido;
import co.edu.unal.sweng1.altanto.persistencia.repositorio.InsumoRepository;
import co.edu.unal.sweng1.altanto.persistencia.repositorio.PedidoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RecepcionPedidoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private InsumoRepository articuloRepository;

    @Test
    void listaPedidosPendientesYAbreElFormularioConOpcionDeCancelar() throws Exception {
        mockMvc.perform(get("/recepcion-pedidos/pendientes"))
                .andExpect(status().isOk())
                .andExpect(view().name("recepcion/pendientes"))
                .andExpect(content().string(containsString("Harinera del Valle S.A.")))
                .andExpect(content().string(containsString("/recepcion-pedidos/1")));

        mockMvc.perform(get("/recepcion-pedidos/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("recepcion/recibir"))
                .andExpect(content().string(containsString("Harina de Trigo Especial")))
                .andExpect(content().string(containsString("type=\"button\"")))
                .andExpect(content().string(containsString("Igualar a pedido")))
                .andExpect(content().string(containsString("data-cantidad-pedida=\"30.000\"")))
                .andExpect(content().string(containsString("data-cantidad-pedida=\"20.000\"")))
                .andExpect(content().string(containsString("Cancelar")))
                .andExpect(content().string(containsString("/recepcion-pedidos/pendientes")));

        assertThat(pedidoRepository.findById(1L).orElseThrow().getEstado())
                .isEqualTo(EstadoPedido.PENDIENTE);
        assertThat(articuloRepository.findById(1L).orElseThrow().getExistenciaActual())
                .isEqualByComparingTo("15.000");
    }

    @Test
    void confirmaRecepcionYRedirigeConMensaje() throws Exception {
        mockMvc.perform(post("/recepcion-pedidos/1")
                        .param("lineas[0].detallePedidoId", "1")
                        .param("lineas[0].cantidadRecibida", "2.500")
                        .param("lineas[1].detallePedidoId", "2")
                        .param("lineas[1].cantidadRecibida", "1.250"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/recepcion-pedidos/pendientes"))
                .andExpect(flash().attribute("mensaje", "Pedido recibido y cerrado."));

        assertThat(pedidoRepository.findById(1L).orElseThrow().getEstado())
                .isEqualTo(EstadoPedido.CERRADO);
        assertThat(articuloRepository.findById(1L).orElseThrow().getExistenciaActual())
                .isEqualByComparingTo("18.750");

        mockMvc.perform(post("/recepcion-pedidos/1")
                        .param("lineas[0].detallePedidoId", "1")
                        .param("lineas[0].cantidadRecibida", "2.500")
                        .param("lineas[1].detallePedidoId", "2")
                        .param("lineas[1].cantidadRecibida", "1.250"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute(
                        "error", "Este pedido ya fue recibido o está siendo procesado."));

        assertThat(articuloRepository.findById(1L).orElseThrow().getExistenciaActual())
                .isEqualByComparingTo("18.750");
    }

    @Test
    void formularioInvalidoMuestraErrorSinModificarExistencias() throws Exception {
        mockMvc.perform(post("/recepcion-pedidos/1")
                        .param("lineas[0].detallePedidoId", "1")
                        .param("lineas[0].cantidadRecibida", "")
                        .param("lineas[1].detallePedidoId", "2")
                        .param("lineas[1].cantidadRecibida", "0"))
                .andExpect(status().isOk())
                .andExpect(view().name("recepcion/recibir"))
                .andExpect(content().string(containsString("Digite una cantidad recibida no negativa")));

        assertThat(pedidoRepository.findById(1L).orElseThrow().getEstado())
                .isEqualTo(EstadoPedido.PENDIENTE);
        assertThat(articuloRepository.findById(1L).orElseThrow().getExistenciaActual())
                .isEqualByComparingTo("15.000");
    }
}
