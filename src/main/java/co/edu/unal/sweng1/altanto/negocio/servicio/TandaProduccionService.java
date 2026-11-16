package co.edu.unal.sweng1.altanto.negocio.servicio;

import co.edu.unal.sweng1.altanto.negocio.dto.InsumoGastado;
import co.edu.unal.sweng1.altanto.negocio.excepcion.InsumoInsuficienteException;
import co.edu.unal.sweng1.altanto.negocio.excepcion.ReglaDeNegocioException;
import co.edu.unal.sweng1.altanto.persistencia.entidad.Articulo;
import co.edu.unal.sweng1.altanto.persistencia.entidad.DetalleTandaInsumo;
import co.edu.unal.sweng1.altanto.persistencia.entidad.TandaProduccion;
import co.edu.unal.sweng1.altanto.persistencia.repositorio.DetalleTandaInsumoRepository;
import co.edu.unal.sweng1.altanto.persistencia.repositorio.InsumoRepository;
import co.edu.unal.sweng1.altanto.persistencia.repositorio.TandaProduccionRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Reglas del CU-03: Registrar una tanda de producción.
 * <ul>
 *   <li>CU-03-HU-01: {@link #registrarTanda} guarda la tanda con la harina pesada, las
 *       unidades obtenidas y los insumos gastados.</li>
 *   <li>CU-03-HU-02: antes de registrar, verifica que haya existencia suficiente de cada
 *       insumo; si la hay, los descuenta de las existencias. Si no, lanza
 *       {@link InsumoInsuficienteException} y no se registra nada.</li>
 * </ul>
 *
 * <p>Todo pasa en UNA sola acción (no hay borrador → confirmar como en el CU-01): el mismo
 * método que registra la tanda descuenta los insumos. {@code @Service}: Spring crea UN
 * objeto de esta clase al arrancar y se lo pasa al controlador que lo pida.</p>
 */
@Service
public class TandaProduccionService {

    private final TandaProduccionRepository tandaRepository;
    private final DetalleTandaInsumoRepository detalleRepository;
    private final InsumoRepository articuloRepository;

    /** Spring ve qué pide el constructor y le pasa los repositorios (inyección de dependencias). */
    public TandaProduccionService(TandaProduccionRepository tandaRepository,
                                  DetalleTandaInsumoRepository detalleRepository,
                                  InsumoRepository articuloRepository) {
        this.tandaRepository = tandaRepository;
        this.detalleRepository = detalleRepository;
        this.articuloRepository = articuloRepository;
    }

    // ---------------------------------------------------------------- Consultas

    /** Productos horneados (pan) activos: las opciones del combo "¿qué se horneó?". */
    @Transactional(readOnly = true)
    public List<Articulo> productosElaborados() {
        return articuloRepository.findByActivoTrueAndTipoArticuloInOrderByTipoArticuloAscNombreAsc(
                List.of(Articulo.TIPO_PRODUCTO_ELABORADO));
    }

    /** Insumos activos: las opciones de las líneas "insumos gastados" (incluye la harina). */
    @Transactional(readOnly = true)
    public List<Articulo> insumosDisponibles() {
        return articuloRepository.findByActivoTrueAndTipoArticuloInOrderByTipoArticuloAscNombreAsc(
                List.of(Articulo.TIPO_INSUMO));
    }

    /** La tanda con ese id, o error si no existe. */
    @Transactional(readOnly = true)
    public TandaProduccion buscar(Long id) {
        return tandaRepository.findById(id)
                .orElseThrow(() -> new ReglaDeNegocioException("No existe la tanda con id " + id + "."));
    }

    /** Las líneas de insumo (una por insumo gastado) de una tanda. */
    @Transactional(readOnly = true)
    public List<DetalleTandaInsumo> detalles(Long tandaId) {
        return detalleRepository.findByTandaId(tandaId);
    }

    // ---------------------------------------------------------------- CU-03-HU-01 y HU-02

    /**
     * Registra una tanda y descuenta de las existencias los insumos gastados, todo en una
     * sola transacción.
     *
     * <p>El orden importa: primero se verifica que TODOS los insumos alcancen y recién
     * entonces se guarda y se descuenta. Así, si alguno no alcanza, no queda nada a medias.
     * Además {@code @Transactional} hace rollback si algo lanza una excepción
     * (criterios 3 de HU-01 y 4 de HU-02: "si falla, no se registra ni se descuenta nada").</p>
     *
     * @throws InsumoInsuficienteException si algún insumo tiene menos existencia que lo gastado.
     * @throws ReglaDeNegocioException     si no existe el producto o un insumo indicado.
     */
    @Transactional
    public TandaProduccion registrarTanda(LocalDate fecha, Long productoId, BigDecimal harinaBaseKg,
                                          Integer unidadesObtenidas, String maestroPanadero,
                                          String observaciones, List<InsumoGastado> insumosGastados) {

        Articulo producto = articuloRepository.findById(productoId)
                .orElseThrow(() -> new ReglaDeNegocioException("No existe el producto " + productoId + "."));

        // 1. Verificar que cada insumo alcance ANTES de guardar o descontar nada (HU-02 criterio 1).
        for (InsumoGastado gastado : insumosGastados) {
            Articulo insumo = buscarInsumo(gastado.insumoId());
            if (insumo.getExistenciaActual().compareTo(gastado.cantidad()) < 0) {
                // Si no alcanza, se indica de cuál se trata y se detiene todo (HU-02 criterio 2).
                throw new InsumoInsuficienteException(
                        insumo.getNombre(), insumo.getExistenciaActual(), gastado.cantidad());
            }
        }

        // 2. Guardar la cabecera de la tanda.
        TandaProduccion tanda = new TandaProduccion();
        tanda.setFecha(fecha);
        tanda.setArticuloProducto(producto);
        tanda.setHarinaBaseKg(harinaBaseKg);
        tanda.setUnidadesObtenidas(unidadesObtenidas);
        tanda.setMaestroPanadero(maestroPanadero);
        tanda.setObservaciones(observaciones);
        tanda = tandaRepository.save(tanda); // save devuelve el objeto con su id

        // 3. Guardar cada línea y descontar el insumo de las existencias (HU-02 criterio 3).
        for (InsumoGastado gastado : insumosGastados) {
            Articulo insumo = buscarInsumo(gastado.insumoId());

            DetalleTandaInsumo detalle = new DetalleTandaInsumo();
            detalle.setTanda(tanda);
            detalle.setInsumo(insumo);
            detalle.setCantidadConsumida(gastado.cantidad());
            detalleRepository.save(detalle);

            // Descontar. No hace falta save(insumo): dentro de la transacción, Hibernate
            // detecta el cambio solo y hace el UPDATE al terminar (dirty checking).
            insumo.setExistenciaActual(insumo.getExistenciaActual().subtract(gastado.cantidad()));
        }
        return tanda;
    }

    /** Busca un insumo por id, o lanza error de negocio si no existe. */
    private Articulo buscarInsumo(Long insumoId) {
        return articuloRepository.findById(insumoId)
                .orElseThrow(() -> new ReglaDeNegocioException("No existe el insumo " + insumoId + "."));
    }
}
