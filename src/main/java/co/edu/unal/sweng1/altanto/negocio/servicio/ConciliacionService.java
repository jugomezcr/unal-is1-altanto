package co.edu.unal.sweng1.altanto.negocio.servicio;

import co.edu.unal.sweng1.altanto.negocio.dto.CierrePrevio;
import co.edu.unal.sweng1.altanto.negocio.dto.ConteoArticulo;
import co.edu.unal.sweng1.altanto.negocio.dto.InsumoEnAlerta;
import co.edu.unal.sweng1.altanto.negocio.excepcion.ConciliacionDuplicadaException;
import co.edu.unal.sweng1.altanto.negocio.excepcion.ConciliacionNoEncontradaException;
import co.edu.unal.sweng1.altanto.negocio.excepcion.ReglaDeNegocioException;
import co.edu.unal.sweng1.altanto.persistencia.entidad.Articulo;
import co.edu.unal.sweng1.altanto.persistencia.entidad.ConciliacionDiaria;
import co.edu.unal.sweng1.altanto.persistencia.entidad.DetalleConciliacion;
import co.edu.unal.sweng1.altanto.persistencia.entidad.EstadoConciliacion;
import co.edu.unal.sweng1.altanto.persistencia.repositorio.ConciliacionRepository;
import co.edu.unal.sweng1.altanto.persistencia.repositorio.DetalleConciliacionRepository;
import co.edu.unal.sweng1.altanto.persistencia.repositorio.DetalleTandaInsumoRepository;
import co.edu.unal.sweng1.altanto.persistencia.repositorio.InsumoConfigRepository;
import co.edu.unal.sweng1.altanto.persistencia.repositorio.InsumoRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Lógica del CU-01: Conciliar el día.
 * <ul>
 *   <li><b>CU-01-HU-01</b>: {@link #registrarBorrador}. Guarda lo vendido y lo contado.</li>
 *   <li><b>CU-01-HU-02</b>: {@link #confirmar} y {@link #descartar}. Confirma la
 *       conciliación (o la descarta) después de revisar las diferencias.</li>
 * </ul>
 *
 * <p><b>{@code @Service}</b> marca la clase como un "servicio" de Spring: Spring crea
 * UN solo objeto de esta clase al arrancar y se lo entrega a quien lo pida (por ejemplo,
 * al {@code ConciliacionController}). Esto se llama <b>inyección de dependencias</b>:
 * nadie escribe {@code new ConciliacionService(...)}.</p>
 *
 * <p>Arquitectura (igual que el repositorio de referencia): el controlador
 * ({@code presentacion}) llama a este servicio ({@code negocio}) y este usa los
 * repositorios ({@code persistencia}). El controlador nunca toca los repositorios.</p>
 */
@Service
public class ConciliacionService {

    /** Tipos de artículo que se concilian (ver constantes en {@link Articulo}). */
    private static final List<String> TIPOS_CONCILIABLES =
            List.of(Articulo.TIPO_INSUMO, Articulo.TIPO_PRODUCTO_REVENTA);

    // "final": se asignan una sola vez, en el constructor, y no cambian después.
    private final ConciliacionRepository conciliacionRepository;
    private final DetalleConciliacionRepository detalleRepository;
    private final InsumoRepository articuloRepository;
    private final DetalleTandaInsumoRepository detalleTandaRepository;
    private final InsumoConfigRepository insumoConfigRepository;

    /**
     * Inyección por constructor: Spring ve que el constructor pide estos 5
     * repositorios, los busca entre los objetos que creó y los pasa aquí solo.
     */
    public ConciliacionService(ConciliacionRepository conciliacionRepository,
                               DetalleConciliacionRepository detalleRepository,
                               InsumoRepository articuloRepository,
                               DetalleTandaInsumoRepository detalleTandaRepository,
                               InsumoConfigRepository insumoConfigRepository) {
        this.conciliacionRepository = conciliacionRepository;
        this.detalleRepository = detalleRepository;
        this.articuloRepository = articuloRepository;
        this.detalleTandaRepository = detalleTandaRepository;
        this.insumoConfigRepository = insumoConfigRepository;
    }

    // =========================================================================
    // Consultas (solo leen)
    // =========================================================================

    /**
     * Artículos que aparecen en el formulario de conciliación: insumos y productos de
     * reventa activos. El pan elaborado no se concilia (ver {@link Articulo#TIPO_PRODUCTO_ELABORADO}).
     *
     * <p>{@code @Transactional(readOnly = true)}: abre una transacción de solo lectura.
     * Es más liviana y deja claro que este método no modifica nada.</p>
     */
    @Transactional(readOnly = true)
    public List<Articulo> articulosAConciliar() {
        return articuloRepository.findByActivoTrueAndTipoArticuloInOrderByTipoArticuloAscNombreAsc(TIPOS_CONCILIABLES);
    }

    /** Conciliación de una fecha, si existe (para la extensión 1a). */
    @Transactional(readOnly = true)
    public Optional<ConciliacionDiaria> buscarPorFecha(LocalDate fecha) {
        return conciliacionRepository.findByFechaConciliacion(fecha);
    }

    /**
     * @throws ConciliacionNoEncontradaException si no existe.
     */
    @Transactional(readOnly = true)
    public ConciliacionDiaria buscar(Long id) {
        // orElseThrow: si el Optional viene vacío, lanza la excepción indicada.
        // "() -> new ..." es una lambda: una función corta que se ejecuta solo si hace falta.
        return conciliacionRepository.findById(id)
                .orElseThrow(() -> new ConciliacionNoEncontradaException(id));
    }

    /** Líneas por artículo de una conciliación (para la vista de diferencias, CU-01-HU-02). */
    @Transactional(readOnly = true)
    public List<DetalleConciliacion> detalles(Long conciliacionId) {
        return detalleRepository.buscarPorConciliacion(conciliacionId);
    }

    /**
     * Cierres previos: todas las conciliaciones, de la más reciente a la más antigua, con
     * cuántos artículos tiene cada una y cuántos no cuadraron. Cumple el compromiso de la
     * Entrega 4 para CU-01-HU-02: "permite confirmar el cierre diario y consultar cierres
     * previos".
     */
    @Transactional(readOnly = true)
    public List<CierrePrevio> historial() {
        return conciliacionRepository.buscarHistorial();
    }

    /** Insumos en su umbral o por debajo (CU-01-HU-02, criterio 4). */
    @Transactional(readOnly = true)
    public List<InsumoEnAlerta> insumosEnAlerta() {
        return insumoConfigRepository.buscarInsumosEnAlerta();
    }

    // =========================================================================
    // CU-01-HU-01: Digitar lo vendido y lo contado del día
    // =========================================================================

    /**
     * Guarda la conciliación del día en estado BORRADOR, con una línea por artículo.
     *
     * <p><b>{@code @Transactional}</b>: todo lo que hace el método ocurre en UNA
     * transacción de base de datos. Si en cualquier punto se lanza una excepción,
     * Spring hace <i>rollback</i>: deshace todo y no queda guardado nada a medias.
     * Así se cumple el criterio 3 de CU-01-HU-01: "si la operación falla o se cancela,
     * no se guarda nada y los días anteriores no cambian".</p>
     *
     * <p>Las existencias de los artículos NO se tocan aquí; eso pasa al confirmar.</p>
     *
     * @param fecha        día que se concilia.
     * @param totalVentasZ total de ventas del día según el reporte Z.
     * @param conteos      lo vendido y lo contado de cada artículo.
     * @return la conciliación guardada (ya con id).
     * @throws ConciliacionDuplicadaException si ya hay una conciliación de esa fecha (1a).
     * @throws ReglaDeNegocioException       si algún dato no es válido.
     */
    @Transactional
    public ConciliacionDiaria registrarBorrador(LocalDate fecha, BigDecimal totalVentasZ, List<ConteoArticulo> conteos) {
        validarFecha(fecha);
        if (totalVentasZ == null || totalVentasZ.signum() < 0) {
            throw new ReglaDeNegocioException("El total de ventas del reporte Z es obligatorio y no puede ser negativo.");
        }
        if (conteos == null || conteos.isEmpty()) {
            throw new ReglaDeNegocioException("No hay artículos para conciliar.");
        }

        // 1. Cabecera
        ConciliacionDiaria conciliacion = new ConciliacionDiaria();
        conciliacion.setFechaConciliacion(fecha);
        conciliacion.setTotalVentaReporteZ(totalVentasZ);
        conciliacion.setFechaHoraRegistro(LocalDateTime.now());
        // save devuelve el objeto ya guardado, con el id que asignó la secuencia.
        conciliacion = conciliacionRepository.save(conciliacion);

        // 2. Una línea por artículo
        for (ConteoArticulo conteo : conteos) {
            detalleRepository.save(crearDetalle(conciliacion, conteo));
        }
        return conciliacion;
    }

    /**
     * Reglas de la fecha:
     * <ol>
     *   <li>Obligatoria y no futura (no se puede contar lo que todavía no pasó).</li>
     *   <li>Sin otra conciliación ese día (extensión 1a).</li>
     *   <li>Posterior a la última conciliación confirmada. Si no, confirmarla
     *       reescribiría existencias de días ya cerrados, y la garantía mínima dice que
     *       "los días anteriores no cambian".</li>
     * </ol>
     */
    private void validarFecha(LocalDate fecha) {
        if (fecha == null) {
            throw new ReglaDeNegocioException("La fecha de la conciliación es obligatoria.");
        }
        if (fecha.isAfter(LocalDate.now())) {
            throw new ReglaDeNegocioException("No se puede conciliar una fecha futura (" + fecha + ").");
        }
        // ifPresent: ejecuta la lambda solo si el Optional trae algo.
        conciliacionRepository.findByFechaConciliacion(fecha).ifPresent(existente -> {
            throw new ConciliacionDuplicadaException(fecha, existente.getId());
        });
        validarPosteriorAUltimaConfirmada(fecha);
    }

    /** Regla 3 de arriba: la fecha debe ser posterior a la última conciliación CONFIRMADA. */
    private void validarPosteriorAUltimaConfirmada(LocalDate fecha) {
        conciliacionRepository.findFirstByEstadoOrderByFechaConciliacionDesc(EstadoConciliacion.CONFIRMADA)
                .ifPresent(ultima -> {
                    if (!fecha.isAfter(ultima.getFechaConciliacion())) {
                        throw new ReglaDeNegocioException("Ya hay una conciliación confirmada el "
                                + ultima.getFechaConciliacion() + ". Solo se pueden conciliar días posteriores.");
                    }
                });
    }

    /**
     * Arma la línea de un artículo con la fórmula del CU-01 (ver {@link DetalleConciliacion}).
     */
    private DetalleConciliacion crearDetalle(ConciliacionDiaria conciliacion, ConteoArticulo conteo) {
        Articulo articulo = articuloRepository.findById(conteo.articuloId())
                .orElseThrow(() -> new ReglaDeNegocioException("No existe el artículo con id " + conteo.articuloId() + "."));
        if (!TIPOS_CONCILIABLES.contains(articulo.getTipoArticulo())) {
            throw new ReglaDeNegocioException("El artículo " + articulo.getNombre() + " no se concilia.");
        }
        BigDecimal contado = noNegativo(conteo.contado(), "contada", articulo, true);

        // Solo los productos de reventa se venden por caja; los insumos no se venden.
        BigDecimal vendido = Articulo.TIPO_PRODUCTO_REVENTA.equals(articulo.getTipoArticulo())
                ? noNegativo(conteo.vendido(), "vendida", articulo, false)
                : BigDecimal.ZERO;

        // Solo los insumos se consumen en tandas (CU-03).
        BigDecimal consumidoEnTandas = Articulo.TIPO_INSUMO.equals(articulo.getTipoArticulo())
                ? detalleTandaRepository.sumarConsumoDelDia(articulo.getId(), conciliacion.getFechaConciliacion())
                : BigDecimal.ZERO;

        DetalleConciliacion detalle = new DetalleConciliacion();
        detalle.setConciliacion(conciliacion);
        detalle.setArticulo(articulo);
        detalle.setExistenciaAnterior(existenciaAnterior(articulo, conciliacion.getFechaConciliacion()));
        // Recepciones de pedidos y compras de emergencia: llegan con CU-04 (Sprint 2).
        detalle.setCantidadRecibida(BigDecimal.ZERO);
        detalle.setCantidadSalida(vendido.add(consumidoEnTandas));
        detalle.setCantidadContada(contado);
        detalle.calcular();
        return detalle;
    }

    /**
     * Existencia de partida: lo contado en la última conciliación confirmada anterior a
     * la fecha. Si el artículo nunca se ha conciliado, se usa su existencia actual.
     *
     * <p>Se usa lo CONTADO y no la existencia actual porque, desde el Sprint 2, las
     * tandas (CU-03-HU-02) y las recepciones (CU-04) cambiarán la existencia durante el
     * día. Si se partiera de ella, esos movimientos se contarían dos veces.</p>
     */
    private BigDecimal existenciaAnterior(Articulo articulo, LocalDate fecha) {
        List<DetalleConciliacion> ultimo = detalleRepository.buscarUltimoConteo(
                articulo.getId(), EstadoConciliacion.CONFIRMADA, fecha, Limit.of(1));
        return ultimo.isEmpty() ? articulo.getExistenciaActual() : ultimo.get(0).getCantidadContada();
    }

    /** Valida que una cantidad digitada no sea negativa (y, si es obligatoria, que exista). */
    private BigDecimal noNegativo(BigDecimal valor, String campo, Articulo articulo, boolean obligatorio) {
        if (valor == null) {
            if (obligatorio) {
                throw new ReglaDeNegocioException("Falta la cantidad " + campo + " de " + articulo.getNombre() + ".");
            }
            return BigDecimal.ZERO;
        }
        if (valor.signum() < 0) {
            throw new ReglaDeNegocioException("La cantidad " + campo + " de " + articulo.getNombre() + " no puede ser negativa.");
        }
        return valor;
    }

    // =========================================================================
    // CU-01-HU-02: Ver las diferencias y confirmar la conciliación
    // =========================================================================

    /**
     * Confirma la conciliación (CU-01, pasos 6 y 7):
     * <ol>
     *   <li>Cambia el estado a CONFIRMADA.</li>
     *   <li>Deja lo contado como existencia actual de cada artículo (criterio 3).</li>
     *   <li>Devuelve los insumos que quedaron en su umbral o por debajo, para mostrar
     *       la alerta de reabastecimiento (criterio 4).</li>
     * </ol>
     *
     * <p>No hace falta llamar {@code save} para los artículos: los objetos que se leen
     * dentro de una transacción quedan "administrados" por Hibernate, que detecta los
     * cambios (<i>dirty checking</i>) y hace los {@code UPDATE} al final de la
     * transacción.</p>
     *
     * @return insumos en alerta después de confirmar (lista vacía si no hay ninguno).
     */
    @Transactional
    public List<InsumoEnAlerta> confirmar(Long id) {
        ConciliacionDiaria conciliacion = buscar(id);
        List<DetalleConciliacion> detalles = detalleRepository.buscarPorConciliacion(id);
        if (detalles.isEmpty()) {
            throw new ReglaDeNegocioException("La conciliación del " + conciliacion.getFechaConciliacion()
                    + " no tiene artículos; descártela y regístrela de nuevo.");
        }
        // Por si mientras este borrador esperaba se confirmó un día posterior.
        if (conciliacion.esBorrador()) {
            validarPosteriorAUltimaConfirmada(conciliacion.getFechaConciliacion());
        }
        conciliacion.confirmar(); // lanza excepción si ya estaba confirmada

        for (DetalleConciliacion detalle : detalles) {
            detalle.getArticulo().setExistenciaActual(detalle.getCantidadContada());
        }
        // Hibernate guarda los cambios pendientes antes de esta consulta, así que la
        // alerta ya ve las existencias nuevas.
        return insumoConfigRepository.buscarInsumosEnAlerta();
    }

    /**
     * Descarta un borrador: lo borra con sus líneas, como si no se hubiera registrado.
     * Es la opción "cancelar" del CU-01 una vez guardado el borrador (garantía mínima).
     *
     * @throws ReglaDeNegocioException si ya está confirmada (los días cerrados no cambian).
     */
    @Transactional
    public void descartar(Long id) {
        ConciliacionDiaria conciliacion = buscar(id);
        if (!conciliacion.esBorrador()) {
            throw new ReglaDeNegocioException("No se puede descartar una conciliación confirmada.");
        }
        // Primero las líneas (hijas) y luego la cabecera, por la llave foránea.
        detalleRepository.deleteAll(detalleRepository.buscarPorConciliacion(id));
        conciliacionRepository.delete(conciliacion);
    }
}
