package co.edu.unal.sweng1.altanto.persistencia.repositorio;

import co.edu.unal.sweng1.altanto.negocio.dto.DatosInsumo;
import co.edu.unal.sweng1.altanto.negocio.servicio.FuenteInsumos;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class FuenteInsumosPersistencia implements FuenteInsumos {

    private final InsumoConfigRepository insumoConfigRepository;

    public FuenteInsumosPersistencia(
            InsumoConfigRepository insumoConfigRepository) {
        this.insumoConfigRepository = insumoConfigRepository;
    }

    @Override
    public List<DatosInsumo> obtenerInsumos() {
        return insumoConfigRepository.buscarDatosParaPanelAlertas();
    }
}