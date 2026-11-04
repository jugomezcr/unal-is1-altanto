package co.edu.unal.sweng1.altanto.negocio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.unal.sweng1.altanto.persistencia.DetalleTandaInsumoRepository;
import co.edu.unal.sweng1.altanto.persistencia.InsumoRepository;
import co.edu.unal.sweng1.altanto.persistencia.TandaProduccionRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/**
 * Pruebas de INTEGRACIÓN de {@link ConciliacionService} (CU-01-HU-01 y CU-01-HU-02).
 *
 * <ul>
 *   <li>{@code @SpringBootTest}: arranca la aplicación completa con la base H2 en
 *       memoria de {@code src/test/resources/application.properties}, cargada con
 *       {@code schema.sql} y {@code data.sql}.</li>
 *   <li>{@code @Transactional} en una clase de prueba: al terminar CADA prueba, Spring
 *       deshace (rollback) todo lo que esta guardó. Así las pruebas no se afectan entre sí.</li>
 *   <li>{@code @Autowired}: Spring inyecta el objeto pedido en el campo.</li>
 * </ul>
 *
 * <p>Datos semilla usados (ver data.sql): la conciliación del 01/11/2026 está
 * CONFIRMADA (harina contada = 15, kumis = 18) y la del 02/11/2026 es un BORRADOR.</p>
 */
@SpringBootTest
@Transactional
class ConciliacionServiceTest {

    private static final LocalDate DIA = LocalDate.of(2026, 11, 3);
    private static final long HARINA = 1L;
    private static final long LEVADURA = 2L;
    private static final long KUMIS = 12L;

    @Autowired
    private ConciliacionService service;
    @Autowired
    private InsumoRepository articuloRepository;
    @Autowired
    private TandaProduccionRepository tandaRepository;
    @Autowired
    private DetalleTandaInsumoRepository detalleTandaRepository;

    /**
     * Arma los conteos de todos los artículos conciliables. Por defecto "cuenta" su
     * existencia actual y no vende nada; {@code cambios} reemplaza el contado de algunos.
     */
    private List<ConteoArticulo> conteos(Map<Long, String> cambios, Map<Long, String> vendidos) {
        return service.articulosAConciliar().stream()
                .map(a -> new ConteoArticulo(
                        a.getId(),
                        vendidos.containsKey(a.getId()) ? new BigDecimal(vendidos.get(a.getId())) : null,
                        cambios.containsKey(a.getId()) ? new BigDecimal(cambios.get(a.getId())) : a.getExistenciaActual()))
                .toList();
    }

    private DetalleConciliacion detalleDe(ConciliacionDiaria c, long articuloId) {
        return service.detalles(c.getId()).stream()
                .filter(d -> d.getArticulo().getId() == articuloId)
                .findFirst().orElseThrow();
    }

    // ---------------------------------------------------------------- HU-01

    @Test
    void soloSeConcilianInsumosYProductosDeReventa() {
        assertThat(service.articulosAConciliar())
                .extracting(Articulo::getTipoArticulo)
                .containsOnly(Articulo.TIPO_INSUMO, Articulo.TIPO_PRODUCTO_REVENTA);
    }

    @Test
    void registraElBorradorCalculandoEsperadoYDiferencia() {
        ConciliacionDiaria c = service.registrarBorrador(DIA, new BigDecimal("250000"),
                conteos(Map.of(HARINA, "14", KUMIS, "15"), Map.of(KUMIS, "3")));

        assertThat(c.getId()).isNotNull();
        assertThat(c.getEstado()).isEqualTo(EstadoConciliacion.BORRADOR);

        // Harina: anterior = contado del 01/11 (15); sin tandas el 03/11 → esperado 15; contado 14 → −1
        DetalleConciliacion harina = detalleDe(c, HARINA);
        assertThat(harina.getExistenciaAnterior()).isEqualByComparingTo("15");
        assertThat(harina.getExistenciaEsperada()).isEqualByComparingTo("15");
        assertThat(harina.getDiferencia()).isEqualByComparingTo("-1");

        // Kumis: anterior 18, vendidos 3 → esperado 15; contado 15 → cuadra
        DetalleConciliacion kumis = detalleDe(c, KUMIS);
        assertThat(kumis.getCantidadSalida()).isEqualByComparingTo("3");
        assertThat(kumis.tieneDiferencia()).isFalse();

        // Levadura nunca se ha conciliado → parte de su existencia actual (1500 g)
        assertThat(detalleDe(c, LEVADURA).getExistenciaAnterior()).isEqualByComparingTo("1500");

        // Guardar el borrador NO cambia las existencias (eso pasa al confirmar)
        assertThat(articuloRepository.findById(HARINA).orElseThrow().getExistenciaActual()).isEqualByComparingTo("15");
    }

