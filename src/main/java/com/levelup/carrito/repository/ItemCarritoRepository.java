package com.levelup.carrito.repository;

import com.levelup.carrito.entity.ItemCarritoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ItemCarritoRepository extends JpaRepository<ItemCarritoEntity, Long> {
    
    // Query Objetual - Buscar item por carrito y producto
    @Query("SELECT i FROM ItemCarritoEntity i WHERE i.carrito.id = :carritoId AND i.productoId = :productoId")
    Optional<ItemCarritoEntity> findByCarritoIdAndProductoId(
            @Param("carritoId") Long carritoId, 
            @Param("productoId") Long productoId
    );
    
    // Query Nativa - Buscar item por carrito y producto
    @Query(value = "SELECT * FROM items_carrito WHERE carrito_id = :carritoId AND producto_id = :productoId",
            nativeQuery = true)
    Optional<ItemCarritoEntity> findItemNativo(
            @Param("carritoId") Long carritoId,
            @Param("productoId") Long productoId
    );
    
    // Query Objetual - Contar items en carrito
    @Query("SELECT COUNT(i) FROM ItemCarritoEntity i WHERE i.carrito.id = :carritoId")
    int countItemsByCarritoId(@Param("carritoId") Long carritoId);
    
    // Query Nativa - Obtener cantidad total de productos en carrito
    @Query(value = "SELECT COALESCE(SUM(cantidad), 0) FROM items_carrito WHERE carrito_id = :carritoId",
            nativeQuery = true)
    int getCantidadTotalProductos(@Param("carritoId") Long carritoId);
}