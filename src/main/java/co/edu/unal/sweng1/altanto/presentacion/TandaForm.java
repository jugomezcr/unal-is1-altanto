package co.edu.unal.sweng1.altanto.presentacion;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * Data of the "Registrar tanda" form (CU-03-HU-01), exactly as the browser sends it.
 *
 * <p>Spring MVC fills this object through data binding (same as
 * {@link ConciliacionForm}): every {@code <input name="...">} maps to the setter with
 * that name. The input rows travel as two position-aligned lists:
 * {@code insumoIds[i]} is the article of row {@code i} and {@code cantidades[i]} its
 * amount. Empty rows arrive as {@code null} values and are ignored by
 * {@code TandaService}.</p>
 *
 * <p>{@code jakarta.validation} annotations are format rules that Spring checks before
 * calling the service (thanks to {@code @Valid} in the controller). Business rules
 * (stock, article type) live in the service.</p>
 */
public class TandaForm {

    /** {@code @DateTimeFormat(ISO.DATE)}: the {@code <input type="date">} sends "2026-11-03". */
    @NotNull(message = "La fecha es obligatoria.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fecha;

    @NotNull(message = "El producto es obligatorio.")
    private Long productoId;

    @NotNull(message = "La harina base es obligatoria.")
    @Positive(message = "La harina base debe ser mayor que cero.")
    @Digits(integer = 8, fraction = 2, message = "Máximo 2 decimales.")
    private BigDecimal harinaBaseKg;

    @NotNull(message = "Las unidades obtenidas son obligatorias.")
    @Positive(message = "Las unidades obtenidas deben ser mayor que cero.")
    private Integer unidadesObtenidas;

    @NotBlank(message = "El maestro panadero es obligatorio.")
    @Size(max = 100, message = "Máximo 100 caracteres.")
    private String maestroPanadero;

    /** Optional: the column accepts null. */
    @Size(max = 255, message = "Máximo 255 caracteres.")
    private String observaciones;

    /** Input id per form row; index {@code i} matches {@code cantidades.get(i)}. */
    private List<Long> insumoIds = new ArrayList<>();

    /** Amount consumed per form row; empty rows carry null or a non-positive value. */
    private List<BigDecimal> cantidades = new ArrayList<>();

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public Long getProductoId() {
        return productoId;
    }

    public void setProductoId(Long productoId) {
        this.productoId = productoId;
    }

    public BigDecimal getHarinaBaseKg() {
        return harinaBaseKg;
    }

    public void setHarinaBaseKg(BigDecimal harinaBaseKg) {
        this.harinaBaseKg = harinaBaseKg;
    }

    public Integer getUnidadesObtenidas() {
        return unidadesObtenidas;
    }

    public void setUnidadesObtenidas(Integer unidadesObtenidas) {
        this.unidadesObtenidas = unidadesObtenidas;
    }

    public String getMaestroPanadero() {
        return maestroPanadero;
    }

    public void setMaestroPanadero(String maestroPanadero) {
        this.maestroPanadero = maestroPanadero;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public List<Long> getInsumoIds() {
        return insumoIds;
    }

    public void setInsumoIds(List<Long> insumoIds) {
        this.insumoIds = insumoIds;
    }

    public List<BigDecimal> getCantidades() {
        return cantidades;
    }

    public void setCantidades(List<BigDecimal> cantidades) {
        this.cantidades = cantidades;
    }
}
