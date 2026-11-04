package co.edu.unal.sweng1.altanto.persistencia.entidad;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/**
 * Cualquier cosa que la panadería guarda o vende (tabla {@code articulos}): insumos,
 * productos de reventa y pan elaborado. Es la tabla central del sistema.
 *
 * <p>Las tres clases de artículo viven en la MISMA tabla y se distinguen por la columna
 * {@code tipo_articulo} (constantes de abajo). Ver {@link ConciliacionDiaria} para las
 * anotaciones JPA.</p>
 */
@Entity
@Table(name = "articulos")
public class Articulo {

    // -------------------------------------------------------------------------
    // Valores posibles de la columna tipo_articulo.
    // "public static final" = constante compartida por toda la clase (no por objeto).
    // Así se escribe Articulo.TIPO_INSUMO en vez de repetir el texto "INSUMO".
    // -------------------------------------------------------------------------

    /** Materia prima del obrador (harina, levadura, margarina...). Se concilia (CU-01). */
    public static final String TIPO_INSUMO = "INSUMO";

    /** Producto que se compra y se revende tal cual (kumis, gaseosas). Se concilia (CU-01). */
    public static final String TIPO_PRODUCTO_REVENTA = "PRODUCTO_REVENTA";

    /**
     * Producto que se hornea en el obrador (pan). Es el producto de una tanda (CU-03).
     * NO se concilia: según la Entrega 2 (HEFESTO-02), el pan no vendido no se registra
     * en el inventario y "la conciliación cubre insumos y productos de reventa".
     */
    public static final String TIPO_PRODUCTO_ELABORADO = "PRODUCTO_ELABORADO";

    /** Llave primaria; la asigna la secuencia {@code articulo_seq}. */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "articulo_seq")
    @SequenceGenerator(name = "articulo_seq", sequenceName = "articulo_seq", allocationSize = 1)
    private Long id;

    /** Código interno único (por ejemplo, {@code INS-HAR-01}). */
    @Column(name = "codigo", nullable = false, unique = true, length = 30)
    private String codigo;

    @Column(name = "nombre", nullable = false, length = 120)
    private String nombre;

    @Column(name = "descripcion", length = 255)
    private String descripcion;

    /** Uno de los {@code TIPO_*} de arriba. */
    @Column(name = "tipo_articulo", nullable = false, length = 30)
    private String tipoArticulo;

    /** KILOGRAMO, GRAMO, UNIDAD... */
    @Column(name = "unidad_medida", nullable = false, length = 20)
    private String unidadMedida;

    /** Cuánto hay hoy. Se actualiza al confirmar una conciliación (CU-01-HU-02). */
    @Column(name = "existencia_actual", nullable = false, precision = 12, scale = 3)
    private BigDecimal existenciaActual = BigDecimal.ZERO;

    /** Borrado lógico: un artículo inactivo no aparece en conciliación ni en alertas. */
    @Column(name = "activo", nullable = false)
    private Boolean activo = Boolean.TRUE;

    /** Constructor vacío que exige JPA. */
    public Articulo() {
    }

    // Getters y setters.

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getTipoArticulo() {
        return tipoArticulo;
    }

    public void setTipoArticulo(String tipoArticulo) {
        this.tipoArticulo = tipoArticulo;
    }

    public String getUnidadMedida() {
        return unidadMedida;
    }

    public void setUnidadMedida(String unidadMedida) {
        this.unidadMedida = unidadMedida;
    }

    public BigDecimal getExistenciaActual() {
        return existenciaActual;
    }

    public void setExistenciaActual(BigDecimal existenciaActual) {
        this.existenciaActual = existenciaActual;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}