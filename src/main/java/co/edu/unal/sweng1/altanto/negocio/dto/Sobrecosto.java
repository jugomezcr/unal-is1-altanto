package co.edu.unal.sweng1.altanto.negocio.dto;

import java.math.BigDecimal;

/**
 * Cuánto más (o menos) se pagó frente al precio habitual del mayorista (CU-04-HU-02).
 * Es un {@code record}: como un {@code @dataclass(frozen=True)} de Python.
 *
 * @param pesos      diferencia total en pesos (positiva = se pagó de más).
 * @param porcentaje diferencia respecto al precio habitual, en %.
 */
public record Sobrecosto(BigDecimal pesos, BigDecimal porcentaje) {
}
