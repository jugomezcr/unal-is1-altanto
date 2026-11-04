package co.edu.unal.sweng1.altanto.persistencia.repositorio;

import co.edu.unal.sweng1.altanto.persistencia.entidad.TandaProduccion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TandaProduccionRepository extends JpaRepository<TandaProduccion, Long> {
    
}