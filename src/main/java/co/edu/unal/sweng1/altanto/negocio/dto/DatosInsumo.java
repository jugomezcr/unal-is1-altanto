package co.edu.unal.sweng1.altanto.negocio.dto;

import java.math.BigDecimal;

// Datos crudos de un insumo, tal como los entrega la fuente (sin depender de JPA).
// record: clase inmutable solo de datos; Java genera constructor y getters (codigo(), ...).
// La llena InsumoConfigRepository.buscarDatosParaPanelAlertas().
public record DatosInsumo(
        String codigo,
        String nombre,
        String unidad,
        BigDecimal existencia,      // existencia actual
        BigDecimal umbral,          // stock mínimo de seguridad
        BigDecimal consumoDiario) { // consumo promedio por día
}