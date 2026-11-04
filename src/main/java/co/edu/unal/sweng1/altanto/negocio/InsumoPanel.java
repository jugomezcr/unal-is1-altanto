
package co.edu.unal.sweng1.altanto.negocio;

import java.math.BigDecimal;
import java.util.Objects;

public final class InsumoPanel {

    private final String nombre;
    private final String unidad;
    private final BigDecimal existencia;
    private final BigDecimal umbral;
    private final BigDecimal consumoDiario;
    private final BigDecimal deficit;
    private final BigDecimal coberturaDias;
    private final NivelAlerta nivel;

    public InsumoPanel(
            String nombre,
            String unidad,
            BigDecimal existencia,
            BigDecimal umbral,
            BigDecimal consumoDiario,
            BigDecimal deficit,
            BigDecimal coberturaDias,
            NivelAlerta nivel) {

        this.nombre = nombre;
        this.unidad = unidad;
        this.existencia = existencia;
        this.umbral = umbral;
        this.consumoDiario = consumoDiario;
        this.deficit = deficit;
        this.coberturaDias = coberturaDias;
        this.nivel = nivel;
    }

    public String nombre() {
        return nombre;
    }

    public String unidad() {
        return unidad;
    }

    public BigDecimal existencia() {
        return existencia;
    }

    public BigDecimal umbral() {
        return umbral;
    }

    public BigDecimal consumoDiario() {
        return consumoDiario;
    }

    public BigDecimal deficit() {
        return deficit;
    }

    public BigDecimal coberturaDias() {
        return coberturaDias;
    }

    public NivelAlerta nivel() {
        return nivel;
    }

    @Override
    public boolean equals(Object objeto) {
        if (this == objeto) return true;
        if (!(objeto instanceof InsumoPanel otro)) return false;

        return Objects.equals(nombre, otro.nombre)
                && Objects.equals(unidad, otro.unidad)
                && Objects.equals(existencia, otro.existencia)
                && Objects.equals(umbral, otro.umbral)
                && Objects.equals(consumoDiario, otro.consumoDiario)
                && Objects.equals(deficit, otro.deficit)
                && Objects.equals(coberturaDias, otro.coberturaDias)
                && nivel == otro.nivel;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                nombre, unidad, existencia, umbral,
                consumoDiario, deficit, coberturaDias, nivel
        );
    }

    @Override
    public String toString() {
        return "InsumoPanel[nombre=" + nombre
                + ", unidad=" + unidad
                + ", existencia=" + existencia
                + ", umbral=" + umbral
                + ", consumoDiario=" + consumoDiario
                + ", deficit=" + deficit
                + ", coberturaDias=" + coberturaDias
                + ", nivel=" + nivel + "]";
    }
}
