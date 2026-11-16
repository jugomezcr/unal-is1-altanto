package co.edu.unal.sweng1.altanto.presentacion.formulario;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * Datos del formulario "Registrar tanda de producción" (CU-03), tal como llegan del
 * navegador.
 *
 * <p>Spring MVC llena este objeto solo: por cada {@code <input name="...">} del HTML busca
 * el setter con ese nombre (data binding). Para la lista usa nombres como
 * {@code lineas[0].cantidad}. Mismo patrón que {@link ConciliacionForm} en el CU-01.</p>
 *
 * <p>Las anotaciones de {@code jakarta.validation} son reglas de FORMATO (no vacío, no
 * negativo, cuántos decimales), que Spring revisa antes de llamar al servicio gracias a
 * {@code @Valid} en el controlador. Las reglas de NEGOCIO (que un insumo alcance) viven en
 * {@code TandaProduccionService}, no aquí.</p>
 */
public class TandaForm {

    /** Día de la horneada. El {@code <input type="date">} envía "2026-11-03". */
    @NotNull(message = "La fecha es obligatoria.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fecha;

    /** Producto que se horneó (un artículo de tipo PRODUCTO_ELABORADO). */
    @NotNull(message = "Elija el producto horneado.")
    private Long articuloProductoId;

    /** Harina pesada en la tanda, en kilogramos. */
    @NotNull(message = "Digite la harina pesada.")
    @Positive(message = "Debe ser mayor que cero.")
    @Digits(integer = 8, fraction = 2, message = "Máximo 2 decimales.")
    private BigDecimal harinaBaseKg;

    /** Panes obtenidos. */
    @NotNull(message = "Digite las unidades obtenidas.")
    @Positive(message = "Debe ser mayor que cero.")
    private Integer unidadesObtenidas;

    /** Quién horneó. */
    @NotBlank(message = "Digite el maestro panadero.")
    private String maestroPanadero;

    /** Observaciones opcionales de la tanda. */
    private String observaciones;

    /** {@code @Valid} aquí hace que también se validen las reglas de cada línea. */
    @Valid
    private List<LineaInsumo> lineas = new ArrayList<>();

    // Getters y setters: Spring los usa para llenar el formulario y Thymeleaf para leerlo.

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public Long getArticuloProductoId() {
        return articuloProductoId;
    }

    public void setArticuloProductoId(Long articuloProductoId) {
        this.articuloProductoId = articuloProductoId;
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

    public List<LineaInsumo> getLineas() {
        return lineas;
    }

    public void setLineas(List<LineaInsumo> lineas) {
        this.lineas = lineas;
    }

    /**
     * Una fila del formulario: un insumo gastado y su cantidad.
     *
     * <p>Es una clase "anidada" ({@code static}): vive dentro de {@link TandaForm} porque
     * solo tiene sentido aquí. El nombre y la unidad viajan en campos ocultos
     * ({@code type="hidden"}) para poder volver a pintar la fila si hay un error.</p>
     */
    public static class LineaInsumo {

        private Long insumoId;       // id del insumo (campo oculto)
        private String nombre;       // solo para mostrar
        private String unidadMedida; // solo para mostrar

        /** Cantidad gastada de este insumo. */
        @NotNull(message = "Digite la cantidad.")
        @PositiveOrZero(message = "No puede ser negativo.")
        @Digits(integer = 8, fraction = 3, message = "Máximo 3 decimales.")
        private BigDecimal cantidad;

        /** Constructor vacío: Spring lo necesita para crear la fila al recibir el formulario. */
        public LineaInsumo() {
        }

        /** Crea la fila vacía de un insumo (lo usa el controlador al pintar el formulario). */
        public LineaInsumo(Long insumoId, String nombre, String unidadMedida) {
            this.insumoId = insumoId;
            this.nombre = nombre;
            this.unidadMedida = unidadMedida;
        }

        // Getters y setters de la fila.

        public Long getInsumoId() {
            return insumoId;
        }

        public void setInsumoId(Long insumoId) {
            this.insumoId = insumoId;
        }

        public String getNombre() {
            return nombre;
        }

        public void setNombre(String nombre) {
            this.nombre = nombre;
        }

        public String getUnidadMedida() {
            return unidadMedida;
        }

        public void setUnidadMedida(String unidadMedida) {
            this.unidadMedida = unidadMedida;
        }

        public BigDecimal getCantidad() {
            return cantidad;
        }

        public void setCantidad(BigDecimal cantidad) {
            this.cantidad = cantidad;
        }
    }
}
