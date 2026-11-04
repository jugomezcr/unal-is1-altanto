package co.edu.unal.sweng1.altanto.persistencia.repositorio;

import co.edu.unal.sweng1.altanto.persistencia.entidad.Articulo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Prueba de una consulta derivada de {@link InsumoRepository} contra los datos de data.sql.
 *
 * <p>{@code @DataJpaTest}: arranca SOLO la parte de base de datos (entidades,
 * repositorios, H2), no los controladores ni la web. Es más rápido que
 * {@code @SpringBootTest} y deshace los cambios al terminar cada prueba.</p>
 */
@DataJpaTest
class InsumoRepositoryTest {

    @Autowired
    private InsumoRepository insumoRepository;

    @Test
    void encuentraLaHarinaConExistenciaBajoElUmbralDe25Kg() {
        // En data.sql la harina tiene 15 kg, así que debe salir con "<= 25".
        List<Articulo> bajoUmbral = insumoRepository
                .findByExistenciaActualLessThanEqual(new BigDecimal("25"));

        // assertThat (AssertJ): verificaciones que se leen como frases.
        assertThat(bajoUmbral).isNotEmpty();
        assertThat(bajoUmbral)
                .anyMatch(a -> "INS-HAR-01".equals(a.getCodigo()));
    }
}
