package co.edu.unal.sweng1.altanto.persistencia.repositorio;

import co.edu.unal.sweng1.altanto.persistencia.entidad.EstadoPedido;
import co.edu.unal.sweng1.altanto.persistencia.entidad.Pedido;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acceso a las cabeceras de pedido. */
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findByEstadoOrderByFechaPedidoAscIdAsc(EstadoPedido estado);
}
