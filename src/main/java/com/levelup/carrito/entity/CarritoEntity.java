package com.levelup.carrito.entity;

import com.levelup.carrito.model.EstadoCarrito;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "carritos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarritoEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long usuarioId;
    
    @Column(nullable = false)
    private LocalDateTime fechaCreacion;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoCarrito estado;
    
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal descuentoAplicado;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;
    
    @OneToMany(mappedBy = "carrito", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemCarritoEntity> items = new ArrayList<>();
    
    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        if (estado == null) {
            estado = EstadoCarrito.ACTIVO;
        }
        if (descuentoAplicado == null) {
            descuentoAplicado = BigDecimal.ZERO;
        }
        if (total == null) {
            total = BigDecimal.ZERO;
        }
    }
    
    // Método para agregar item al carrito
    public void addItem(ItemCarritoEntity item) {
        items.add(item);
        item.setCarrito(this);
    }
    
    // Método para remover item del carrito
    public void removeItem(ItemCarritoEntity item) {
        items.remove(item);
        item.setCarrito(null);
    }
}