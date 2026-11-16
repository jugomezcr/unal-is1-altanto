package co.edu.unal.sweng1.altanto.persistencia.repositorio;

import static org.assertj.core.api.Assertions.assertThat;

import co.edu.unal.sweng1.altanto.persistencia.entidad.DetalleTandaInsumo;
import co.edu.unal.sweng1.altanto.persistencia.entidad.TandaProduccion;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

/**
 * Pruebas de las consultas derivadas de los repositorios de tandas (CU-03), contra los
 * datos de data.sql.
 *
 * <p>{@code @DataJpaTest}: arranca SOLO la parte de base de datos (entidades,
 * repositorios, H2), no los controladores ni la web. Es más rápido que
 * {@code @SpringBootTest} y deshace los cambios al terminar cada prueba. Mismo estilo que
 * {@link InsumoRepositoryTest}.</p>
 *
 * <p>En data.sql hay dos tandas: la 1 (01/11/2026, 2 líneas de insumo) y la 2
 * (02/11/2026, 2 líneas de insumo).</p>
 */
@DataJpaTest
class TandaProduccionRepositoryTest {

    @Autowired
    private TandaProduccionRepository tandaRepository;
    @Autowired
    private DetalleTandaInsumoRepository detalleRepository;

    @Test
    void listaLasTandasDeLaMasRecienteALaMasAntigua() {
        List<TandaProduccion> tandas = tandaRepository.findAllByOrderByFechaDesc();

        // Hay al menos las dos tandas de data.sql.
        assertThat(tandas).hasSizeGreaterThanOrEqualTo(2);
        // La primera de la lista es la más reciente (02/11 va antes que 01/11).
        assertThat(tandas.get(0).getFecha()).isAfterOrEqualTo(tandas.get(1).getFecha());
    }

    @Test
    void encuentraLasLineasDeInsumoDeUnaTanda() {
        // La tanda 1 de data.sql gastó harina (id 1, 25 kg) y levadura (id 2, 350 g).
        List<DetalleTandaInsumo> lineas = detalleRepository.findByTandaId(1L);

        assertThat(lineas).hasSize(2);
        assertThat(lineas)
                .anyMatch(d -> "INS-HAR-01".equals(d.getInsumo().getCodigo()))
                .anyMatch(d -> "INS-LEV-01".equals(d.getInsumo().getCodigo()));
    }
}
