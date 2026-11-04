package co.edu.unal.sweng1.altanto.negocio.servicio;

import co.edu.unal.sweng1.altanto.negocio.dto.DatosInsumo;
import java.util.List;

// Contrato: de dónde salen los insumos. El servicio solo conoce esta interfaz.
// interface = lista de métodos sin código (como una clase abstracta / Protocol de Python).
// En la app la implementa FuenteInsumosPersistencia (BD); en pruebas, FuenteEnMemoria.
public interface FuenteInsumos {
    // Todos los insumos candidatos al panel; puede devolver null o lista vacía.
    List<DatosInsumo> obtenerInsumos();
}