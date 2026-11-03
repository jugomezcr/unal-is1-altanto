package co.edu.unal.sweng1.altanto.persistencia;

import co.edu.unal.sweng1.altanto.negocio.TandaProduccion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TandaProduccionRepository extends JpaRepository<TandaProduccion, Long> {
    
}