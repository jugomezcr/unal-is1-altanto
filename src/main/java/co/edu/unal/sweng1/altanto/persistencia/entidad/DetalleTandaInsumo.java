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

@Entity
@Table(name = "detalles_tanda_insumo")
public class DetalleTandaInsumo {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "detalle_tanda_seq")
    @SequenceGenerator(name = "detalle_tanda_seq", sequenceName = "detalle_tanda_seq", allocationSize = 1)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "tanda_id", nullable = false)
    private TandaProduccion tanda;

    @ManyToOne
    @JoinColumn(name = "insumo_id", nullable = false)
    private Articulo insumo;

    @Column(name = "cantidad_consumida", nullable = false, precision = 10, scale = 3)
    private BigDecimal cantidadConsumida;

    public DetalleTandaInsumo() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TandaProduccion getTanda() {
        return tanda;
    }

    public void setTanda(TandaProduccion tanda) {
        this.tanda = tanda;
    }

    public Articulo getInsumo() {
        return insumo;
    }

    public void setInsumo(Articulo insumo) {
        this.insumo = insumo;
    }

    public BigDecimal getCantidadConsumida() {
        return cantidadConsumida;
    }

    public void setCantidadConsumida(BigDecimal cantidadConsumida) {
        this.cantidadConsumida = cantidadConsumida;
    }
}