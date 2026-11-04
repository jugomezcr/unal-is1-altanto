package co.edu.unal.sweng1.altanto.negocio.servicio;

import co.edu.unal.sweng1.altanto.negocio.dto.ConteoArticulo;
import co.edu.unal.sweng1.altanto.negocio.excepcion.ConciliacionDuplicadaException;
import co.edu.unal.sweng1.altanto.negocio.excepcion.ConciliacionNoEncontradaException;
import co.edu.unal.sweng1.altanto.negocio.excepcion.ReglaDeNegocioException;
import co.edu.unal.sweng1.altanto.persistencia.entidad.Articulo;
import co.edu.unal.sweng1.altanto.persistencia.entidad.ConciliacionDiaria;
import co.edu.unal.sweng1.altanto.persistencia.entidad.DetalleConciliacion;
import co.edu.unal.sweng1.altanto.persistencia.repositorio.ConciliacionRepository;
import co.edu.unal.sweng1.altanto.persistencia.repositorio.DetalleConciliacionRepository;
import co.edu.unal.sweng1.altanto.persistencia.repositorio.InsumoRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Reglas del CU-01: Conciliar el día.
 * <ul>
 *   <li>CU-01-HU-01: {@link #registrarBorrador} guarda lo vendido y lo contado.</li>
 *   <li>CU-01-HU-02: {@link #confirmar} deja lo contado como existencia nueva.</li>
 * </ul>
 *
 * <p>{@code @Service}: Spring crea UN objeto de esta clase al arrancar y se lo pasa a
 * quien lo pida (el controlador). Nadie escribe {@code new ConciliacionService(...)}.</p>
 */
@Service
public class ConciliacionService {

    private final ConciliacionRepository conciliacionRepository;
    private final DetalleConciliacionRepository detalleRepository;
    private final InsumoRepository articuloRepository;

    /** Spring ve qué pide el constructor y le pasa los repositorios (inyección de dependencias). */
    public ConciliacionService(ConciliacionRepository conciliacionRepository,
                               DetalleConciliacionRepository detalleRepository,
                               InsumoRepository articuloRepository) {
        this.conciliacionRepository = conciliacionRepository;
        this.detalleRepository = detalleRepository;
        this.articuloRepository = articuloRepository;
    }

    // ---------------------------------------------------------------- Consultas

    /** Insumos y productos de reventa activos: los que salen en el formulario. */
    @Transactional(readOnly = true)
    public List<Articulo> articulosAConciliar() {
        return articuloRepository.findByActivoTrueAndTipoArticuloInOrderByTipoArticuloAscNombreAsc(
                List.of(Articulo.TIPO_INSUMO, Articulo.TIPO_PRODUCTO_REVENTA));
    }

    /** La conciliación con ese id, o error si no existe. */
    @Transactional(readOnly = true)
    public ConciliacionDiaria buscar(Long id) {
        // orElseThrow: si no la encuentra, lanza la excepción (como raise en Python).
        return conciliacionRepository.findById(id)
                .orElseThrow(() -> new ConciliacionNoEncontradaException(id));
    }

    /** Las líneas (una por artículo) de una conciliación. */
    @Transactional(readOnly = true)
    public List<DetalleConciliacion> detalles(Long conciliacionId) {
        return detalleRepository.findByConciliacionId(conciliacionId);
    }

    /** Cierres previos: todas las conciliaciones, la más reciente primero. */
    @Transactional(readOnly = true)
    public List<ConciliacionDiaria> historial() {
        return conciliacionRepository.findAllByOrderByFechaConciliacionDesc();
    }

    // ---------------------------------------------------------------- CU-01-HU-01

    /**
     * Guarda la conciliación del día como BORRADOR, con una línea por artículo.
     *
     * <p>{@code @Transactional}: todo pasa en UNA transacción. Si algo lanza una excepción,
     * se deshace todo (rollback) y no queda nada guardado a medias.</p>
     *
     * @throws ConciliacionDuplicadaException si ya hay una conciliación de esa fecha.
     * @throws ReglaDeNegocioException       si la fecha es futura.
     */
    @Transactional
    public ConciliacionDiaria registrarBorrador(LocalDate fecha, BigDecimal totalVentasZ,
                                                List<ConteoArticulo> conteos) {
        if (fecha.isAfter(LocalDate.now())) {
            throw new ReglaDeNegocioException("No se puede conciliar una fecha futura (" + fecha + ").");
        }
        // ifPresent: si ya existe una de esa fecha, ejecuta la lambda (que lanza el error).
        conciliacionRepository.findByFechaConciliacion(fecha).ifPresent(existente -> {
            throw new ConciliacionDuplicadaException(fecha, existente.getId());
        });

        ConciliacionDiaria conciliacion = new ConciliacionDiaria();
        conciliacion.setFechaConciliacion(fecha);
        conciliacion.setTotalVentaReporteZ(totalVentasZ);
        conciliacion = conciliacionRepository.save(conciliacion); // save devuelve el objeto con su id

        for (ConteoArticulo conteo : conteos) {
            detalleRepository.save(crearDetalle(conciliacion, conteo));
        }
        return conciliacion;
    }

    /** Arma la línea de un artículo: anterior, vendido, contado y la diferencia. */
    private DetalleConciliacion crearDetalle(ConciliacionDiaria conciliacion, ConteoArticulo conteo) {
        Articulo articulo = articuloRepository.findById(conteo.articuloId())
                .orElseThrow(() -> new ReglaDeNegocioException("No existe el artículo " + conteo.articuloId() + "."));

        // Los insumos no se venden por caja: su "vendido" llega vacío y cuenta como 0.
        BigDecimal vendido = (conteo.vendido() == null) ? BigDecimal.ZERO : conteo.vendido();

        DetalleConciliacion detalle = new DetalleConciliacion();
        detalle.setConciliacion(conciliacion);
        detalle.setArticulo(articulo);
        detalle.setExistenciaAnterior(articulo.getExistenciaActual());
        detalle.setCantidadSalida(vendido);
        detalle.setCantidadContada(conteo.contado());
        detalle.calcular();
        return detalle;
    }

    // ---------------------------------------------------------------- CU-01-HU-02

    /**
     * Confirma la conciliación: pasa a CONFIRMADA y lo contado queda como existencia
     * actual de cada artículo.
     *
     * <p>No hace falta llamar {@code save} para los artículos: dentro de una transacción,
     * Hibernate detecta los objetos modificados y hace el UPDATE al terminar el método.</p>
     *
     * @throws ReglaDeNegocioException si ya estaba confirmada.
     */
    @Transactional
    public void confirmar(Long id) {
        ConciliacionDiaria conciliacion = buscar(id);
        conciliacion.confirmar(); // la regla "no confirmar dos veces" vive en la entidad

        for (DetalleConciliacion detalle : detalleRepository.findByConciliacionId(id)) {
            detalle.getArticulo().setExistenciaActual(detalle.getCantidadContada());
        }
    }
}
