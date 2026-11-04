package co.edu.unal.sweng1.altanto.persistencia.repositorio;

import co.edu.unal.sweng1.altanto.persistencia.entidad.TandaProduccion;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Acceso a la tabla {@code tandas_produccion} (CU-03).
 *
 * <p>Vacía a propósito: hereda de {@code JpaRepository} los métodos {@code save},
 * {@code findById}, {@code findAll}, {@code delete}... Spring crea la implementación al
 * arrancar. CU-03-HU-01 agregará aquí sus consultas (ver {@link ConciliacionRepository}).</p>
 */
public interface TandaProduccionRepository extends JpaRepository<TandaProduccion, Long> {
}
