package co.edu.unal.sweng1.altanto.negocio;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Una fila del historial de conciliaciones ("consultar cierres previos", compromiso de la
 * Entrega 4 para CU-01-HU-02).
 *
 * <p>Es un {@code record} (ver {@link ConteoArticulo}): solo transporta datos. Lo llena
 * directamente la consulta {@code ConciliacionRepository.buscarHistorial()}, que en una
 * sola ida a la base de datos trae cada conciliación con sus dos conteos.</p>
 *
 * @param id               id de la conciliación (para el enlace a sus diferencias).
 * @param fecha            día conciliado.
 * @param totalVentasZ     total del reporte Z de ese día.
 * @param estado           BORRADOR o CONFIRMADA.
 * @param articulos        cuántos artículos se conciliaron (líneas).
 * @param conDiferencia    cuántos de ellos no cuadraron (diferencia distinta de cero).
 */
public record CierrePrevio(Long id, LocalDate fecha, BigDecimal totalVentasZ, EstadoConciliacion estado,
                           Long articulos, Long conDiferencia) {

    /** Thymeleaf lo usa para pintar el estado en ámbar o en verde. */
    public boolean esBorrador() {
        return estado == EstadoConciliacion.BORRADOR;
    }
}
