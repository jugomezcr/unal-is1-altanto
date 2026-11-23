package co.edu.unal.sweng1.altanto.negocio.servicio;

import static org.assertj.core.api.Assertions.assertThat;

import co.edu.unal.sweng1.altanto.persistencia.entidad.CompraEmergencia;
import co.edu.unal.sweng1.altanto.persistencia.repositorio.InsumoRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/**
 * Prueba de INTEGRACIÓN de CU-04-HU-02: servicio real con H2 cargada con schema.sql y
 * data.sql. {@code @Transactional} deshace lo guardado al terminar cada prueba.
 * En data.sql la harina (id 1) tiene 15 kg.
 */
@SpringBootTest
@Transactional
class CompraEmergenciaServiceTest {

    private static final Long HARINA = 1L;

    @Autowired
    private CompraEmergenciaService service;
    @Autowired
    private InsumoRepository articuloRepository;

    @Test
    void registrarSumaLasExistenciasYGuardaElSobrecosto() {
        // 10 kg a 4000 (habitual 3200). La harina pasa de 15 a 25 kg.
        CompraEmergencia compra = service.registrar(HARINA, new BigDecimal("10"),
                new BigDecimal("4000"), new BigDecimal("3200"));

        assertThat(articuloRepository.findById(HARINA).orElseThrow().getExistenciaActual())
                .isEqualByComparingTo("25");
        assertThat(compra.getId()).isNotNull();
        assertThat(compra.getSobrecostoPesos()).isEqualByComparingTo("8000");
        assertThat(compra.getSobrecostoPorcentaje()).isEqualByComparingTo("25");
    }

}
