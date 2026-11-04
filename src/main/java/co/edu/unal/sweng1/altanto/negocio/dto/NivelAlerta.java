package co.edu.unal.sweng1.altanto.negocio.dto;

// Niveles de alerta. El orden va de más urgente a menos: el panel ordena con él.
// Para agregar un nivel nuevo, se inserta en su lugar de urgencia.
public enum NivelAlerta {
    CRITICO,
    ALERTA,
    NORMAL
}