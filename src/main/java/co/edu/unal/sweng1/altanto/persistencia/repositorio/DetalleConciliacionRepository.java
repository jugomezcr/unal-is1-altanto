package co.edu.unal.sweng1.altanto.persistencia.repositorio;

import co.edu.unal.sweng1.altanto.persistencia.entidad.DetalleConciliacion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Acceso a la tabla {@code detalles_conciliacion} (las líneas por artículo del CU-01).
 * Ver {@link ConciliacionRepository} para entender cómo funcionan estos repositorios.
 */
public interface DetalleConciliacionRepository extends JpaRepository<DetalleConciliacion, Long> {

    /**
     * Líneas de una conciliación. {@code ConciliacionId} recorre la relación:
     * {@code detalle.conciliacion.id = ?}.
     */
    List<DetalleConciliacion> findByConciliacionId(Long conciliacionId);
}
