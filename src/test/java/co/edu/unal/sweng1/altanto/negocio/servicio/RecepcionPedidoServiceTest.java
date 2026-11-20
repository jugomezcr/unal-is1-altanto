package co.edu.unal.sweng1.altanto.negocio.servicio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.unal.sweng1.altanto.negocio.dto.CantidadRecibidaPedido;
import co.edu.unal.sweng1.altanto.negocio.excepcion.PedidoNoEncontradoException;
import co.edu.unal.sweng1.altanto.negocio.excepcion.ReglaDeNegocioException;
import co.edu.unal.sweng1.altanto.persistencia.entidad.Articulo;
import co.edu.unal.sweng1.altanto.persistencia.entidad.DetallePedido;
import co.edu.unal.sweng1.altanto.persistencia.entidad.Pedido;
import co.edu.unal.sweng1.altanto.persistencia.repositorio.DetallePedidoRepository;
import co.edu.unal.sweng1.altanto.persistencia.repositorio.InsumoRepository;
import co.edu.unal.sweng1.altanto.persistencia.repositorio.PedidoRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class RecepcionPedidoServiceTest {

    private static final Long PEDIDO = 1L;
    private static final Long HARINA = 1L;

    @Autowired
    private RecepcionPedidoService service;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private DetallePedidoRepository detallePedidoRepository;

    @Autowired
    private InsumoRepository articuloRepository;

    @Test
    void recibirSumaYGuardaLaCantidadRecibidaEnCadaLineaRepetida() {
        service.recibir(PEDIDO, cantidades("2.500", "1.250"));

        assertThat(articuloRepository.findById(HARINA).orElseThrow().getExistenciaActual())
                .isEqualByComparingTo("18.750");
        assertThat(detallePedidoRepository.findByPedidoIdOrderByIdAsc(PEDIDO))
                .extracting(DetallePedido::getCantidadRecibida)
                .containsExactly(new BigDecimal("2.500"), new BigDecimal("1.250"));
        Pedido recibido = pedidoRepository.findById(PEDIDO).orElseThrow();
        assertThat(recibido.esPendiente()).isFalse();
        assertThat(recibido.getFechaRecepcion()).isEqualTo(LocalDate.now());
    }

    @Test
    void permiteRecibirCero() {
        service.recibir(PEDIDO, cantidades("0", "0"));

        assertThat(articuloRepository.findById(HARINA).orElseThrow().getExistenciaActual())
                .isEqualByComparingTo("15.000");
        assertThat(detallePedidoRepository.findByPedidoIdOrderByIdAsc(PEDIDO))
                .extracting(DetallePedido::getCantidadRecibida)
                .containsExactly(BigDecimal.ZERO, BigDecimal.ZERO);
    }

    @Test
    void permiteDecimalesParaUnProductoDeReventa() {
        Long pedidoReventa = crearPedidoConArticulo(12L);
        Long detalleId = detallePedidoRepository.findByPedidoIdOrderByIdAsc(pedidoReventa).get(0).getId();

        service.recibir(
                pedidoReventa,
                List.of(new CantidadRecibidaPedido(detalleId, new BigDecimal("0.125"))));

        assertThat(articuloRepository.findById(12L).orElseThrow().getExistenciaActual())
                .isEqualByComparingTo("18.125");
    }

    @Test
    void rechazaArticulosElaboradosEnUnPedidoDeRecepcion() {
        Long pedidoElaborado = crearPedidoConArticulo(10L);
        Long detalleId = detallePedidoRepository.findByPedidoIdOrderByIdAsc(pedidoElaborado).get(0).getId();

        assertThatThrownBy(() -> service.recibir(
                pedidoElaborado,
                List.of(new CantidadRecibidaPedido(detalleId, BigDecimal.ONE))))
                .isInstanceOf(ReglaDeNegocioException.class)
                .hasMessage("El artículo Pan Francés Tradicional no se puede recibir en este pedido.");

        assertThat(articuloRepository.findById(10L).orElseThrow().getExistenciaActual())
                .isEqualByComparingTo("45.000");
    }

    @Test
    void rechazaCantidadNegativaSinCambiarPedidoNiExistencias() {
        assertThatThrownBy(() -> service.recibir(PEDIDO, cantidades("-1", "2")))
                .isInstanceOf(ReglaDeNegocioException.class)
                .hasMessage("La cantidad recibida no puede ser negativa.");

        assertThat(articuloRepository.findById(HARINA).orElseThrow().getExistenciaActual())
                .isEqualByComparingTo("15.000");
        assertThat(pedidoRepository.findById(PEDIDO).orElseThrow().esPendiente()).isTrue();
    }

    @Test
    void rechazaCantidadVaciaODemasiadosDecimalesSinCambiarExistencias() {
        assertThatThrownBy(() -> service.recibir(
                PEDIDO,
                List.of(new CantidadRecibidaPedido(1L, null),
                        new CantidadRecibidaPedido(2L, new BigDecimal("2")))))
                .isInstanceOf(ReglaDeNegocioException.class)
                .hasMessage("Digite la cantidad recibida de cada línea.");

        assertThatThrownBy(() -> service.recibir(PEDIDO, cantidades("1.2345", "2")))
                .isInstanceOf(ReglaDeNegocioException.class)
                .hasMessage("La cantidad recibida debe tener máximo 9 enteros y 3 decimales.");

        assertThat(articuloRepository.findById(HARINA).orElseThrow().getExistenciaActual())
                .isEqualByComparingTo("15.000");
    }

    @Test
    void rechazaLineasFaltantesDuplicadasOAjenaAlPedido() {
        assertThatThrownBy(() -> service.recibir(
                PEDIDO,
                List.of(new CantidadRecibidaPedido(1L, BigDecimal.ONE))))
                .isInstanceOf(ReglaDeNegocioException.class)
                .hasMessage("Digite una cantidad recibida para cada línea del pedido.");

        assertThatThrownBy(() -> service.recibir(
                PEDIDO,
                List.of(new CantidadRecibidaPedido(1L, BigDecimal.ONE),
                        new CantidadRecibidaPedido(1L, BigDecimal.ONE))))
                .isInstanceOf(ReglaDeNegocioException.class)
                .hasMessage("Una línea del pedido aparece más de una vez.");

        assertThatThrownBy(() -> service.recibir(
                PEDIDO,
                List.of(new CantidadRecibidaPedido(1L, BigDecimal.ONE),
                        new CantidadRecibidaPedido(99L, BigDecimal.ONE))))
                .isInstanceOf(ReglaDeNegocioException.class)
                .hasMessage("El formulario contiene líneas que no pertenecen al pedido.");

        assertThat(articuloRepository.findById(HARINA).orElseThrow().getExistenciaActual())
                .isEqualByComparingTo("15.000");
    }

    @Test
    void unaSegundaRecepcionFallaConMensajeClaroYSinSumarDeNuevo() {
        List<CantidadRecibidaPedido> cantidades = cantidades("2", "3");
        service.recibir(PEDIDO, cantidades);
        BigDecimal existenciaTrasPrimeraRecepcion =
                articuloRepository.findById(HARINA).orElseThrow().getExistenciaActual();

        assertThatThrownBy(() -> service.recibir(PEDIDO, cantidades))
                .isInstanceOf(ReglaDeNegocioException.class)
                .hasMessage("Este pedido ya fue recibido o está siendo procesado.");

        assertThat(articuloRepository.findById(HARINA).orElseThrow().getExistenciaActual())
                .isEqualByComparingTo(existenciaTrasPrimeraRecepcion);
    }

    @Test
    void pedidoInexistenteLanzaExcepcionEspecifica() {
        assertThatThrownBy(() -> service.recibir(999L, cantidades("1", "1")))
                .isInstanceOf(PedidoNoEncontradoException.class)
                .hasMessage("No existe el pedido con id 999.");
    }

    private List<CantidadRecibidaPedido> cantidades(String primera, String segunda) {
        return List.of(
                new CantidadRecibidaPedido(1L, new BigDecimal(primera)),
                new CantidadRecibidaPedido(2L, new BigDecimal(segunda)));
    }

    private Long crearPedidoConArticulo(Long articuloId) {
        Pedido pedido = new Pedido();
        pedido.setProveedor(pedidoRepository.findById(PEDIDO).orElseThrow().getProveedor());
        pedido.setFechaPedido(LocalDate.now());
        pedido = pedidoRepository.saveAndFlush(pedido);

        DetallePedido detalle = new DetallePedido();
        detalle.setPedido(pedido);
        detalle.setArticulo(articuloRepository.findById(articuloId).orElseThrow());
        detalle.setCantidadPedida(BigDecimal.ONE);
        detallePedidoRepository.saveAndFlush(detalle);
        return pedido.getId();
    }
}
