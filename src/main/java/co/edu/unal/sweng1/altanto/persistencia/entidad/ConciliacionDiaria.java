package co.edu.unal.sweng1.altanto.persistencia.entidad;

import co.edu.unal.sweng1.altanto.negocio.excepcion.ReglaDeNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Cabecera de la conciliación de UN día (CU-01). Las líneas por artículo están en
 * {@link DetalleConciliacion}.
 *
 * <p>{@code @Entity}: cada objeto de esta clase es una fila de una tabla.
 * {@code @Table}: cuál tabla ({@code conciliaciones_diarias}, creada en {@code schema.sql}).
 * Es como un modelo de SQLAlchemy o Django.</p>
 */
@Entity
@Table(name = "conciliaciones_diarias")
public class ConciliacionDiaria {

    /**
     * Llave primaria. {@code @GeneratedValue(SEQUENCE)}: al guardar, Hibernate pide el
     * siguiente número a la secuencia {@code conciliacion_seq}. Nunca se asigna a mano.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "conciliacion_seq")
    @SequenceGenerator(name = "conciliacion_seq", sequenceName = "conciliacion_seq", allocationSize = 1)
    private Long id;

    /** Día que se concilia. {@code unique}: solo una conciliación por fecha. */
    @Column(name = "fecha_conciliacion", nullable = false, unique = true)
    private LocalDate fechaConciliacion;

    /** Total de ventas del día según el reporte Z de la caja (en pesos). */
    @Column(name = "total_venta_reporte_z", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalVentaReporteZ;

    /**
     * BORRADOR o CONFIRMADA. {@code EnumType.STRING} guarda el texto ("BORRADOR") y no la
     * posición (0, 1), para que la tabla se pueda leer.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoConciliacion estado = EstadoConciliacion.BORRADOR;

    /** Constructor vacío: JPA lo usa para crear el objeto al leer una fila. */
    public ConciliacionDiaria() {
    }

    /** {@code true} si todavía no se ha confirmado. Thymeleaf lo usa para mostrar el botón. */
    public boolean esBorrador() {
        return estado == EstadoConciliacion.BORRADOR;
    }

    /**
     * Pasa de BORRADOR a CONFIRMADA. La regla vive aquí, en la entidad, para que
     * cualquier parte del sistema que confirme pase por la misma validación.
     *
     * @throws ReglaDeNegocioException si ya estaba confirmada.
     */
    public void confirmar() {
        if (!esBorrador()) {
            throw new ReglaDeNegocioException("La conciliación del " + fechaConciliacion + " ya está confirmada.");
        }
        this.estado = EstadoConciliacion.CONFIRMADA;
    }

    // Getters y setters: JPA y Thymeleaf leen y escriben los campos a través de ellos.

    public Long getId() {
        return id;
    }

    public LocalDate getFechaConciliacion() {
        return fechaConciliacion;
    }

    public void setFechaConciliacion(LocalDate fechaConciliacion) {
        this.fechaConciliacion = fechaConciliacion;
    }

    public BigDecimal getTotalVentaReporteZ() {
        return totalVentaReporteZ;
    }

    public void setTotalVentaReporteZ(BigDecimal totalVentaReporteZ) {
        this.totalVentaReporteZ = totalVentaReporteZ;
    }

    public EstadoConciliacion getEstado() {
        return estado;
    }
}
