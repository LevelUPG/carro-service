package com.levelup.carrito.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemCarritoDTO {
    private Long id;
    private Long productoId;
    private String nombreProducto;
    private BigDecimal precioUnitario;
    private Integer cantidad;
    private BigDecimal subtotal;
}