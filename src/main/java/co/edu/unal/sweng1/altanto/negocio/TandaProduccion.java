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
import java.time.LocalDate;

@Entity
@Table(name = "tandas_produccion")
public class TandaProduccion {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tanda_seq")
    @SequenceGenerator(name = "tanda_seq", sequenceName = "tanda_seq", allocationSize = 1)
    private Long id;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @ManyToOne
    @JoinColumn(name = "articulo_producto_id", nullable = false)
    private Articulo articuloProducto;

    @Column(name = "harina_base_kg", nullable = false, precision = 10, scale = 2)
    private BigDecimal harinaBaseKg;

    @Column(name = "unidades_obtenidas", nullable = false)
    private Integer unidadesObtenidas;

    @Column(name = "maestro_panadero", nullable = false, length = 100)
    private String maestroPanadero;

    @Column(name = "observaciones", length = 255)
    private String observaciones;

    public TandaProduccion() {
    }

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