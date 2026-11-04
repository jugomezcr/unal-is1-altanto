package co.edu.unal.sweng1.altanto.persistencia.entidad;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/**
 * Configuración de reabastecimiento de un insumo (tabla {@code insumos_config}): umbral
 * mínimo, consumo diario y presentación de compra. Lo usan las alertas (CU-01-HU-02 y CU-02).
 *
 * <p>Es una relación 1 a 1 con {@link Articulo}: su llave primaria ES el id del artículo.
 * Por eso no tiene {@code @GeneratedValue} y guarda el id como número, no como
 * {@code @ManyToOne}. Las consultas lo unen a mano ({@code c.articuloId = a.id}).</p>
 */
@Entity
@Table(name = "insumos_config")
public class InsumoConfig {

    /** Id del artículo configurado (llave primaria y foránea a la vez). */
    @Id
    @Column(name = "articulo_id")
    private Long articuloId;

    /** Id del proveedor al que normalmente se le compra (puede ser nulo). */
    @Column(name = "proveedor_habitual_id")
    private Long proveedorHabitualId;

    /** Stock de seguridad: en este valor o por debajo, el insumo entra en alerta. */
    @Column(name = "umbral_minimo", nullable = false, precision = 12, scale = 3)
    private BigDecimal umbralMinimo;

    /** Gasto promedio por día; sirve para calcular los días de cobertura. */
    @Column(name = "consumo_promedio_diario", nullable = false, precision = 12, scale = 3)
    private BigDecimal consumoPromedioDiario = BigDecimal.ZERO;

    /** Cómo se compra (por ejemplo, {@code BULTO_50KG}). */
    @Column(name = "presentacion_compra", nullable = false, length = 30)
    private String presentacionCompra;

    /** Cuántas unidades de medida trae una presentación (bulto de 50 kg → 50). */
    @Column(name = "factor_conversion_presentacion", nullable = false, precision = 12, scale = 3)
    private BigDecimal factorConversionPresentacion;

    /** Constructor vacío que exige JPA. */
    public InsumoConfig() {
    }

    // Getters y setters.

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
