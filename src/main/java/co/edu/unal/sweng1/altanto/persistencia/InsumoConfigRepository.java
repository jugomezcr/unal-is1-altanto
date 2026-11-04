package co.edu.unal.sweng1.altanto.persistencia;

import co.edu.unal.sweng1.altanto.negocio.InsumoConfig;
import co.edu.unal.sweng1.altanto.negocio.InsumoEnAlerta;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import co.edu.unal.sweng1.altanto.negocio.DatosInsumo;

/**
 * Acceso a la tabla {@code insumos_config} (umbral mínimo, proveedor habitual y
 * presentación de compra de cada insumo).
 */
public interface InsumoConfigRepository extends JpaRepository<InsumoConfig, Long> {

    /**
     * Insumos activos cuya existencia actual está en su umbral mínimo o por debajo.
     *
     * <p>Une {@code Articulo} con {@code InsumoConfig} por el id
     * ({@code c.articuloId = a.id}), porque {@code InsumoConfig} guarda el id como
     * número y no como relación {@code @ManyToOne}.</p>
     *
     * <p>{@code SELECT new ...InsumoEnAlerta(...)} es una "expresión constructora": en
     * vez de devolver entidades, JPA crea directamente un {@link InsumoEnAlerta} por
     * fila con los 4 valores indicados. Hay que escribir el nombre completo del paquete.</p>
     */
    @Query("""
            SELECT new co.edu.unal.sweng1.altanto.negocio.InsumoEnAlerta(
                       a.nombre, a.unidadMedida, a.existenciaActual, c.umbralMinimo)
            FROM Articulo a, InsumoConfig c
            WHERE c.articuloId = a.id
              AND a.activo = true
              AND a.existenciaActual <= c.umbralMinimo
            ORDER BY a.nombre
            """)
    List<InsumoEnAlerta> buscarInsumosEnAlerta();

    /**
 * Obtiene los datos de los insumos activos y configurados
 * necesarios para calcular el panel de alertas.
 */
@Query("""
        SELECT new co.edu.unal.sweng1.altanto.negocio.DatosInsumo(
            a.codigo,
            a.nombre,
            a.unidadMedida,
            a.existenciaActual,
            c.umbralMinimo,
            c.consumoPromedioDiario
        )
        FROM Articulo a, InsumoConfig c
        WHERE c.articuloId = a.id
          AND a.tipoArticulo = 'INSUMO'
          AND a.activo = true
        ORDER BY a.nombre
        """)
List<DatosInsumo> buscarDatosParaPanelAlertas();
}
