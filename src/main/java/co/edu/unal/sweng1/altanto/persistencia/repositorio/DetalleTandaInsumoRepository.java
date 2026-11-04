package co.edu.unal.sweng1.altanto.persistencia.repositorio;

import co.edu.unal.sweng1.altanto.persistencia.entidad.DetalleTandaInsumo;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Acceso a la tabla {@code detalles_tanda_insumo}: los insumos gastados en cada tanda
 * (CU-03). La conciliación (CU-01) lo usa para restar "lo consumido en tandas".
 * CU-03-HU-01 puede agregar aquí los métodos que necesite para guardar tandas.
 */
public interface DetalleTandaInsumoRepository extends JpaRepository<DetalleTandaInsumo, Long> {

    /**
     * Total consumido de un insumo en todas las tandas de una fecha.
     *
     * <p>{@code SUM(...)} devuelve {@code NULL} si no hubo tandas ese día.
     * {@code COALESCE(x, 0)} cambia ese {@code NULL} por 0, para que el servicio siempre
     * reciba un número.</p>
     */
    @Query("""
            SELECT COALESCE(SUM(d.cantidadConsumida), 0)
            FROM DetalleTandaInsumo d
            WHERE d.insumo.id = :insumoId AND d.tanda.fecha = :fecha
            """)
    BigDecimal sumarConsumoDelDia(@Param("insumoId") Long insumoId, @Param("fecha") LocalDate fecha);
}
