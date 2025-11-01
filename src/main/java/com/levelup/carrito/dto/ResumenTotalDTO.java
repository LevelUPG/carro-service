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
public class ResumenTotalDTO {
    private BigDecimal subtotal;
    private BigDecimal descuentoPorcentaje;
    private BigDecimal descuentoMonto;
    private BigDecimal total;
    private Integer cantidadItems;
}