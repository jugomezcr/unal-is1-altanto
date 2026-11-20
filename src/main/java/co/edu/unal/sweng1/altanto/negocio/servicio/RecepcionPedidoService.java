package co.edu.unal.sweng1.altanto.negocio.servicio;

import co.edu.unal.sweng1.altanto.negocio.dto.CantidadRecibidaPedido;
import co.edu.unal.sweng1.altanto.negocio.excepcion.PedidoNoEncontradoException;
import co.edu.unal.sweng1.altanto.negocio.excepcion.ReglaDeNegocioException;
import co.edu.unal.sweng1.altanto.persistencia.entidad.Articulo;
import co.edu.unal.sweng1.altanto.persistencia.entidad.DetallePedido;
import co.edu.unal.sweng1.altanto.persistencia.entidad.EstadoPedido;
import co.edu.unal.sweng1.altanto.persistencia.entidad.Pedido;
import co.edu.unal.sweng1.altanto.persistencia.repositorio.DetallePedidoRepository;
import co.edu.unal.sweng1.altanto.persistencia.repositorio.InsumoRepository;
import co.edu.unal.sweng1.altanto.persistencia.repositorio.PedidoRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Reglas de negocio de la recepción de un pedido pendiente (CU-04-HU-01). */
@Service
public class RecepcionPedidoService {

    private static final String MENSAJE_PEDIDO_NO_PENDIENTE =
            "Este pedido ya fue recibido o está siendo procesado.";
    private static final int MAX_DECIMALES = 3;
    private static final int MAX_ENTEROS = 9;

    private final PedidoRepository pedidoRepository;
    private final DetallePedidoRepository detallePedidoRepository;
    private final InsumoRepository articuloRepository;

    public RecepcionPedidoService(PedidoRepository pedidoRepository,
                                  DetallePedidoRepository detallePedidoRepository,
                                  InsumoRepository articuloRepository) {
        this.pedidoRepository = pedidoRepository;
        this.detallePedidoRepository = detallePedidoRepository;
        this.articuloRepository = articuloRepository;
    }

    @Transactional(readOnly = true)
    public List<Pedido> pedidosPendientes() {
        return pedidoRepository.findByEstadoOrderByFechaPedidoAscIdAsc(EstadoPedido.PENDIENTE);
    }

    @Transactional(readOnly = true)
    public Pedido buscarPendiente(Long pedidoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new PedidoNoEncontradoException(pedidoId));
        if (!pedido.esPendiente()) {
            throw new ReglaDeNegocioException(MENSAJE_PEDIDO_NO_PENDIENTE);
        }
        return pedido;
    }

    @Transactional(readOnly = true)
    public List<DetallePedido> detalles(Long pedidoId) {
        buscarPendiente(pedidoId);
        return detallePedidoRepository.findByPedidoIdOrderByIdAsc(pedidoId);
    }

    @Transactional
    public void recibir(Long pedidoId, List<CantidadRecibidaPedido> cantidadesRecibidas) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new PedidoNoEncontradoException(pedidoId));
        if (!pedido.esPendiente()) {
            throw new ReglaDeNegocioException(MENSAJE_PEDIDO_NO_PENDIENTE);
        }

        List<DetallePedido> detalles = detallePedidoRepository.findByPedidoIdOrderByIdAsc(pedidoId);
        if (detalles.isEmpty()) {
            throw new ReglaDeNegocioException("El pedido no tiene líneas para recibir.");
        }
        Map<Long, BigDecimal> cantidadesPorDetalle = validarCantidades(detalles, cantidadesRecibidas);

        for (DetallePedido detalle : detalles) {
            BigDecimal recibida = cantidadesPorDetalle.get(detalle.getId());
            detalle.setCantidadRecibida(recibida);
            Articulo articulo = detalle.getArticulo();
            articulo.setExistenciaActual(articulo.getExistenciaActual().add(recibida));
        }
        pedido.cerrar(LocalDate.now());

        try {
            pedidoRepository.saveAndFlush(pedido);
        } catch (OptimisticLockingFailureException e) {
            throw new ReglaDeNegocioException(MENSAJE_PEDIDO_NO_PENDIENTE);
        }
    }

    private Map<Long, BigDecimal> validarCantidades(
            List<DetallePedido> detalles,
            List<CantidadRecibidaPedido> cantidadesRecibidas) {
        if (cantidadesRecibidas == null || cantidadesRecibidas.size() != detalles.size()) {
            throw new ReglaDeNegocioException(
                    "Digite una cantidad recibida para cada línea del pedido.");
        }

        Set<Long> idsDetallePedido = detalles.stream()
                .map(DetallePedido::getId)
                .collect(Collectors.toSet());
        Map<Long, BigDecimal> porDetalle = new HashMap<>();
        for (CantidadRecibidaPedido cantidad : cantidadesRecibidas) {
            if (cantidad == null || cantidad.detallePedidoId() == null) {
                throw new ReglaDeNegocioException("Cada cantidad debe identificar una línea del pedido.");
            }
            if (cantidad.cantidadRecibida() == null) {
                throw new ReglaDeNegocioException("Digite la cantidad recibida de cada línea.");
            }
            if (!idsDetallePedido.contains(cantidad.detallePedidoId())) {
                throw new ReglaDeNegocioException(
                        "El formulario contiene líneas que no pertenecen al pedido.");
            }
            if (porDetalle.putIfAbsent(cantidad.detallePedidoId(), cantidad.cantidadRecibida()) != null) {
                throw new ReglaDeNegocioException("Una línea del pedido aparece más de una vez.");
            }
        }

        for (DetallePedido detalle : detalles) {
            BigDecimal recibida = porDetalle.get(detalle.getId());
            if (recibida == null) {
                throw new ReglaDeNegocioException("Digite la cantidad recibida de cada línea.");
            }
            validarCantidad(detalle, recibida);
        }

        return porDetalle;
    }

    private void validarCantidad(DetallePedido detalle, BigDecimal cantidad) {
        if (cantidad.signum() < 0) {
            throw new ReglaDeNegocioException("La cantidad recibida no puede ser negativa.");
        }
        BigDecimal normalizada = cantidad.stripTrailingZeros();
        int decimales = Math.max(normalizada.scale(), 0);
        int enteros = Math.max(normalizada.precision() - normalizada.scale(), 0);
        if (decimales > MAX_DECIMALES || enteros > MAX_ENTEROS) {
            throw new ReglaDeNegocioException(
                    "La cantidad recibida debe tener máximo 9 enteros y 3 decimales.");
        }

        String tipoArticulo = detalle.getArticulo().getTipoArticulo();
        if (!Articulo.TIPO_INSUMO.equals(tipoArticulo)
                && !Articulo.TIPO_PRODUCTO_REVENTA.equals(tipoArticulo)) {
            throw new ReglaDeNegocioException(
                    "El artículo " + detalle.getArticulo().getNombre() + " no se puede recibir en este pedido.");
        }
    }
}
