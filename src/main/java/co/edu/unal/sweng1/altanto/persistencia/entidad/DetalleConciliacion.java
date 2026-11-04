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
 * CONTÓ (CU-01, pasos 4 y 5).
 *
 * <p>Fórmula de la Entrega 2 (CU-01, paso 4, y CU-01-HU-02, criterio 1):</p>
 * <pre>
 *   existencia esperada = existencia anterior + recibido − salida
 *   salida              = vendido (reporte Z) + consumido en tandas
 *   diferencia          = contado − existencia esperada
 * </pre>
 * <p>Diferencia negativa = falta mercancía. Positiva = sobra. Cero = cuadra.</p>
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
     * {@code @JoinColumn} indica la columna de la llave foránea ({@code conciliacion_id}).
     * En Java se trabaja con el objeto completo, no con el número.
     */
    @ManyToOne
    @JoinColumn(name = "conciliacion_id", nullable = false)
    private ConciliacionDiaria conciliacion;

    /** Artículo conciliado (insumo o producto de reventa). */
    @ManyToOne
    @JoinColumn(name = "articulo_id", nullable = false)
    private Articulo articulo;

    /** Lo que quedó contado en la última conciliación confirmada (punto de partida). */
    @Column(name = "existencia_anterior", nullable = false, precision = 12, scale = 3)
    private BigDecimal existenciaAnterior;

    /** Lo recibido de proveedores en el día. Se llenará con CU-04 (Sprint 2); hoy es 0. */
    @Column(name = "cantidad_recibida", nullable = false, precision = 12, scale = 3)
    private BigDecimal cantidadRecibida = BigDecimal.ZERO;

    /** Lo vendido (reporte Z) más lo consumido en las tandas del día. */
    @Column(name = "cantidad_salida", nullable = false, precision = 12, scale = 3)
    private BigDecimal cantidadSalida = BigDecimal.ZERO;

    /** anterior + recibida − salida. Lo calcula {@link #calcular()}. */
    @Column(name = "existencia_esperada", nullable = false, precision = 12, scale = 3)
    private BigDecimal existenciaEsperada;

    /** Lo que el Administrador contó físicamente en bodega y nevera. */
    @Column(name = "cantidad_contada", nullable = false, precision = 12, scale = 3)
    private BigDecimal cantidadContada;

    /** contada − esperada. Negativa = falta; positiva = sobra. */
    @Column(name = "diferencia", nullable = false, precision = 12, scale = 3)
    private BigDecimal diferencia;

    /** Constructor vacío que exige JPA. */
    public DetalleConciliacion() {
    }

    // -------------------------------------------------------------------------
    // Comportamiento de negocio
    // -------------------------------------------------------------------------

    /**
     * Calcula la existencia esperada y la diferencia con la fórmula de la clase.
     * Se llama después de llenar anterior, recibida, salida y contada.
     *
     * <p>{@code BigDecimal} no usa los operadores + y −. Se usan los métodos
     * {@code add} (sumar) y {@code subtract} (restar), que devuelven un número NUEVO
     * (no modifican el original). Ver la sección 3.1 de la doc de persistencia: por qué
     * {@code BigDecimal} y no {@code double}.</p>
     */
    public void calcular() {
        this.existenciaEsperada = existenciaAnterior.add(cantidadRecibida).subtract(cantidadSalida);
        this.diferencia = cantidadContada.subtract(existenciaEsperada);
    }

    /**
     * CU-01-HU-02, criterio 2: "el sistema señala los artículos que tienen diferencia".
     * Se usa {@code compareTo} y no {@code equals}, porque para {@code BigDecimal}
     * {@code equals} también compara la escala (0.000 no "es igual" a 0).
     *
     * @return {@code true} si lo contado no cuadra con lo esperado.
     */
    public boolean tieneDiferencia() {
        return diferencia != null && diferencia.compareTo(BigDecimal.ZERO) != 0;
    }

    // -------------------------------------------------------------------------
    // Getters y setters
    // -------------------------------------------------------------------------

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

    public BigDecimal getCantidadRecibida() {
        return cantidadRecibida;
    }

    public void setCantidadRecibida(BigDecimal cantidadRecibida) {
        this.cantidadRecibida = cantidadRecibida;
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

    public void setExistenciaEsperada(BigDecimal existenciaEsperada) {
        this.existenciaEsperada = existenciaEsperada;
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

    public void setDiferencia(BigDecimal diferencia) {
        this.diferencia = diferencia;
    }
}
