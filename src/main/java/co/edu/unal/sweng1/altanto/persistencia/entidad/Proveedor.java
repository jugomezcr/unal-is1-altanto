package co.edu.unal.sweng1.altanto.persistencia.entidad;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

/**
 * Proveedor de la panadería (tabla {@code proveedores}). Lo usará CU-02 para saber a quién
 * pedirle cada insumo.
 *
 * <p>{@code @Entity}: un objeto de esta clase = una fila de la tabla. La tabla la crea
 * {@code schema.sql}; Hibernate solo verifica que coincida ({@code ddl-auto=validate}).
 * Ver {@link ConciliacionDiaria} para la explicación completa de las anotaciones.</p>
 */
@Entity
@Table(name = "proveedores")
public class Proveedor {

    /** Llave primaria; la asigna la secuencia {@code proveedor_seq} al guardar. */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "proveedor_seq")
    @SequenceGenerator(name = "proveedor_seq", sequenceName = "proveedor_seq", allocationSize = 1)
    private Long id;

    /** NIT de la empresa; {@code unique = true}: no puede repetirse. */
    @Column(name = "nit", nullable = false, unique = true, length = 20)
    private String nit;

    /** Nombre legal de la empresa. */
    @Column(name = "razon_social", nullable = false, length = 150)
    private String razonSocial;

    /** Persona a quien se le hace el pedido (opcional). */
    @Column(name = "nombre_contacto", length = 100)
    private String nombreContacto;

    @Column(name = "telefono", nullable = false, length = 30)
    private String telefono;

    /** Qué vende (harinas, lácteos...). */
    @Column(name = "categoria_suministro", nullable = false, length = 50)
    private String categoriaSuministro;

    /** Borrado lógico: en vez de eliminar el proveedor, se marca inactivo. */
    @Column(name = "activo", nullable = false)
    private Boolean activo = Boolean.TRUE;

    /** Constructor vacío que exige JPA para crear el objeto al leer la fila. */
    public Proveedor() {
    }

    // Getters y setters: JPA y Thymeleaf leen y escriben los campos a través de ellos.

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNit() {
        return nit;
    }

    public void setNit(String nit) {
        this.nit = nit;
    }

    public String getRazonSocial() {
        return razonSocial;
    }

    public void setRazonSocial(String razonSocial) {
        this.razonSocial = razonSocial;
    }

    public String getNombreContacto() {
        return nombreContacto;
    }

    public void setNombreContacto(String nombreContacto) {
        this.nombreContacto = nombreContacto;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getCategoriaSuministro() {
        return categoriaSuministro;
    }

    public void setCategoriaSuministro(String categoriaSuministro) {
        this.categoriaSuministro = categoriaSuministro;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}
