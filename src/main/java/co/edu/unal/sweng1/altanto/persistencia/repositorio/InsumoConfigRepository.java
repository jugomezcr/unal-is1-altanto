package co.edu.unal.sweng1.altanto.persistencia.repositorio;

import co.edu.unal.sweng1.altanto.negocio.dto.DatosInsumo;
import co.edu.unal.sweng1.altanto.persistencia.entidad.InsumoConfig;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * Acceso a la tabla {@code insumos_config} (umbral mínimo, proveedor habitual y
 * presentación de compra de cada insumo).
 */
public interface InsumoConfigRepository extends JpaRepository<InsumoConfig, Long> {

    /**
     * Datos crudos de TODOS los insumos activos con configuración, para el panel de
     * alertas (CU-02-HU-01). El nivel (NORMAL/ALERTA/CRÍTICO) lo decide
     * {@code CalculadoraAlerta} en Java, no la consulta.
     *
     * <p>{@code @Query} escribe la consulta a mano en JPQL: parece SQL pero usa clases y
     * atributos Java ({@code Articulo a}, {@code a.existenciaActual}). Se une por
     * {@code c.articuloId = a.id} porque {@code InsumoConfig} guarda el id como número.
     * {@code SELECT new ...DatosInsumo(...)} crea un record por fila (hay que escribir el
     * paquete completo).</p>
     */
    @Query("""
            SELECT new co.edu.unal.sweng1.altanto.negocio.dto.DatosInsumo(
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
