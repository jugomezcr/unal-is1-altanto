package co.edu.unal.sweng1.altanto.negocio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.unal.sweng1.altanto.persistencia.DetalleTandaInsumoRepository;
import co.edu.unal.sweng1.altanto.persistencia.InsumoRepository;
import co.edu.unal.sweng1.altanto.persistencia.TandaProduccionRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for {@link TandaService} (CU-03-HU-01).
 *
 * <ul>
 *   <li>{@code @SpringBootTest}: boots the whole application against the in-memory H2
 *       of {@code src/test/resources/application.properties}, seeded by
 *       {@code schema.sql} and {@code data.sql}.</li>
 *   <li>{@code @Transactional}: Spring rolls back after EVERY test, so the writes of the
 *       happy path never leak into the other test classes (seed stock: harina = 15 kg,
 *       levadura = 1500 g, batch ids 1-2, detail ids 1-4).</li>
 * </ul>
 *
 * <p>Minimum guarantee under test: a batch that fails or is rejected leaves NO batch
 * row, NO detail and NO stock discount.</p>
 */
@SpringBootTest
@Transactional
class TandaServiceTest {

    private static final LocalDate DIA = LocalDate.of(2026, 11, 3);
    private static final long HARINA = 1L;
    private static final long LEVADURA = 2L;
    private static final long PAN_FRANCES = 10L;
    private static final long KUMIS = 12L;

    @Autowired
    private TandaService service;
    @Autowired
    private TandaProduccionRepository tandaRepository;
    @Autowired
    private DetalleTandaInsumoRepository detalleRepository;
    @Autowired
    private InsumoRepository articuloRepository;

    private BigDecimal existenciaDe(long articuloId) {
        return articuloRepository.findById(articuloId).orElseThrow().getExistenciaActual();
    }

    private DetalleTandaInsumo detalleDe(TandaProduccion tanda, long insumoId) {
        return detalleRepository.findAll().stream()
                .filter(d -> tanda.getId().equals(d.getTanda().getId())
                        && insumoId == d.getInsumo().getId())
                .findFirst().orElseThrow();
    }

    // ------------------------------------------------------------- happy path

    @Test
    void registraLaTandaConSusDetallesYDescuentaLasExistencias() {
        long tandasAntes = tandaRepository.count();
        long detallesAntes = detalleRepository.count();

        TandaProduccion tanda = service.registrarTanda(DIA, PAN_FRANCES, new BigDecimal("5"),
                30, "Leonardo Betancur", "Prueba CU-03",
                List.of(HARINA, LEVADURA), List.of(new BigDecimal("5"), new BigDecimal("350")));

        assertThat(tanda.getId()).isNotNull();
        assertThat(tanda.getArticuloProducto().getId()).isEqualTo(PAN_FRANCES);
        assertThat(tandaRepository.count()).isEqualTo(tandasAntes + 1);
        assertThat(detalleRepository.count()).isEqualTo(detallesAntes + 2);

        assertThat(detalleDe(tanda, HARINA).getCantidadConsumida()).isEqualByComparingTo("5");
        assertThat(detalleDe(tanda, LEVADURA).getCantidadConsumida()).isEqualByComparingTo("350");

        // Stock discounted exactly once, with the requested amounts.
        assertThat(existenciaDe(HARINA)).isEqualByComparingTo("10");    // 15 − 5
        assertThat(existenciaDe(LEVADURA)).isEqualByComparingTo("1150"); // 1500 − 350
    }

    // --------------------------------------------------- minimum guarantee

    @Test
    void existenciaInsuficienteLanzaLaExcepcionCorrectaSinEscribirNada() {
        long tandasAntes = tandaRepository.count();
        long detallesAntes = detalleRepository.count();
        BigDecimal harinaAntes = existenciaDe(HARINA);

        assertThatThrownBy(() -> service.registrarTanda(DIA, PAN_FRANCES, new BigDecimal("5"),
                30, "Leonardo Betancur", null,
                List.of(HARINA), List.of(new BigDecimal("16"))))
                .isInstanceOf(ExistenciaInsuficienteException.class)
                .hasMessageContaining("Insumo Harina de Trigo Especial insuficiente")
                .extracting(e -> ((ExistenciaInsuficienteException) e).getNombreInsumo())
                .isEqualTo("Harina de Trigo Especial");

        // Zero writes: no batch, no detail, stock untouched.
        assertThat(tandaRepository.count()).isEqualTo(tandasAntes);
        assertThat(detalleRepository.count()).isEqualTo(detallesAntes);
        assertThat(existenciaDe(HARINA)).isEqualByComparingTo(harinaAntes);
    }

    @Test
    void soloSePuedenRegistrarTandasDeProductosElaborados() {
        assertThatThrownBy(() -> service.registrarTanda(DIA, KUMIS, new BigDecimal("5"),
                30, "Leonardo Betancur", null,
                List.of(HARINA), List.of(BigDecimal.ONE)))
                .isInstanceOf(ReglaDeNegocioException.class)
                .hasMessageContaining("no es un producto elaborado");
    }

    @Test
    void siElSegundoInsumoFallaElPrimeroTambienQuedaIntacto() {
        long tandasAntes = tandaRepository.count();
        long detallesAntes = detalleRepository.count();
        BigDecimal harinaAntes = existenciaDe(HARINA);
        BigDecimal levaduraAntes = existenciaDe(LEVADURA);

        // Harina is fine (5 of 15 kg); levadura asks far more than its 1500 g.
        assertThatThrownBy(() -> service.registrarTanda(DIA, PAN_FRANCES, new BigDecimal("5"),
                30, "Leonardo Betancur", null,
                List.of(HARINA, LEVADURA), List.of(new BigDecimal("5"), new BigDecimal("999999"))))
                .isInstanceOf(ExistenciaInsuficienteException.class)
                .hasMessageContaining("Levadura Fresca en Bloque");

        // Total rollback: neither the first input nor anything else changed.
        assertThat(tandaRepository.count()).isEqualTo(tandasAntes);
        assertThat(detalleRepository.count()).isEqualTo(detallesAntes);
        assertThat(existenciaDe(HARINA)).isEqualByComparingTo(harinaAntes);
        assertThat(existenciaDe(LEVADURA)).isEqualByComparingTo(levaduraAntes);
    }
}
