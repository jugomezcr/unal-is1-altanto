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
 * Una compra de emergencia (tabla {@code compras_emergencia}, CU-04-HU-02): se compró un
 * artículo fuera del pedido normal, y queda marcada con lo que costó de más.
 * Es un "registro": se crea una vez y no se modifica (por eso no tiene setters).
 */
@Entity
@Table(name = "compras_emergencia")
public class CompraEmergencia {

    /** Llave primaria; la asigna la secuencia {@code compra_emergencia_seq}. */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "compra_emergencia_seq")
    @SequenceGenerator(name = "compra_emergencia_seq", sequenceName = "compra_emergencia_seq",
            allocationSize = 1)
    private Long id;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    /** Artículo comprado. {@code @ManyToOne}: muchas compras pueden ser del mismo artículo. */
    @ManyToOne
    @JoinColumn(name = "articulo_id", nullable = false)
    private Articulo articulo;

    /** Cantidad que realmente llegó, en la unidad del artículo. */
    @Column(name = "cantidad_recibida", nullable = false, precision = 12, scale = 3)
    private BigDecimal cantidadRecibida;

    /** Precio por unidad que se pagó en la emergencia. */
    @Column(name = "precio_pagado", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioPagado;

    /** Precio por unidad que cobra el mayorista habitual. */
    @Column(name = "precio_habitual", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioHabitual;

    /** (pagado − habitual) × cantidad, en pesos. */
    @Column(name = "sobrecosto_pesos", nullable = false, precision = 18, scale = 2)
    private BigDecimal sobrecostoPesos;

    /** (pagado − habitual) / habitual × 100. */
    @Column(name = "sobrecosto_porcentaje", nullable = false, precision = 16, scale = 2)
    private BigDecimal sobrecostoPorcentaje;

    /** Constructor vacío que exige JPA. */
    protected CompraEmergencia() {
    }

    /** Constructor que usa el servicio: crea la compra con todos sus datos. */
    public CompraEmergencia(LocalDate fecha, Articulo articulo, BigDecimal cantidadRecibida,
                            BigDecimal precioPagado, BigDecimal precioHabitual,
                            BigDecimal sobrecostoPesos, BigDecimal sobrecostoPorcentaje) {
        this.fecha = fecha;
        this.articulo = articulo;
        this.cantidadRecibida = cantidadRecibida;
        this.precioPagado = precioPagado;
        this.precioHabitual = precioHabitual;
        this.sobrecostoPesos = sobrecostoPesos;
        this.sobrecostoPorcentaje = sobrecostoPorcentaje;
    }

    // Solo getters (Thymeleaf y los servicios los usan para leer).

    public Long getId() {
        return id;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public Articulo getArticulo() {
        return articulo;
    }

    public BigDecimal getCantidadRecibida() {
        return cantidadRecibida;
    }

    public BigDecimal getPrecioPagado() {
        return precioPagado;
    }

    public BigDecimal getPrecioHabitual() {
        return precioHabitual;
    }

    public BigDecimal getSobrecostoPesos() {
        return sobrecostoPesos;
    }

    public BigDecimal getSobrecostoPorcentaje() {
        return sobrecostoPorcentaje;
    }
}