    @Test
    void restaLoConsumidoEnLasTandasDelDia() {
        Articulo harina = articuloRepository.findById(HARINA).orElseThrow();
        TandaProduccion tanda = new TandaProduccion();
        tanda.setFecha(DIA);
        tanda.setArticuloProducto(articuloRepository.findById(10L).orElseThrow());
        tanda.setHarinaBaseKg(new BigDecimal("5"));
        tanda.setUnidadesObtenidas(30);
        tanda.setMaestroPanadero("Leonardo Betancur");
        tandaRepository.save(tanda);
        DetalleTandaInsumo consumo = new DetalleTandaInsumo();
        consumo.setTanda(tanda);
        consumo.setInsumo(harina);
        consumo.setCantidadConsumida(new BigDecimal("5"));
        detalleTandaRepository.save(consumo);

        ConciliacionDiaria c = service.registrarBorrador(DIA, BigDecimal.ZERO, conteos(Map.of(HARINA, "10"), Map.of()));

        // 15 − 5 consumidos = 10 esperados; contado 10 → cuadra
        DetalleConciliacion linea = detalleDe(c, HARINA);
        assertThat(linea.getCantidadSalida()).isEqualByComparingTo("5");
        assertThat(linea.getExistenciaEsperada()).isEqualByComparingTo("10");
        assertThat(linea.tieneDiferencia()).isFalse();
    }

    @Test
    void noPermiteDosConciliacionesDeLaMismaFechaYOfreceLaExistente() {
        // El 02/11/2026 ya tiene un borrador (id 2) en los datos semilla. Extensión 1a.
        assertThatThrownBy(() -> service.registrarBorrador(LocalDate.of(2026, 11, 2), BigDecimal.ZERO, conteos(Map.of(), Map.of())))
                .isInstanceOf(ConciliacionDuplicadaException.class)
                .extracting(e -> ((ConciliacionDuplicadaException) e).getIdExistente())
                .isEqualTo(2L);
    }

    @Test
    void noPermiteConciliarDiasYaCerradosNiFechasFuturas() {
        List<ConteoArticulo> conteos = conteos(Map.of(), Map.of());
        assertThatThrownBy(() -> service.registrarBorrador(LocalDate.of(2026, 10, 31), BigDecimal.ZERO, conteos))
                .isInstanceOf(ReglaDeNegocioException.class)
                .hasMessageContaining("confirmada");
        assertThatThrownBy(() -> service.registrarBorrador(LocalDate.now().plusDays(1), BigDecimal.ZERO, conteos))
                .isInstanceOf(ReglaDeNegocioException.class)
                .hasMessageContaining("futura");
    }

    // ---------------------------------------------------------------- HU-02

    @Test
    void alConfirmarLoContadoQuedaComoExistenciaYSeMuestraLaAlerta() {
        ConciliacionDiaria c = service.registrarBorrador(DIA, new BigDecimal("100000"), conteos(Map.of(HARINA, "14"), Map.of()));

        List<InsumoEnAlerta> alertas = service.confirmar(c.getId());

        assertThat(service.buscar(c.getId()).getEstado()).isEqualTo(EstadoConciliacion.CONFIRMADA);
        assertThat(articuloRepository.findById(HARINA).orElseThrow().getExistenciaActual()).isEqualByComparingTo("14");
        // Harina: 14 kg ≤ umbral de 25 kg → alerta de reabastecimiento (criterio 4)
        assertThat(alertas).extracting(InsumoEnAlerta::nombre).contains("Harina de Trigo Especial");
        // Confirmar dos veces no se permite
        assertThatThrownBy(() -> service.confirmar(c.getId())).isInstanceOf(ReglaDeNegocioException.class);
    }

    @Test
    void descartarElBorradorNoDejaNadaGuardado() {
        ConciliacionDiaria c = service.registrarBorrador(DIA, BigDecimal.ZERO, conteos(Map.of(), Map.of()));

        service.descartar(c.getId());

        assertThat(service.buscarPorFecha(DIA)).isEmpty();
        // Una conciliación confirmada (01/11, id 1) no se puede descartar
        assertThatThrownBy(() -> service.descartar(1L)).isInstanceOf(ReglaDeNegocioException.class);
    }
}
