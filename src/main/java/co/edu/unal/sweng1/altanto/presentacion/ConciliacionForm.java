package co.edu.unal.sweng1.altanto.presentacion;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * Datos del formulario "Conciliar el día" (CU-01-HU-01), tal como llegan del navegador.
 *
 * <p>Spring MVC llena este objeto solo: por cada campo {@code <input name="...">} del
 * HTML busca el setter con ese nombre (por ejemplo, {@code name="totalVentasZ"} llama a
 * {@code setTotalVentasZ}). Para la lista usa nombres como {@code lineas[0].contado}.
 * Esto se llama <b>data binding</b>.</p>
 *
 * <p>Las anotaciones de {@code jakarta.validation} ({@code @NotNull},
 * {@code @PositiveOrZero}, ...) son reglas de formato que Spring revisa antes de llamar
 * al servicio, gracias a {@code @Valid} en el controlador. Si alguna falla, el error se
 * muestra junto al campo. Las reglas de NEGOCIO (fecha duplicada, etc.) están en el
 * servicio.</p>
 *
 * <p>Esta clase está en {@code presentacion} porque es un detalle de la pantalla. El
 * servicio no la conoce: recibe {@code ConteoArticulo}.</p>
 */
public class ConciliacionForm {

    /** {@code @DateTimeFormat(ISO.DATE)}: el {@code <input type="date">} envía "2026-11-03". */
    @NotNull(message = "La fecha es obligatoria.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fecha;

    @NotNull(message = "El total del reporte Z es obligatorio.")
    @PositiveOrZero(message = "No puede ser negativo.")
    @Digits(integer = 10, fraction = 2, message = "Máximo 2 decimales.")
    private BigDecimal totalVentasZ;

    /** {@code @Valid} aquí hace que también se validen las reglas de cada línea. */
    @Valid
    private List<LineaConteo> lineas = new ArrayList<>();

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public BigDecimal getTotalVentasZ() {
        return totalVentasZ;
    }

    public void setTotalVentasZ(BigDecimal totalVentasZ) {
        this.totalVentasZ = totalVentasZ;
    }

    public List<LineaConteo> getLineas() {
        return lineas;
    }

    public void setLineas(List<LineaConteo> lineas) {
        this.lineas = lineas;
    }

    /**
     * Una fila del formulario: un artículo con lo vendido y lo contado.
     *
     * <p>Es una clase "anidada" ({@code static}): vive dentro de {@link ConciliacionForm}
     * porque solo tiene sentido aquí. Nombre, unidad y tipo viajan en campos ocultos
     * ({@code type="hidden"}) para poder volver a pintar la fila si hay un error.</p>
     */
    public static class LineaConteo {

        private Long articuloId;
        private String nombre;
        private String unidadMedida;
        private String tipoArticulo;

        /** Solo para productos de reventa; los insumos no se venden por caja. */
        @PositiveOrZero(message = "No puede ser negativo.")
        @Digits(integer = 9, fraction = 3, message = "Máximo 3 decimales.")
        private BigDecimal vendido;

        @NotNull(message = "Digite lo contado.")
        @PositiveOrZero(message = "No puede ser negativo.")
        @Digits(integer = 9, fraction = 3, message = "Máximo 3 decimales.")
        private BigDecimal contado;

        /** Constructor vacío: Spring lo necesita para crear la fila al recibir el formulario. */
        public LineaConteo() {
        }

        public LineaConteo(Long articuloId, String nombre, String unidadMedida, String tipoArticulo) {
            this.articuloId = articuloId;
            this.nombre = nombre;
            this.unidadMedida = unidadMedida;
            this.tipoArticulo = tipoArticulo;
        }

        /** Thymeleaf lo usa para decidir si pinta la casilla "Vendido". */
        public boolean esReventa() {
            return co.edu.unal.sweng1.altanto.negocio.Articulo.TIPO_PRODUCTO_REVENTA.equals(tipoArticulo);
        }

        public Long getArticuloId() {
            return articuloId;
        }

        public void setArticuloId(Long articuloId) {
            this.articuloId = articuloId;
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

        public String getTipoArticulo() {
            return tipoArticulo;
        }

        public void setTipoArticulo(String tipoArticulo) {
            this.tipoArticulo = tipoArticulo;
        }

        public BigDecimal getVendido() {
            return vendido;
        }

        public void setVendido(BigDecimal vendido) {
            this.vendido = vendido;
        }

        public BigDecimal getContado() {
            return contado;
        }

        public void setContado(BigDecimal contado) {
            this.contado = contado;
        }
    }
}
