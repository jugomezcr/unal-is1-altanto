package co.edu.unal.sweng1.altanto.persistencia.repositorio;

import co.edu.unal.sweng1.altanto.persistencia.entidad.ConciliacionDiaria;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Acceso a la tabla {@code conciliaciones_diarias}.
 *
 * <p><b>¿Por qué es una interfaz sin código?</b> Spring Data JPA escribe la implementación
 * al arrancar. Al extender {@code JpaRepository<ConciliacionDiaria, Long>} (entidad, tipo de
 * la llave) se heredan {@code save}, {@code findById}, {@code findAll}, {@code delete}...</p>
 *
 * <p>Para los métodos de abajo, Spring "lee" el NOMBRE y arma el SQL (consultas derivadas):
 * {@code findByFechaConciliacion} → {@code SELECT ... WHERE fecha_conciliacion = ?}.</p>
 */
public interface ConciliacionRepository extends JpaRepository<ConciliacionDiaria, Long> {

    /**
     * La conciliación de una fecha, si existe.
     * {@code Optional} es una "caja" que puede venir vacía (en vez de devolver {@code null}).
     */
    Optional<ConciliacionDiaria> findByFechaConciliacion(LocalDate fecha);

    /** Todas, de la fecha más reciente a la más antigua ({@code ORDER BY ... DESC}). */
    List<ConciliacionDiaria> findAllByOrderByFechaConciliacionDesc();
}
