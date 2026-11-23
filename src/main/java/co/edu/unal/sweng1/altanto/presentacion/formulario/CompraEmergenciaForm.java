package co.edu.unal.sweng1.altanto.presentacion.formulario;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

/**
 * Datos del formulario de compra de emergencia (CU-04-HU-02), tal como llegan del navegador.
 * Spring llena cada campo con el {@code <input name="...">} del mismo nombre (data binding).
 * Las anotaciones son reglas de FORMATO; las de negocio están en
 * {@code CompraEmergenciaService}. Los límites de {@code @Digits} caben en las columnas de
 * {@code compras_emergencia}.
 */
public class CompraEmergenciaForm {

    @NotNull(message = "Elija el artículo comprado.")
    private Long articuloId;

    @NotNull(message = "Digite la cantidad recibida.")
    @Positive(message = "La cantidad debe ser mayor que cero.")
    @Digits(integer = 6, fraction = 3, message = "Máximo 6 enteros y 3 decimales.")
    private BigDecimal cantidadRecibida;

    @NotNull(message = "Digite el precio pagado.")
    @Positive(message = "El precio debe ser mayor que cero.")
    @Digits(integer = 8, fraction = 2, message = "Máximo 8 enteros y 2 decimales.")
    private BigDecimal precioPagado;

    @NotNull(message = "Digite el precio habitual del mayorista.")
    @Positive(message = "El precio debe ser mayor que cero.")
    @Digits(integer = 8, fraction = 2, message = "Máximo 8 enteros y 2 decimales.")
    private BigDecimal precioHabitual;

    // Getters y setters: Spring los usa para llenar el formulario y Thymeleaf para leerlo.

    public Long getArticuloId() {
        return articuloId;
    }

    public void setArticuloId(Long articuloId) {
        this.articuloId = articuloId;
    }

    public BigDecimal getCantidadRecibida() {
        return cantidadRecibida;
    }

    public void setCantidadRecibida(BigDecimal cantidadRecibida) {
        this.cantidadRecibida = cantidadRecibida;
    }

    public BigDecimal getPrecioPagado() {
        return precioPagado;
    }

    public void setPrecioPagado(BigDecimal precioPagado) {
        this.precioPagado = precioPagado;
    }

    public BigDecimal getPrecioHabitual() {
        return precioHabitual;
    }

    public void setPrecioHabitual(BigDecimal precioHabitual) {
        this.precioHabitual = precioHabitual;
    }
}
