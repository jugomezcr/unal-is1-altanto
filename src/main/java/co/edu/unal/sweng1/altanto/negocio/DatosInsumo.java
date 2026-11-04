package co.edu.unal.sweng1.altanto.negocio;

import java.math.BigDecimal;

// Datos crudos de un insumo, tal como los entrega la fuente (sin depender de JPA).
public record DatosInsumo(
        String codigo,
        String nombre,
        String unidad,
        BigDecimal existencia,      // existencia actual
        BigDecimal umbral,          // stock mínimo de seguridad
        BigDecimal consumoDiario) { // consumo promedio por día
}