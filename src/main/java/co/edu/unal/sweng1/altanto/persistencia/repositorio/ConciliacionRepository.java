package co.edu.unal.sweng1.altanto.persistencia.repositorio;

import co.edu.unal.sweng1.altanto.negocio.dto.CierrePrevio;
import co.edu.unal.sweng1.altanto.persistencia.entidad.ConciliacionDiaria;
import co.edu.unal.sweng1.altanto.persistencia.entidad.EstadoConciliacion;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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

    /**
     * Historial de conciliaciones, de la más reciente a la más antigua, con cuántos
     * artículos tiene cada una y cuántos no cuadraron.
     *
     * <p>Cómo leer la consulta (JPQL):</p>
     * <ul>
     *   <li>{@code LEFT JOIN DetalleConciliacion d ON d.conciliacion = c}: une cada
     *       conciliación con sus líneas. "LEFT" hace que también salgan las que no tienen
     *       líneas (por ejemplo, un borrador vacío), con conteos en 0.</li>
     *   <li>{@code COUNT(d)}: cuántas líneas tiene.</li>
     *   <li>{@code SUM(CASE WHEN ... THEN 1 ELSE 0 END)}: cuenta solo las líneas con
     *       diferencia. {@code COALESCE(..., 0)} cambia el {@code NULL} por 0 cuando no hay líneas.</li>
     *   <li>{@code GROUP BY}: una fila por conciliación.</li>
     *   <li>{@code SELECT new ...CierrePrevio(...)}: arma un {@code CierrePrevio} por fila
     *       (expresión constructora, ver InsumoConfigRepository).</li>
     * </ul>
     */
    @Query("""
            SELECT new co.edu.unal.sweng1.altanto.negocio.dto.CierrePrevio(
                       c.id, c.fechaConciliacion, c.totalVentaReporteZ, c.estado,
                       COUNT(d),
                       COALESCE(SUM(CASE WHEN d.diferencia <> 0 THEN 1L ELSE 0L END), 0L))
            FROM ConciliacionDiaria c
            LEFT JOIN DetalleConciliacion d ON d.conciliacion = c
            GROUP BY c.id, c.fechaConciliacion, c.totalVentaReporteZ, c.estado
            ORDER BY c.fechaConciliacion DESC
            """)
    List<CierrePrevio> buscarHistorial();
}
