package co.edu.unal.sweng1.altanto.persistencia.repositorio;

import co.edu.unal.sweng1.altanto.persistencia.entidad.DetallePedido;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Acceso a las líneas individuales de cada pedido. */
public interface DetallePedidoRepository extends JpaRepository<DetallePedido, Long> {

    List<DetallePedido> findByPedidoIdOrderByIdAsc(Long pedidoId);
}
