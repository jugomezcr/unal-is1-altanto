
package co.edu.unal.sweng1.altanto.negocio;

import java.math.BigDecimal;
import java.util.Objects;

public final class DatosInsumo {

    private final String codigo;
    private final String nombre;
    private final String unidad;
    private final BigDecimal existencia;
    private final BigDecimal umbral;
    private final BigDecimal consumoDiario;

    public DatosInsumo(
            String codigo,
            String nombre,
            String unidad,
            BigDecimal existencia,
            BigDecimal umbral,
            BigDecimal consumoDiario) {

        this.codigo = codigo;
        this.nombre = nombre;
        this.unidad = unidad;
        this.existencia = existencia;
        this.umbral = umbral;
        this.consumoDiario = consumoDiario;
    }

    public String codigo() {
        return codigo;
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

    @Override
    public boolean equals(Object objeto) {
        if (this == objeto) return true;
        if (!(objeto instanceof DatosInsumo otro)) return false;

        return Objects.equals(codigo, otro.codigo)
                && Objects.equals(nombre, otro.nombre)
                && Objects.equals(unidad, otro.unidad)
                && Objects.equals(existencia, otro.existencia)
                && Objects.equals(umbral, otro.umbral)
                && Objects.equals(consumoDiario, otro.consumoDiario);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                codigo, nombre, unidad,
                existencia, umbral, consumoDiario
        );
    }

    @Override
    public String toString() {
        return "DatosInsumo[codigo=" + codigo
                + ", nombre=" + nombre
                + ", unidad=" + unidad
                + ", existencia=" + existencia
                + ", umbral=" + umbral
                + ", consumoDiario=" + consumoDiario + "]";
    }
}
