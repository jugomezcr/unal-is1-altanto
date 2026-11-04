package co.edu.unal.sweng1.altanto.negocio.servicio;

import co.edu.unal.sweng1.altanto.negocio.dto.DatosInsumo;
import java.util.List;

// Contrato: de dónde salen los insumos. El servicio solo conoce esta interfaz.
public interface FuenteInsumos {
    List<DatosInsumo> obtenerInsumos();
}