package co.edu.unal.sweng1.altanto.negocio.servicio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.unal.sweng1.altanto.negocio.dto.InsumoGastado;
import co.edu.unal.sweng1.altanto.negocio.excepcion.InsumoInsuficienteException;
import co.edu.unal.sweng1.altanto.persistencia.entidad.TandaProduccion;
import co.edu.unal.sweng1.altanto.persistencia.repositorio.InsumoRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/**
 * Prueba de INTEGRACIÓN del CU-03: usa el servicio real con la base H2 en memoria,
 * cargada con schema.sql y data.sql. Mismo estilo que {@link ConciliacionServiceTest}.
 *
 * <p>En data.sql: la harina (id 1) tiene 15 kg, la levadura (id 2) tiene 1500 g y el
 * Pan Francés (id 10) es un producto elaborado.</p>
 */
@SpringBootTest  // arranca la aplicación completa
@Transactional   // al terminar cada prueba se deshace lo guardado: las pruebas no se afectan
class TandaProduccionServiceTest {

    private static final LocalDate DIA = LocalDate.of(2026, 11, 3);
    private static final Long PAN_FRANCES = 10L;
    private static final Long HARINA = 1L;
    private static final Long LEVADURA = 2L;

    @Autowired
    private TandaProduccionService service;
    @Autowired
    private InsumoRepository articuloRepository;

    @Test
    void registrarTandaDescuentaLosInsumos() {
        // Gastar 10 kg de harina (había 15) y 500 g de levadura (había 1500).
        List<InsumoGastado> gastados = List.of(
                new InsumoGastado(HARINA, new BigDecimal("10")),
                new InsumoGastado(LEVADURA, new BigDecimal("500")));

        service.registrarTanda(DIA, PAN_FRANCES, new BigDecimal("10"), 120,
                "Leonardo Betancur", "Tanda de prueba", gastados);

        assertThat(articuloRepository.findById(HARINA).orElseThrow().getExistenciaActual())
                .isEqualByComparingTo("5");    // 15 - 10
        assertThat(articuloRepository.findById(LEVADURA).orElseThrow().getExistenciaActual())
                .isEqualByComparingTo("1000"); // 1500 - 500
    }

    @Test
    void registrarTandaGuardaCabeceraYLineas() {
        List<InsumoGastado> gastados = List.of(
                new InsumoGastado(HARINA, new BigDecimal("8")),
                new InsumoGastado(LEVADURA, new BigDecimal("200")));

        TandaProduccion tanda = service.registrarTanda(DIA, PAN_FRANCES, new BigDecimal("8"), 90,
                "Leonardo Betancur", null, gastados);

        assertThat(tanda.getId()).isNotNull();
        assertThat(tanda.getUnidadesObtenidas()).isEqualTo(90);
        assertThat(tanda.getHarinaBaseKg()).isEqualByComparingTo("8");
        assertThat(service.detalles(tanda.getId())).hasSize(2);
    }

    @Test
    void siUnInsumoNoAlcanzaNoRegistraNiDescuenta() {
        // La harina tiene 15 kg; se piden 20. No alcanza.
        List<InsumoGastado> gastados = List.of(
                new InsumoGastado(HARINA, new BigDecimal("20")),
                new InsumoGastado(LEVADURA, new BigDecimal("100")));

        assertThatThrownBy(() -> service.registrarTanda(DIA, PAN_FRANCES, new BigDecimal("20"), 100,
                "Leonardo Betancur", null, gastados))
                .isInstanceOf(InsumoInsuficienteException.class)
                .hasMessageContaining("Harina de Trigo Especial");

        // No se descontó NADA: ni la harina (que falló) ni la levadura (que alcanzaba).
        assertThat(articuloRepository.findById(HARINA).orElseThrow().getExistenciaActual())
                .isEqualByComparingTo("15");
        assertThat(articuloRepository.findById(LEVADURA).orElseThrow().getExistenciaActual())
                .isEqualByComparingTo("1500");
    }
}
