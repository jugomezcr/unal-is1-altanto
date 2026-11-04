package co.edu.unal.sweng1.altanto.negocio.dto;

import java.math.BigDecimal;

// Una fila lista para la vista (templates/inventario/alertas.html). La arma CalculadoraAlerta.
// coberturaDias es null cuando no se puede calcular.
public record InsumoPanel(
        String nombre,
        String unidad,
        BigDecimal existencia,
        BigDecimal umbral,
        BigDecimal deficit,
        BigDecimal coberturaDias,
        NivelAlerta nivel) {
}