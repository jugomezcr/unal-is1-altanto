package co.edu.unal.sweng1.altanto.negocio.dto;

import java.math.BigDecimal;

// Una fila lista para la vista (templates/inventario/alertas.html). La arma CalculadoraAlerta.
// record: Java genera solo el constructor, los getters (nombre(), ...), equals, hashCode y toString.
// coberturaDias es null cuando no se puede calcular.
public record InsumoPanel(
        String nombre,
        String unidad,
        BigDecimal existencia,
        BigDecimal umbral,
        BigDecimal consumoDiario,   // consumo promedio por día (null si no está configurado)
        BigDecimal deficit,
        BigDecimal coberturaDias,
        NivelAlerta nivel) {
}
