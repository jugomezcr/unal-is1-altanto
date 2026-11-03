package co.edu.unal.sweng1.altanto.negocio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "insumos_config")
public class InsumoConfig {

    @Id
    @Column(name = "articulo_id")
    private Long articuloId;

    @Column(name = "proveedor_habitual_id")
    private Long proveedorHabitualId;

    @Column(name = "umbral_minimo", nullable = false, precision = 12, scale = 3)
    private BigDecimal umbralMinimo;

    @Column(name = "consumo_promedio_diario", nullable = false, precision = 12, scale = 3)
    private BigDecimal consumoPromedioDiario = BigDecimal.ZERO;

    @Column(name = "presentacion_compra", nullable = false, length = 30)
    private String presentacionCompra;

    @Column(name = "factor_conversion_presentacion", nullable = false, precision = 12, scale = 3)
    private BigDecimal factorConversionPresentacion;

    public InsumoConfig() {
    }

    public Long getArticuloId() {
        return articuloId;
    }

    public void setArticuloId(Long articuloId) {
        this.articuloId = articuloId;
    }

    public Long getProveedorHabitualId() {
        return proveedorHabitualId;
    }

    public void setProveedorHabitualId(Long proveedorHabitualId) {
        this.proveedorHabitualId = proveedorHabitualId;
    }

    public BigDecimal getUmbralMinimo() {
        return umbralMinimo;
    }

    public void setUmbralMinimo(BigDecimal umbralMinimo) {
        this.umbralMinimo = umbralMinimo;
    }

    public BigDecimal getConsumoPromedioDiario() {
        return consumoPromedioDiario;
    }

    public void setConsumoPromedioDiario(BigDecimal consumoPromedioDiario) {
        this.consumoPromedioDiario = consumoPromedioDiario;
    }

    public String getPresentacionCompra() {
        return presentacionCompra;
    }

    public void setPresentacionCompra(String presentacionCompra) {
        this.presentacionCompra = presentacionCompra;
    }

    public BigDecimal getFactorConversionPresentacion() {
        return factorConversionPresentacion;
    }

    public void setFactorConversionPresentacion(BigDecimal factorConversionPresentacion) {
        this.factorConversionPresentacion = factorConversionPresentacion;
    }
}