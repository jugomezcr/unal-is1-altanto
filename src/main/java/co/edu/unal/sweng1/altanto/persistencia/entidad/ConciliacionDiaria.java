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
import java.time.LocalDateTime;

/**
 * Cabecera de la conciliación de UN día (CU-01: Conciliar el día).
 *
 * <p><b>{@code @Entity}</b> le dice a JPA/Hibernate que cada objeto de esta clase
 * corresponde a una fila de una tabla. <b>{@code @Table}</b> indica cuál tabla
 * ({@code conciliaciones_diarias}, definida en {@code schema.sql}).</p>
 *
 * <p>Las líneas por artículo (esperado vs. contado) están en
 * {@link DetalleConciliacion}, que apunta a esta cabecera. Esta clase no tiene la lista
 * de detalles (relación unidireccional, ver docs/hito-4/persistencia-jpa-y-repositorios.md,
 * sección 3.4). Los detalles se consultan con {@code DetalleConciliacionRepository}.</p>
 */
@Entity
@Table(name = "conciliaciones_diarias")
public class ConciliacionDiaria {

    /**
     * Llave primaria. {@code @GeneratedValue(SEQUENCE)} hace que Hibernate pida el
     * siguiente número a la secuencia {@code conciliacion_seq} de la base de datos al
     * guardar un objeto nuevo. Nunca se asigna a mano.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "conciliacion_seq")
    @SequenceGenerator(name = "conciliacion_seq", sequenceName = "conciliacion_seq", allocationSize = 1)
    private Long id;

    /** Día que se concilia. {@code unique = true}: solo una conciliación por fecha (extensión 1a). */
    @Column(name = "fecha_conciliacion", nullable = false, unique = true)
    private LocalDate fechaConciliacion;

    /** Total de ventas del día según el reporte Z de la caja registradora (en pesos). */
    @Column(name = "total_venta_reporte_z", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalVentaReporteZ;

    /** Momento en que se registró (se llena al guardar el borrador). */
    @Column(name = "fecha_hora_registro")
    private LocalDateTime fechaHoraRegistro;

    /**
     * BORRADOR o CONFIRMADA. {@code @Enumerated(EnumType.STRING)} guarda el NOMBRE del
     * valor ("BORRADOR") en la columna de texto, no su posición (0, 1). Así la base de
     * datos se puede leer y no se rompe si algún día se reordena el enum.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoConciliacion estado = EstadoConciliacion.BORRADOR;

    /** Nota libre del Administrador (opcional). */
    @Column(name = "observaciones", length = 500)
    private String observaciones;

    /**
     * Constructor vacío: JPA lo exige para poder crear el objeto al leer una fila de la
     * base de datos (luego llena los campos uno por uno).
     */
    public ConciliacionDiaria() {
    }

    // -------------------------------------------------------------------------
    // Comportamiento de negocio
    // -------------------------------------------------------------------------

    /** @return {@code true} si todavía se puede confirmar o descartar. */
    public boolean esBorrador() {
        return estado == EstadoConciliacion.BORRADOR;
    }

    /**
     * Pasa la conciliación de BORRADOR a CONFIRMADA (CU-01-HU-02, paso 6 del CU-01).
     *
     * <p>La regla vive aquí, en la entidad, y no en el controlador. Así, cualquier parte
     * del sistema que intente confirmar pasa por la misma validación.</p>
     *
     * @throws ReglaDeNegocioException si ya estaba confirmada.
     */
    public void confirmar() {
        if (!esBorrador()) {
            throw new ReglaDeNegocioException(
                    "La conciliación del " + fechaConciliacion + " ya está confirmada.");
        }
        this.estado = EstadoConciliacion.CONFIRMADA;
    }

    // -------------------------------------------------------------------------
    // Getters y setters: métodos para leer (get) y modificar (set) cada campo.
    // JPA y Thymeleaf los usan para acceder a los datos.
    // -------------------------------------------------------------------------

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public LocalDateTime getFechaHoraRegistro() {
        return fechaHoraRegistro;
    }

    public void setFechaHoraRegistro(LocalDateTime fechaHoraRegistro) {
        this.fechaHoraRegistro = fechaHoraRegistro;
    }

    public EstadoConciliacion getEstado() {
        return estado;
    }

    public void setEstado(EstadoConciliacion estado) {
        this.estado = estado;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
