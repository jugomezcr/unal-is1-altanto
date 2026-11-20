package co.edu.unal.sweng1.altanto.persistencia.repositorio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.unal.sweng1.altanto.persistencia.entidad.EstadoPedido;
import co.edu.unal.sweng1.altanto.persistencia.entidad.Pedido;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

@DataJpaTest
class PedidoRepositoryTest {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private DetallePedidoRepository detallePedidoRepository;

    @Test
    void encuentraPedidosPendientesYConservaLineasRepetidas() {
        assertThat(pedidoRepository.findByEstadoOrderByFechaPedidoAscIdAsc(EstadoPedido.PENDIENTE))
                .extracting(Pedido::getId)
                .contains(1L);

        assertThat(detallePedidoRepository.findByPedidoIdOrderByIdAsc(1L))
                .extracting(detalle -> detalle.getId())
                .containsExactly(1L, 2L);
    }

    @Test
    void cerrarIncrementaVersionYRechazaCerrarDeNuevo() {
        Pedido pedido = pedidoRepository.findById(1L).orElseThrow();
        assertThat(pedido.getVersion()).isZero();

        pedido.cerrar(LocalDate.now());
        pedidoRepository.saveAndFlush(pedido);

        assertThat(pedido.getVersion()).isEqualTo(1L);
        assertThatThrownBy(() -> pedido.cerrar(LocalDate.now()))
                .hasMessage("Este pedido ya fue recibido o está siendo procesado.");
    }
}
