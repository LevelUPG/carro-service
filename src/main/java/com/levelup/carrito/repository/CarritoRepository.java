package com.levelup.carrito.repository;

import com.levelup.carrito.entity.CarritoEntity;
import com.levelup.carrito.model.EstadoCarrito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CarritoRepository extends JpaRepository<CarritoEntity, Long> {
    
    // Query Objetual - Buscar carrito activo por usuario
    @Query("SELECT c FROM CarritoEntity c WHERE c.usuarioId = :usuarioId AND c.estado = :estado")
    Optional<CarritoEntity> findByUsuarioIdAndEstado(
            @Param("usuarioId") Long usuarioId, 
            @Param("estado") EstadoCarrito estado
    );
    
    // Query Nativa - Buscar carrito activo por usuario
    @Query(value = "SELECT * FROM carritos WHERE usuario_id = :usuarioId AND estado = 'ACTIVO' LIMIT 1",
            nativeQuery = true)
    Optional<CarritoEntity> findCarritoActivoNativo(@Param("usuarioId") Long usuarioId);
    
    // Query Objetual - Buscar todos los carritos de un usuario
    @Query("SELECT c FROM CarritoEntity c WHERE c.usuarioId = :usuarioId ORDER BY c.fechaCreacion DESC")
    java.util.List<CarritoEntity> findAllByUsuarioId(@Param("usuarioId") Long usuarioId);
    
    // Query Nativa - Contar carritos activos por usuario
    @Query(value = "SELECT COUNT(*) FROM carritos WHERE usuario_id = :usuarioId AND estado = 'ACTIVO'",
            nativeQuery = true)
    int countCarritosActivos(@Param("usuarioId") Long usuarioId);
    
    // Query Objetual - Verificar existencia de carrito activo
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM CarritoEntity c " +
            "WHERE c.usuarioId = :usuarioId AND c.estado = 'ACTIVO'")
    boolean existsCarritoActivo(@Param("usuarioId") Long usuarioId);
}