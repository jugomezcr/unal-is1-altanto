package co.edu.unal.sweng1.altanto.persistencia.repositorio;

import co.edu.unal.sweng1.altanto.negocio.dto.DatosInsumo;
import co.edu.unal.sweng1.altanto.negocio.servicio.FuenteInsumos;
import java.util.List;
import org.springframework.stereotype.Repository;

/**
 * Implementación REAL de {@link FuenteInsumos}: saca los insumos de la base de datos.
 *
 * <p>El servicio de alertas solo conoce la interfaz {@code FuenteInsumos}. Spring ve que
 * esta clase la implementa y la inyecta allí. En las pruebas se usa otra implementación en
 * memoria, sin base de datos (inversión de dependencias).</p>
 *
 * <p>{@code @Repository}: como {@code @Service}, registra la clase para que Spring cree
 * un objeto de ella; además indica que es de la capa de persistencia.</p>
 */
@Repository
public class FuenteInsumosPersistencia implements FuenteInsumos {

    private final InsumoConfigRepository insumoConfigRepository;

    /** Spring inyecta el repositorio por constructor. */
    public FuenteInsumosPersistencia(
            InsumoConfigRepository insumoConfigRepository) {
        this.insumoConfigRepository = insumoConfigRepository;
    }

    /** {@code @Override}: implementa el método declarado en la interfaz. */
    @Override
    public List<DatosInsumo> obtenerInsumos() {
        return insumoConfigRepository.buscarDatosParaPanelAlertas();
    }
}
