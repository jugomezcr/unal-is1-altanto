package co.edu.unal.sweng1.altanto.negocio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "conciliaciones_diarias")
public class ConciliacionDiaria {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "conciliacion_seq")
    @SequenceGenerator(name = "conciliacion_seq", sequenceName = "conciliacion_seq", allocationSize = 1)
    private Long id;

    @Column(name = "fecha_conciliacion", nullable = false, unique = true)
    private LocalDate fechaConciliacion;

    @Column(name = "total_venta_reporte_z", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalVentaReporteZ;

    @Column(name = "fecha_hora_registro")
    private LocalDateTime fechaHoraRegistro;

    @Column(name = "estado", nullable = false, length = 20)
    private String estado = "BORRADOR";

    @Column(name = "observaciones", length = 500)
    private String observaciones;

    public ConciliacionDiaria() {
    }

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

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}