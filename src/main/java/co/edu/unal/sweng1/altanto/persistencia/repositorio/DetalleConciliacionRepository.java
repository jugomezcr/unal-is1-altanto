package co.edu.unal.sweng1.altanto.persistencia.repositorio;

import co.edu.unal.sweng1.altanto.persistencia.entidad.DetalleConciliacion;
import co.edu.unal.sweng1.altanto.persistencia.entidad.EstadoConciliacion;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Acceso a la tabla {@code detalles_conciliacion} (las líneas por artículo del CU-01).
 * Ver {@link ConciliacionRepository} para entender cómo funcionan estos repositorios.
 */
public interface DetalleConciliacionRepository extends JpaRepository<DetalleConciliacion, Long> {

    /**
     * Líneas de una conciliación, ordenadas por tipo de artículo y luego por nombre.
     *
     * <p>{@code @Query} permite escribir la consulta a mano en <b>JPQL</b>: se parece a
     * SQL, pero usa nombres de CLASES y ATRIBUTOS de Java ({@code DetalleConciliacion},
     * {@code d.articulo.nombre}), no de tablas y columnas. {@code :conciliacionId} es un
     * parámetro que se llena con el argumento marcado con {@code @Param}.</p>
     */
    @Query("""
            SELECT d FROM DetalleConciliacion d
            WHERE d.conciliacion.id = :conciliacionId
            ORDER BY d.articulo.tipoArticulo, d.articulo.nombre
            """)
    List<DetalleConciliacion> buscarPorConciliacion(@Param("conciliacionId") Long conciliacionId);

    /**
     * Lo último que se contó de un artículo en una conciliación del estado indicado
     * (CONFIRMADA) anterior a {@code fecha}. Es la "existencia anterior" del CU-01:
     * "lo contado queda como existencia de partida del día siguiente".
     *
     * <p>{@code Limit.of(1)} hace que la base de datos devuelva solo la primera fila
     * (la más reciente, por el {@code ORDER BY ... DESC}).</p>
     */
    @Query("""
            SELECT d FROM DetalleConciliacion d
            WHERE d.articulo.id = :articuloId
              AND d.conciliacion.estado = :estado
              AND d.conciliacion.fechaConciliacion < :fecha
            ORDER BY d.conciliacion.fechaConciliacion DESC
            """)
    List<DetalleConciliacion> buscarUltimoConteo(@Param("articuloId") Long articuloId,
                                                @Param("estado") EstadoConciliacion estado,
                                                @Param("fecha") LocalDate fecha,
                                                Limit limite);
}
