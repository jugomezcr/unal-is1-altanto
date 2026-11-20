package co.edu.unal.sweng1.altanto.negocio.dto;

import java.math.BigDecimal;

/** Cantidad recibida asociada a una línea concreta, no solo a un artículo. */
public record CantidadRecibidaPedido(Long detallePedidoId, BigDecimal cantidadRecibida) {
}
