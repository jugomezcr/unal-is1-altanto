package co.edu.unal.sweng1.altanto.presentacion.formulario;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Datos editables de la pantalla para recibir un pedido. */
public class RecepcionPedidoForm {

    @NotEmpty(message = "El pedido debe tener al menos una línea para recibir.")
    private List<@Valid LineaRecepcion> lineas = new ArrayList<>();

    public List<LineaRecepcion> getLineas() {
        return lineas;
    }

    public void setLineas(List<LineaRecepcion> lineas) {
        this.lineas = lineas;
    }

    public static class LineaRecepcion {

        @NotNull(message = "La línea del pedido no es válida.")
        private Long detallePedidoId;

        @NotNull(message = "Digite la cantidad recibida.")
        @PositiveOrZero(message = "La cantidad recibida no puede ser negativa.")
        @Digits(integer = 9, fraction = 3, message = "Use máximo 9 enteros y 3 decimales.")
        private BigDecimal cantidadRecibida;

        public LineaRecepcion() {
        }

        public LineaRecepcion(Long detallePedidoId) {
            this.detallePedidoId = detallePedidoId;
        }

        public Long getDetallePedidoId() {
            return detallePedidoId;
        }

        public void setDetallePedidoId(Long detallePedidoId) {
            this.detallePedidoId = detallePedidoId;
        }

        public BigDecimal getCantidadRecibida() {
            return cantidadRecibida;
        }

        public void setCantidadRecibida(BigDecimal cantidadRecibida) {
            this.cantidadRecibida = cantidadRecibida;
        }
    }
}
