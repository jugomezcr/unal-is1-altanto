package co.edu.unal.sweng1.altanto.persistencia;

import co.edu.unal.sweng1.altanto.negocio.Articulo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

/**
 * Acceso a la tabla {@code articulos} (insumos y productos).
 * Ver {@link ConciliacionRepository} para entender las consultas derivadas.
 */
public interface InsumoRepository extends JpaRepository<Articulo, Long> {

    /** Artículos con existencia menor o igual a un valor (usado por CU-02-HU-01). */
    List<Articulo> findByExistenciaActualLessThanEqual(BigDecimal umbral);

    /**
     * Artículos activos de los tipos indicados, ordenados por tipo y nombre.
     *
     * <p>CU-01 lo usa con {@code INSUMO} y {@code PRODUCTO_REVENTA}: son los artículos
     * que se concilian. Spring traduce el nombre así: {@code ActivoTrue} →
     * {@code activo = true}; {@code TipoArticuloIn} → {@code tipo_articulo IN (...)};
     * {@code OrderByTipoArticuloAscNombreAsc} → ordenar por tipo y luego por nombre.</p>
     */
    List<Articulo> findByActivoTrueAndTipoArticuloInOrderByTipoArticuloAscNombreAsc(Collection<String> tipos);
}
