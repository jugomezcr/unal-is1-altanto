package co.edu.unal.sweng1.altanto.negocio.servicio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.unal.sweng1.altanto.negocio.dto.ConteoArticulo;
import co.edu.unal.sweng1.altanto.negocio.excepcion.ConciliacionDuplicadaException;
import co.edu.unal.sweng1.altanto.persistencia.entidad.ConciliacionDiaria;
import co.edu.unal.sweng1.altanto.persistencia.entidad.DetalleConciliacion;
import co.edu.unal.sweng1.altanto.persistencia.repositorio.InsumoRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/**
 * Prueba de INTEGRACIÓN del CU-01: usa el servicio real con la base H2 en memoria,
 * cargada con schema.sql y data.sql. En data.sql la harina (id 1) tiene 15 kg y ya
 * existe una conciliación del 2026-11-01.
 */
@SpringBootTest  // arranca la aplicación completa
@Transactional   // al terminar cada prueba se deshace lo guardado: las pruebas no se afectan
class ConciliacionServiceTest {

    private static final LocalDate DIA = LocalDate.of(2026, 11, 3);
    private static final Long HARINA = 1L;

    @Autowired // Spring inyecta los objetos en los campos
    private ConciliacionService service;
    @Autowired
    private InsumoRepository articuloRepository;

    /** Cuenta todos los artículos tal como están, salvo la harina: se cuentan 14 kg. */
    private List<ConteoArticulo> conteosConHarinaEn14() {
        return service.articulosAConciliar().stream()
                .map(a -> new ConteoArticulo(a.getId(), null,
                        a.getId().equals(HARINA) ? new BigDecimal("14") : a.getExistenciaActual()))
                .toList();
    }

    @Test
    void registrarBorradorCalculaLaDiferencia() {
        ConciliacionDiaria c = service.registrarBorrador(DIA, new BigDecimal("250000"), conteosConHarinaEn14());

        DetalleConciliacion harina = service.detalles(c.getId()).stream()
                .filter(d -> d.getArticulo().getId().equals(HARINA))
                .findFirst().orElseThrow();
        // había 15, no se vendió nada, se contaron 14 → falta 1
        assertThat(harina.getDiferencia()).isEqualByComparingTo("-1");
        assertThat(c.esBorrador()).isTrue();
    }

    @Test
    void noPermiteDosConciliacionesDelMismoDia() {
        LocalDate yaConciliado = LocalDate.of(2026, 11, 1); // existe en data.sql

        assertThatThrownBy(() -> service.registrarBorrador(yaConciliado, BigDecimal.ZERO, conteosConHarinaEn14()))
                .isInstanceOf(ConciliacionDuplicadaException.class);
    }

    @Test
    void confirmarDejaLoContadoComoExistencia() {
        ConciliacionDiaria c = service.registrarBorrador(DIA, new BigDecimal("250000"), conteosConHarinaEn14());

        service.confirmar(c.getId());

        assertThat(articuloRepository.findById(HARINA).orElseThrow().getExistenciaActual())
                .isEqualByComparingTo("14");
    }
}
