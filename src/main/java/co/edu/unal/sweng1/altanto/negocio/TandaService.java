package co.edu.unal.sweng1.altanto.negocio;

import co.edu.unal.sweng1.altanto.persistencia.DetalleTandaInsumoRepository;
import co.edu.unal.sweng1.altanto.persistencia.InsumoRepository;
import co.edu.unal.sweng1.altanto.persistencia.TandaProduccionRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Business logic for CU-03-HU-01: register a production batch.
 *
 * <p>{@link #registrarTanda} runs inside ONE transaction and validates everything
 * BEFORE the first write: the product must be {@code PRODUCTO_ELABORADO} and every
 * input must cover the requested amount. Only then it persists the batch, its input
 * details and the stock discount. Any {@link ReglaDeNegocioException} rolls the whole
 * transaction back, so a failed or cancelled batch leaves no batch row, no detail and
 * no stock change (minimum guarantee of the use case).</p>
 *
 * <p>Architecture, same as {@link ConciliacionService}: the controller
 * ({@code presentacion}) calls this service ({@code negocio}) and this one uses the
 * repositories ({@code persistencia}). The controller never touches the repositories.</p>
 */
@Service
public class TandaService {

    private final TandaProduccionRepository tandaRepository;
    private final DetalleTandaInsumoRepository detalleRepository;
    private final InsumoRepository articuloRepository;

    /** Constructor injection: Spring passes the three repositories, once, at startup. */
    public TandaService(TandaProduccionRepository tandaRepository,
                        DetalleTandaInsumoRepository detalleRepository,
                        InsumoRepository articuloRepository) {
        this.tandaRepository = tandaRepository;
        this.detalleRepository = detalleRepository;
        this.articuloRepository = articuloRepository;
    }

    // =========================================================================
    // Queries (read only)
    // =========================================================================

    /** Active {@code PRODUCTO_ELABORADO} articles for the product select of the form. */
    @Transactional(readOnly = true)
    public List<Articulo> productosElaborados() {
        return articuloRepository.findByActivoTrueAndTipoArticuloInOrderByTipoArticuloAscNombreAsc(
                List.of(Articulo.TIPO_PRODUCTO_ELABORADO));
    }

    /** Active {@code INSUMO} articles for the input rows of the form (stock included). */
    @Transactional(readOnly = true)
    public List<Articulo> insumos() {
        return articuloRepository.findByActivoTrueAndTipoArticuloInOrderByTipoArticuloAscNombreAsc(
                List.of(Articulo.TIPO_INSUMO));
    }

    // =========================================================================
    // CU-03-HU-01: register the batch
    // =========================================================================

    /**
     * Saves one batch with its input details and discounts every input stock in the
     * same transaction.
     *
     * <p><b>Order matters (steps 4/4a of the CU):</b> header, product type and the
     * stock of EVERY input are validated before the first insert, so a shortage throws
     * {@link ExistenciaInsuficienteException} with zero writes already on disk.</p>
     *
     * <p>Empty form rows ({@code insumoId == null} or {@code cantidad <= 0}) are
     * ignored; the two lists are position-aligned as sent by the form.</p>
     *
     * @param fecha             production day of the batch.
     * @param productoId        article that was baked; must be {@code PRODUCTO_ELABORADO}.
     * @param harinaBaseKg      base flour used, in kg (&gt; 0).
     * @param unidadesObtenidas units obtained (&gt; 0).
     * @param maestroPanadero   baker in charge (required).
     * @param observaciones     optional free-text note.
     * @param insumoIds         input ids, one per form row.
     * @param cantidades         amounts consumed, aligned with {@code insumoIds}.
     * @return the saved batch (already with its id).
     * @throws ExistenciaInsuficienteException if some input lacks stock (nothing written).
     * @throws ReglaDeNegocioException         if any other business rule fails.
     */
    @Transactional
    public TandaProduccion registrarTanda(LocalDate fecha, Long productoId, BigDecimal harinaBaseKg,
                                          Integer unidadesObtenidas, String maestroPanadero,
                                          String observaciones, List<Long> insumoIds,
                                          List<BigDecimal> cantidades) {
        validarEncabezado(fecha, productoId, harinaBaseKg, unidadesObtenidas, maestroPanadero);

        Articulo producto = articuloRepository.findById(productoId)
                .orElseThrow(() -> new ReglaDeNegocioException("No existe el artículo con id " + productoId + "."));
        if (!Articulo.TIPO_PRODUCTO_ELABORADO.equals(producto.getTipoArticulo())) {
            throw new ReglaDeNegocioException("El artículo " + producto.getNombre() + " no es un producto elaborado.");
        }

        if (insumoIds == null || cantidades == null || insumoIds.size() != cantidades.size()) {
            throw new ReglaDeNegocioException("Las filas de insumos del formulario no son consistentes.");
        }

        // Step 4: check every input BEFORE the first write. If one is short, this
        // loop throws and the transaction has nothing to undo yet.
        Map<Long, Articulo> insumosPorId = new HashMap<>();
        Map<Long, BigDecimal> requeridoPorInsumo = new LinkedHashMap<>();
        for (int i = 0; i < insumoIds.size(); i++) {
            Long insumoId = insumoIds.get(i);
            BigDecimal cantidad = cantidades.get(i);
            if (insumoId == null || cantidad == null || cantidad.signum() <= 0) {
                continue; // empty row of the form: ignored by contract
            }
            Articulo insumo = insumosPorId.computeIfAbsent(insumoId,
                    id -> articuloRepository.findById(id).orElseThrow(
                            () -> new ReglaDeNegocioException("No existe el insumo con id " + id + ".")));
            if (!Articulo.TIPO_INSUMO.equals(insumo.getTipoArticulo())) {
                throw new ReglaDeNegocioException("El artículo " + insumo.getNombre() + " no es un insumo.");
            }
            // Merged per input, so duplicated rows of the same input cannot push the
            // stock below zero either.
            BigDecimal requerido = requeridoPorInsumo.merge(insumoId, cantidad, BigDecimal::add);
            if (requerido.compareTo(insumo.getExistenciaActual()) > 0) {
                throw new ExistenciaInsuficienteException(insumo.getNombre(), insumo.getExistenciaActual(), requerido);
            }
        }

        // All checks passed: batch + details + discount, all in this transaction.
        TandaProduccion tanda = new TandaProduccion();
        tanda.setFecha(fecha);
        tanda.setArticuloProducto(producto);
        tanda.setHarinaBaseKg(harinaBaseKg);
        tanda.setUnidadesObtenidas(unidadesObtenidas);
        tanda.setMaestroPanadero(maestroPanadero);
        tanda.setObservaciones(observaciones);
        TandaProduccion guardada = tandaRepository.save(tanda);

        for (int i = 0; i < insumoIds.size(); i++) {
            Long insumoId = insumoIds.get(i);
            BigDecimal cantidad = cantidades.get(i);
            if (insumoId == null || cantidad == null || cantidad.signum() <= 0) {
                continue;
            }
            Articulo insumo = insumosPorId.get(insumoId);
            DetalleTandaInsumo detalle = new DetalleTandaInsumo();
            detalle.setTanda(guardada);
            detalle.setInsumo(insumo);
            detalle.setCantidadConsumida(cantidad);
            detalleRepository.save(detalle);
            // No save() needed for the input: it is a managed entity, so Hibernate
            // detects the change (dirty checking) and flushes the UPDATE with the
            // same transaction that writes the batch.
            insumo.setExistenciaActual(insumo.getExistenciaActual().subtract(cantidad));
        }
        return guardada;
    }

    /** Header rules: the form validates too, but the service never trusts the caller. */
    private void validarEncabezado(LocalDate fecha, Long productoId, BigDecimal harinaBaseKg,
                                   Integer unidadesObtenidas, String maestroPanadero) {
        if (fecha == null) {
            throw new ReglaDeNegocioException("La fecha de la tanda es obligatoria.");
        }
        if (productoId == null) {
            throw new ReglaDeNegocioException("El producto de la tanda es obligatorio.");
        }
        if (harinaBaseKg == null || harinaBaseKg.signum() <= 0) {
            throw new ReglaDeNegocioException("La harina base debe ser mayor que cero.");
        }
        if (unidadesObtenidas == null || unidadesObtenidas <= 0) {
            throw new ReglaDeNegocioException("Las unidades obtenidas deben ser mayor que cero.");
        }
        if (maestroPanadero == null || maestroPanadero.isBlank()) {
            throw new ReglaDeNegocioException("El maestro panadero es obligatorio.");
        }
    }
}
