package co.edu.unal.sweng1.altanto.persistencia.entidad;

import co.edu.unal.sweng1.altanto.negocio.excepcion.ReglaDeNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.LocalDate;

/** Cabecera del pedido y estado de su recepción. */
@Entity
@Table(name = "pedidos")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pedido_seq")
    @SequenceGenerator(name = "pedido_seq", sequenceName = "pedido_seq", allocationSize = 1)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "proveedor_id", nullable = false)
    private Proveedor proveedor;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoPedido estado = EstadoPedido.PENDIENTE;

    @Column(name = "fecha_pedido", nullable = false)
    private LocalDate fechaPedido;

    @Column(name = "fecha_recepcion")
    private LocalDate fechaRecepcion;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    public Pedido() {
    }

    public boolean esPendiente() {
        return estado == EstadoPedido.PENDIENTE;
    }

    public void cerrar(LocalDate fechaRecepcion) {
        if (!esPendiente()) {
            throw new ReglaDeNegocioException(
                    "Este pedido ya fue recibido o está siendo procesado.");
        }
        if (fechaRecepcion == null) {
            throw new ReglaDeNegocioException("La fecha de recepción es obligatoria.");
        }
        this.estado = EstadoPedido.CERRADO;
        this.fechaRecepcion = fechaRecepcion;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Proveedor getProveedor() {
        return proveedor;
    }

    public void setProveedor(Proveedor proveedor) {
        this.proveedor = proveedor;
    }

    public EstadoPedido getEstado() {
        return estado;
    }

    public LocalDate getFechaPedido() {
        return fechaPedido;
    }

    public void setFechaPedido(LocalDate fechaPedido) {
        this.fechaPedido = fechaPedido;
    }

    public LocalDate getFechaRecepcion() {
        return fechaRecepcion;
    }

    public Long getVersion() {
        return version;
    }
}
