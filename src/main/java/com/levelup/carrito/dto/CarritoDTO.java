package com.levelup.carrito.dto;

import com.levelup.carrito.model.EstadoCarrito;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarritoDTO {
    private Long id;
    private Long usuarioId;
    private LocalDateTime fechaCreacion;
    private EstadoCarrito estado;
    private BigDecimal descuentoAplicado;
    private List<ItemCarritoDTO> items;
    private BigDecimal subtotal;
    private BigDecimal descuentoMonto;
    private BigDecimal total;
    private Integer cantidadItems;
}