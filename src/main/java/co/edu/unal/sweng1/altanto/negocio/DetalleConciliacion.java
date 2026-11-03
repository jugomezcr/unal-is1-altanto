package co.edu.unal.sweng1.altanto.negocio;

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

@Entity
@Table(name = "detalles_conciliacion")
public class DetalleConciliacion {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "detalle_conciliacion_seq")
    @SequenceGenerator(name = "detalle_conciliacion_seq", sequenceName = "detalle_conciliacion_seq", allocationSize = 1)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "conciliacion_id", nullable = false)
    private ConciliacionDiaria conciliacion;

    @ManyToOne
    @JoinColumn(name = "articulo_id", nullable = false)
    private Articulo articulo;

    @Column(name = "existencia_anterior", nullable = false, precision = 12, scale = 3)
    private BigDecimal existenciaAnterior;

    @Column(name = "cantidad_recibida", nullable = false, precision = 12, scale = 3)
    private BigDecimal cantidadRecibida = BigDecimal.ZERO;

    @Column(name = "cantidad_salida", nullable = false, precision = 12, scale = 3)
    private BigDecimal cantidadSalida = BigDecimal.ZERO;

    @Column(name = "existencia_esperada", nullable = false, precision = 12, scale = 3)
    private BigDecimal existenciaEsperada;

    @Column(name = "cantidad_contada", nullable = false, precision = 12, scale = 3)
    private BigDecimal cantidadContada;

    @Column(name = "diferencia", nullable = false, precision = 12, scale = 3)
    private BigDecimal diferencia;

    public DetalleConciliacion() {
    }

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