package co.edu.unal.sweng1.altanto.negocio;

/**
 * Estados posibles de una conciliación diaria (CU-01).
 *
 * <p>Un {@code enum} en Java es un tipo con un conjunto CERRADO de valores. Usarlo en
 * lugar de un {@code String} evita errores de digitación ("Confirmada" vs "CONFIRMADA"):
 * el compilador solo deja usar los valores declarados aquí.</p>
 *
 * <p>En la base de datos se guarda como texto (columna {@code estado VARCHAR(20)} de
 * {@code conciliaciones_diarias}) gracias a {@code @Enumerated(EnumType.STRING)} en la
 * entidad {@link ConciliacionDiaria}.</p>
 */
public enum EstadoConciliacion {

    /**
     * El Administrador ya digitó lo vendido y lo contado (CU-01-HU-01), pero todavía
     * no ha confirmado. Las existencias de los artículos NO han cambiado.
     */
    BORRADOR,

    /**
     * El Administrador revisó las diferencias y confirmó (CU-01-HU-02). Lo contado
     * quedó como existencia de partida del día siguiente.
     */
    CONFIRMADA
}
