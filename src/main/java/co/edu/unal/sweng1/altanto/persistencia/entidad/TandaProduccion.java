package co.edu.unal.sweng1.altanto.persistencia.entidad;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Una horneada (tabla {@code tandas_produccion}, CU-03): cuánta harina se usó y cuántas
 * unidades salieron. Los insumos gastados van en {@link DetalleTandaInsumo}.
 */
@Entity
@Table(name = "tandas_produccion")
public class TandaProduccion {

    /** Llave primaria; la asigna la secuencia {@code tanda_seq}. */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tanda_seq")
    @SequenceGenerator(name = "tanda_seq", sequenceName = "tanda_seq", allocationSize = 1)
    private Long id;

    /** Día de la horneada; la conciliación suma las tandas de esa fecha. */
    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    /**
     * Producto horneado (un {@link Articulo} de tipo PRODUCTO_ELABORADO).
     * {@code @ManyToOne}: muchas tandas pueden ser del mismo producto.
     */
    @ManyToOne
    @JoinColumn(name = "articulo_producto_id", nullable = false)
    private Articulo articuloProducto;

    /** Harina pesada en la tanda (base del cálculo de rendimiento). */
    @Column(name = "harina_base_kg", nullable = false, precision = 10, scale = 2)
    private BigDecimal harinaBaseKg;

    /** Panes obtenidos. */
    @Column(name = "unidades_obtenidas", nullable = false)
    private Integer unidadesObtenidas;

    /** Quién horneó. */
    @Column(name = "maestro_panadero", nullable = false, length = 100)
    private String maestroPanadero;

    @Column(name = "observaciones", length = 255)
    private String observaciones;

    /** Constructor vacío que exige JPA. */
    public TandaProduccion() {
    }

    // Getters y setters.

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public Articulo getArticuloProducto() {
        return articuloProducto;
    }

    public void setArticuloProducto(Articulo articuloProducto) {
        this.articuloProducto = articuloProducto;
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
}
