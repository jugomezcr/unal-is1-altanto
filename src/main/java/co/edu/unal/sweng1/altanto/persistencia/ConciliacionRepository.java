package co.edu.unal.sweng1.altanto.persistencia;

import co.edu.unal.sweng1.altanto.negocio.ConciliacionDiaria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConciliacionRepository extends JpaRepository<ConciliacionDiaria, Long> {
    
}