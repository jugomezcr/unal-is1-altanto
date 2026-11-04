package co.edu.unal.sweng1.altanto.negocio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Garantía mínima del CU-01 (CU-01-HU-01, criterio 3): "si la operación falla, no se
 * guarda nada".
 *
 * <p>Esta clase NO lleva {@code @Transactional}, a diferencia de
 * {@link ConciliacionServiceTest}. Si lo llevara, todo correría dentro de la
 * transacción de la prueba y no se podría ver si el servicio deshizo lo suyo. Aquí el
 * servicio abre y cierra su propia transacción, como en la aplicación real.</p>
 */
@SpringBootTest
class ConciliacionRollbackTest {

    @Autowired
    private ConciliacionService service;

    @Test
    void siUnaLineaFallaNoQuedaGuardadaNiLaCabecera() {
        LocalDate dia = LocalDate.of(2026, 11, 3);
        List<ConteoArticulo> conteos = new ArrayList<>();
        for (Articulo a : service.articulosAConciliar()) {
            conteos.add(new ConteoArticulo(a.getId(), null, a.getExistenciaActual()));
        }
        // El ÚLTIMO artículo trae un contado negativo: la cabecera y las primeras líneas
        // alcanzan a guardarse antes del error y deben deshacerse.
        Long ultimo = conteos.get(conteos.size() - 1).articuloId();
        conteos.set(conteos.size() - 1, new ConteoArticulo(ultimo, null, new BigDecimal("-1")));

        assertThatThrownBy(() -> service.registrarBorrador(dia, BigDecimal.TEN, conteos))
                .isInstanceOf(ReglaDeNegocioException.class);

        assertThat(service.buscarPorFecha(dia)).isEmpty();
    }
}
