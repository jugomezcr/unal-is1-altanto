package co.edu.unal.sweng1.altanto.persistencia.repositorio;

import co.edu.unal.sweng1.altanto.persistencia.entidad.CompraEmergencia;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Acceso a la tabla {@code compras_emergencia} (CU-04-HU-02).
 * Sin métodos propios: {@code save} y {@code findById} heredados bastan para esta historia.
 */
public interface CompraEmergenciaRepository extends JpaRepository<CompraEmergencia, Long> {
}
