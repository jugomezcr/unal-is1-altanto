package co.edu.unal.sweng1.altanto.persistencia;

import co.edu.unal.sweng1.altanto.negocio.Articulo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.math.BigDecimal;
import java.util.List;

public interface InsumoRepository extends JpaRepository<Articulo, Long> {

    List<Articulo> findByExistenciaActualLessThanEqual(BigDecimal umbral);
}