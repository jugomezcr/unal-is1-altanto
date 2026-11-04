package co.edu.unal.sweng1.altanto.persistencia;

import co.edu.unal.sweng1.altanto.negocio.ConciliacionDiaria;
import co.edu.unal.sweng1.altanto.negocio.EstadoConciliacion;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Acceso a la tabla {@code conciliaciones_diarias}.
 *
 * <p><b>¿Por qué es una interfaz sin código?</b> Spring Data JPA crea la
 * implementación al arrancar la aplicación. Al extender
 * {@code JpaRepository<ConciliacionDiaria, Long>} (entidad, tipo de la llave) se heredan
 * {@code save}, {@code findById}, {@code findAll}, {@code delete}, etc.</p>
 *
 * <p>Para los métodos declarados aquí, Spring "lee" el NOMBRE del método y arma la
 * consulta SQL. Por ejemplo, {@code findByFechaConciliacion} se convierte en
 * {@code SELECT ... WHERE fecha_conciliacion = ?}. Se llaman "consultas derivadas".</p>
 */
public interface ConciliacionRepository extends JpaRepository<ConciliacionDiaria, Long> {

    /**
     * Busca la conciliación de una fecha (CU-01, extensión 1a).
     *
     * @return un {@link Optional}: una "caja" que puede traer la conciliación o venir
     *         vacía si no hay ninguna. Así se evita devolver {@code null}.
     */
    Optional<ConciliacionDiaria> findByFechaConciliacion(LocalDate fecha);

    /**
     * Última conciliación en un estado dado, ordenada por fecha de mayor a menor
     * ({@code findFirst...OrderBy...Desc}). Se usa con {@code CONFIRMADA} para saber
     * desde qué día ya está cerrado el inventario.
     */
    Optional<ConciliacionDiaria> findFirstByEstadoOrderByFechaConciliacionDesc(EstadoConciliacion estado);
}
