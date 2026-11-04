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

/**
 * Una línea de la conciliación: lo que DEBERÍA haber de un artículo frente a lo que SE
 * CONTÓ (tabla {@code detalles_conciliacion}).
 *
 * <pre>
 *   existencia esperada = existencia anterior − vendido
 *   diferencia          = contado − existencia esperada
 * </pre>
 * <p>Diferencia negativa = falta mercancía. Positiva = sobra. Cero = cuadra.</p>
 *
 * <p>La tabla también tiene {@code cantidad_recibida} (compras, CU-04). No se mapea aquí
 * todavía: la base de datos le pone 0 por defecto.</p>
 */
@Entity
@Table(name = "detalles_conciliacion")
public class DetalleConciliacion {

    /** Llave primaria; la asigna la secuencia {@code detalle_conciliacion_seq}. */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "detalle_conciliacion_seq")
    @SequenceGenerator(name = "detalle_conciliacion_seq", sequenceName = "detalle_conciliacion_seq", allocationSize = 1)
    private Long id;

    /**
     * {@code @ManyToOne}: MUCHAS líneas pertenecen a UNA conciliación.
     * {@code @JoinColumn}: la columna de la llave foránea. En Java se usa el objeto, no el número.
     */
    @ManyToOne
    @JoinColumn(name = "conciliacion_id", nullable = false)
    private ConciliacionDiaria conciliacion;

    /** Artículo conciliado (insumo o producto de reventa). */
    @ManyToOne
    @JoinColumn(name = "articulo_id", nullable = false)
    private Articulo articulo;

    /** Existencia del artículo antes de conciliar (punto de partida). */
    @Column(name = "existencia_anterior", nullable = false, precision = 12, scale = 3)
    private BigDecimal existenciaAnterior;

    /** Lo vendido según el reporte Z (0 para insumos). */
    @Column(name = "cantidad_salida", nullable = false, precision = 12, scale = 3)
    private BigDecimal cantidadSalida = BigDecimal.ZERO;

    /** anterior − vendido. Lo calcula {@link #calcular()}. */
    @Column(name = "existencia_esperada", nullable = false, precision = 12, scale = 3)
    private BigDecimal existenciaEsperada;

    /** Lo que el Administrador contó en bodega y nevera. */
    @Column(name = "cantidad_contada", nullable = false, precision = 12, scale = 3)
    private BigDecimal cantidadContada;

    /** contado − esperado. */
    @Column(name = "diferencia", nullable = false, precision = 12, scale = 3)
    private BigDecimal diferencia;

    /** Constructor vacío que exige JPA. */
    public DetalleConciliacion() {
    }

    /**
     * Calcula la existencia esperada y la diferencia. Se llama después de llenar
     * anterior, salida y contada.
     *
     * <p>{@code BigDecimal} (números exactos, sin errores de redondeo como {@code double})
     * no usa + ni −: usa {@code add} y {@code subtract}, que devuelven un número nuevo.</p>
     */
    public void calcular() {
        this.existenciaEsperada = existenciaAnterior.subtract(cantidadSalida);
        this.diferencia = cantidadContada.subtract(existenciaEsperada);
    }

    /**
     * {@code true} si lo contado no cuadra. Se usa {@code compareTo} y no {@code equals}:
     * con {@code BigDecimal}, {@code equals} también compara decimales (0.000 ≠ 0).
     */
    public boolean tieneDiferencia() {
        return diferencia.compareTo(BigDecimal.ZERO) != 0;
    }

    // Getters y setters: JPA y Thymeleaf leen y escriben los campos a través de ellos.

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ConciliacionDiaria getConciliacion() {
        return conciliacion;
    }

    public void setConciliacion(ConciliacionDiaria conciliacion) {
        this.conciliacion = conciliacion;
    }

    public Articulo getArticulo() {
        return articulo;
    }

    public void setArticulo(Articulo articulo) {
        this.articulo = articulo;
    }

    public BigDecimal getExistenciaAnterior() {
        return existenciaAnterior;
    }

    public void setExistenciaAnterior(BigDecimal existenciaAnterior) {
        this.existenciaAnterior = existenciaAnterior;
    }

    public BigDecimal getCantidadSalida() {
        return cantidadSalida;
    }

    public void setCantidadSalida(BigDecimal cantidadSalida) {
        this.cantidadSalida = cantidadSalida;
    }

    public BigDecimal getExistenciaEsperada() {
        return existenciaEsperada;
    }

    public BigDecimal getCantidadContada() {
        return cantidadContada;
    }

    public void setCantidadContada(BigDecimal cantidadContada) {
        this.cantidadContada = cantidadContada;
    }

    public BigDecimal getDiferencia() {
        return diferencia;
    }
}
