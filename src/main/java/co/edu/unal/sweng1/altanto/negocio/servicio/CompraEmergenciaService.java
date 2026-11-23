package co.edu.unal.sweng1.altanto.negocio.servicio;

import co.edu.unal.sweng1.altanto.negocio.calculo.CalculadoraSobrecosto;
import co.edu.unal.sweng1.altanto.negocio.dto.Sobrecosto;
import co.edu.unal.sweng1.altanto.negocio.excepcion.CompraEmergenciaNoEncontradaException;
import co.edu.unal.sweng1.altanto.negocio.excepcion.ReglaDeNegocioException;
import co.edu.unal.sweng1.altanto.persistencia.entidad.Articulo;
import co.edu.unal.sweng1.altanto.persistencia.entidad.CompraEmergencia;
import co.edu.unal.sweng1.altanto.persistencia.repositorio.CompraEmergenciaRepository;
import co.edu.unal.sweng1.altanto.persistencia.repositorio.InsumoRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Reglas de CU-04-HU-02: registrar una compra de emergencia y ver su sobrecosto.
 *
 * <p>Dos pasos en la pantalla, dos métodos aquí: {@link #calcularSobrecosto} solo calcula
 * (para mostrar la diferencia, no guarda nada) y {@link #registrar} guarda la compra y suma
 * lo recibido a las existencias. Cancelar es no llamar a {@code registrar}.</p>
 */
@Service
public class CompraEmergenciaService {

    private final CompraEmergenciaRepository compraRepository;
    private final InsumoRepository articuloRepository;
    private final CalculadoraSobrecosto calculadora = new CalculadoraSobrecosto();

    /** Spring pasa los repositorios al crear el servicio (inyección por constructor). */
    public CompraEmergenciaService(CompraEmergenciaRepository compraRepository,
                                   InsumoRepository articuloRepository) {
        this.compraRepository = compraRepository;
        this.articuloRepository = articuloRepository;
    }

    /** Artículos que se pueden comprar de emergencia: insumos y productos de reventa activos. */
    @Transactional(readOnly = true)
    public List<Articulo> articulosComprables() {
        return articuloRepository.findByActivoTrueAndTipoArticuloInOrderByTipoArticuloAscNombreAsc(
                List.of(Articulo.TIPO_INSUMO, Articulo.TIPO_PRODUCTO_REVENTA));
    }

    /** El artículo con ese id, si existe y se puede comprar; si no, error de negocio. */
    @Transactional(readOnly = true)
    public Articulo articuloComprable(Long articuloId) {
        Articulo articulo = articuloRepository.findById(articuloId)
                .orElseThrow(() -> new ReglaDeNegocioException("No existe el artículo " + articuloId + "."));
        boolean tipoValido = Articulo.TIPO_INSUMO.equals(articulo.getTipoArticulo())
                || Articulo.TIPO_PRODUCTO_REVENTA.equals(articulo.getTipoArticulo());
        if (!tipoValido || !Boolean.TRUE.equals(articulo.getActivo())) {
            throw new ReglaDeNegocioException(
                    "El artículo " + articulo.getNombre() + " no se puede comprar de emergencia.");
        }
        return articulo;
    }

    /** Vista previa: valida los números y calcula la diferencia. No guarda nada. */
    public Sobrecosto calcularSobrecosto(BigDecimal cantidad, BigDecimal precioPagado,
                                         BigDecimal precioHabitual) {
        if (cantidad == null || cantidad.signum() <= 0) {
            throw new ReglaDeNegocioException("La cantidad recibida debe ser mayor que cero.");
        }
        if (precioPagado == null || precioPagado.signum() <= 0
                || precioHabitual == null || precioHabitual.signum() <= 0) {
            throw new ReglaDeNegocioException("Los precios deben ser mayores que cero.");
        }
        return calculadora.calcular(cantidad, precioPagado, precioHabitual);
    }

    /**
     * Confirma la compra: la guarda con su sobrecosto y suma lo recibido a las existencias.
     * {@code @Transactional}: o se hace todo o no se hace nada (como un {@code with
     * transaction.atomic()} de Django). El sobrecosto se recalcula aquí, no se confía en lo
     * que mostró la pantalla.
     */
    @Transactional
    public CompraEmergencia registrar(Long articuloId, BigDecimal cantidad,
                                      BigDecimal precioPagado, BigDecimal precioHabitual) {
        Articulo articulo = articuloComprable(articuloId);
        Sobrecosto sobrecosto = calcularSobrecosto(cantidad, precioPagado, precioHabitual);

        CompraEmergencia compra = compraRepository.save(new CompraEmergencia(
                LocalDate.now(), articulo, cantidad, precioPagado, precioHabitual,
                sobrecosto.pesos(), sobrecosto.porcentaje()));

        // No hace falta save(articulo): dentro de la transacción Hibernate detecta el
        // cambio y hace el UPDATE solo (dirty checking).
        articulo.setExistenciaActual(articulo.getExistenciaActual().add(cantidad));
        return compra;
    }

    /** La compra con ese id, o {@link CompraEmergenciaNoEncontradaException}. */
    @Transactional(readOnly = true)
    public CompraEmergencia buscar(Long id) {
        return compraRepository.findById(id)
                .orElseThrow(() -> new CompraEmergenciaNoEncontradaException(id));
    }
}
