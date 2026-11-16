package co.edu.unal.sweng1.altanto.persistencia.repositorio;

import co.edu.unal.sweng1.altanto.persistencia.entidad.DetalleTandaInsumo;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Acceso a la tabla {@code detalles_tanda_insumo} (las líneas de insumos gastados en una
 * tanda, CU-03).
 *
 * <p>Interfaz sin código: Spring Data JPA escribe la implementación al arrancar. Ver
 * {@link ConciliacionRepository} y {@link DetalleConciliacionRepository} (su equivalente
 * en el CU-01) para entender cómo funcionan estos repositorios.</p>
 */
public interface DetalleTandaInsumoRepository extends JpaRepository<DetalleTandaInsumo, Long> {

    /**
     * Líneas de una tanda. {@code TandaId} recorre la relación:
     * {@code detalle.tanda.id = ?}.
     */
    List<DetalleTandaInsumo> findByTandaId(Long tandaId);
}
