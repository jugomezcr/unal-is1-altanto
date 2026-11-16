package co.edu.unal.sweng1.altanto.persistencia.repositorio;

import co.edu.unal.sweng1.altanto.persistencia.entidad.TandaProduccion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Acceso a la tabla {@code tandas_produccion} (CU-03).
 *
 * <p>Al extender {@code JpaRepository<TandaProduccion, Long>} (entidad, tipo de la llave)
 * se heredan {@code save}, {@code findById}, {@code findAll}, {@code delete}... Spring
 * crea la implementación al arrancar. Ver {@link ConciliacionRepository} para entender
 * las consultas derivadas (el SQL sale del NOMBRE del método).</p>
 */
public interface TandaProduccionRepository extends JpaRepository<TandaProduccion, Long> {

    /** Todas las tandas, de la fecha más reciente a la más antigua ({@code ORDER BY fecha DESC}). */
    List<TandaProduccion> findAllByOrderByFechaDesc();
}
