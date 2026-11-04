package co.edu.unal.sweng1.altanto.persistencia.repositorio;

import co.edu.unal.sweng1.altanto.persistencia.entidad.Articulo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class InsumoRepositoryTest {

    @Autowired
    private InsumoRepository insumoRepository;

    @Test
    void encuentraLaHarinaConExistenciaBajoElUmbralDe25Kg() {
        List<Articulo> bajoUmbral = insumoRepository
                .findByExistenciaActualLessThanEqual(new BigDecimal("25"));

        assertThat(bajoUmbral).isNotEmpty();
        assertThat(bajoUmbral)
                .anyMatch(a -> "INS-HAR-01".equals(a.getCodigo()));
    }
}