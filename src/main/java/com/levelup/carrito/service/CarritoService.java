package com.levelup.carrito.service;

import com.levelup.carrito.dto.*;
import com.levelup.carrito.entity.CarritoEntity;
import com.levelup.carrito.entity.ItemCarritoEntity;
import com.levelup.carrito.model.EstadoCarrito;
import com.levelup.carrito.repository.CarritoRepository;
import com.levelup.carrito.repository.ItemCarritoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CarritoService {
    
    private final CarritoRepository carritoRepository;
    private final ItemCarritoRepository itemCarritoRepository;
    
    @Value("${app.descuento.duoc.porcentaje:20}")
    private int descuentoDuocPorcentaje;
    
    /**
     * Obtener o crear carrito activo para un usuario
     */
    @Transactional
    public CarritoDTO obtenerCarritoActivo(Long usuarioId, boolean esDuoc) {
        log.info("Obteniendo carrito activo para usuario: {}", usuarioId);
        
        CarritoEntity carrito = carritoRepository.findByUsuarioIdAndEstado(usuarioId, EstadoCarrito.ACTIVO)
                .orElseGet(() -> crearNuevoCarrito(usuarioId, esDuoc));
        
        // Actualizar descuento si es necesario
        if (esDuoc && carrito.getDescuentoAplicado().compareTo(BigDecimal.ZERO) == 0) {
            carrito.setDescuentoAplicado(new BigDecimal(descuentoDuocPorcentaje));
            carrito = carritoRepository.save(carrito);
        }
        
        return convertirADTO(carrito);
    }
    
    /**
     * Agregar producto al carrito
     */
    @Transactional
    public CarritoDTO agregarProducto(Long usuarioId, AgregarItemDTO agregarItemDTO, boolean esDuoc) {
        log.info("Agregando producto {} al carrito del usuario {}", agregarItemDTO.getProductoId(), usuarioId);
        
        // Validaciones
        if (agregarItemDTO.getCantidad() <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
        }
        
        // Obtener o crear carrito
        CarritoEntity carrito = carritoRepository.findByUsuarioIdAndEstado(usuarioId, EstadoCarrito.ACTIVO)
                .orElseGet(() -> crearNuevoCarrito(usuarioId, esDuoc));
        
        // Verificar si el producto ya existe en el carrito
        ItemCarritoEntity itemExistente = itemCarritoRepository
                .findByCarritoIdAndProductoId(carrito.getId(), agregarItemDTO.getProductoId())
                .orElse(null);
        
        if (itemExistente != null) {
            // Si existe, sumar cantidades
            itemExistente.setCantidad(itemExistente.getCantidad() + agregarItemDTO.getCantidad());
            itemExistente.calcularSubtotal();
            itemCarritoRepository.save(itemExistente);
            log.info("Producto existente, nueva cantidad: {}", itemExistente.getCantidad());
        } else {
            // Si no existe, crear nuevo item
            ItemCarritoEntity nuevoItem = new ItemCarritoEntity();
            nuevoItem.setCarrito(carrito);
            nuevoItem.setProductoId(agregarItemDTO.getProductoId());
            nuevoItem.setNombreProducto(agregarItemDTO.getNombreProducto());
            nuevoItem.setPrecioUnitario(agregarItemDTO.getPrecioUnitario());
            nuevoItem.setCantidad(agregarItemDTO.getCantidad());
            nuevoItem.calcularSubtotal();
            
            carrito.addItem(nuevoItem);
            itemCarritoRepository.save(nuevoItem);
            log.info("Nuevo producto agregado al carrito");
        }
        
        // Recalcular total del carrito
        calcularTotalCarrito(carrito);
        carrito = carritoRepository.save(carrito);
        
        return convertirADTO(carrito);
    }
    
    /**
     * Actualizar cantidad de un item
     */
    @Transactional
    public CarritoDTO actualizarCantidad(Long itemId, int nuevaCantidad) {
        log.info("Actualizando cantidad del item {} a {}", itemId, nuevaCantidad);
        
        if (nuevaCantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
        }
        
        ItemCarritoEntity item = itemCarritoRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item no encontrado con ID: " + itemId));
        
        item.setCantidad(nuevaCantidad);
        item.calcularSubtotal();
        itemCarritoRepository.save(item);
        
        CarritoEntity carrito = item.getCarrito();
        calcularTotalCarrito(carrito);
        carrito = carritoRepository.save(carrito);
        
        return convertirADTO(carrito);
    }
    
    /**
     * Eliminar item del carrito
     */
    @Transactional
    public CarritoDTO eliminarItem(Long itemId) {
        log.info("Eliminando item del carrito: {}", itemId);
        
        ItemCarritoEntity item = itemCarritoRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item no encontrado con ID: " + itemId));
        
        CarritoEntity carrito = item.getCarrito();
        carrito.removeItem(item);
        itemCarritoRepository.delete(item);
        
        calcularTotalCarrito(carrito);
        carrito = carritoRepository.save(carrito);
        
        return convertirADTO(carrito);
    }
    
    /**
     * Vaciar carrito
     */
    @Transactional
    public void vaciarCarrito(Long usuarioId) {
        log.info("Vaciando carrito del usuario: {}", usuarioId);
        
        CarritoEntity carrito = carritoRepository.findByUsuarioIdAndEstado(usuarioId, EstadoCarrito.ACTIVO)
                .orElseThrow(() -> new RuntimeException("No hay carrito activo para el usuario: " + usuarioId));
        
        carrito.getItems().clear();
        carrito.setTotal(BigDecimal.ZERO);
        carritoRepository.save(carrito);
    }
    
    /**
     * Cerrar carrito (después de compra)
     */
    @Transactional
    public void cerrarCarrito(Long usuarioId) {
        log.info("Cerrando carrito del usuario: {}", usuarioId);
        
        CarritoEntity carrito = carritoRepository.findByUsuarioIdAndEstado(usuarioId, EstadoCarrito.ACTIVO)
                .orElseThrow(() -> new RuntimeException("No hay carrito activo para el usuario: " + usuarioId));
        
        carrito.setEstado(EstadoCarrito.CERRADO);
        carritoRepository.save(carrito);
    }
    
    /**
     * Obtener resumen del total
     */
    @Transactional(readOnly = true)
    public ResumenTotalDTO obtenerTotal(Long usuarioId) {
        log.info("Obteniendo total del carrito para usuario: {}", usuarioId);
        
        CarritoEntity carrito = carritoRepository.findByUsuarioIdAndEstado(usuarioId, EstadoCarrito.ACTIVO)
                .orElseThrow(() -> new RuntimeException("No hay carrito activo para el usuario: " + usuarioId));
        
        BigDecimal subtotal = carrito.getItems().stream()
                .map(ItemCarritoEntity::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal descuento = subtotal.multiply(carrito.getDescuentoAplicado())
                .divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
        
        BigDecimal total = subtotal.subtract(descuento);
        
        return ResumenTotalDTO.builder()
                .subtotal(subtotal)
                .descuentoPorcentaje(carrito.getDescuentoAplicado())
                .descuentoMonto(descuento)
                .total(total)
                .cantidadItems(carrito.getItems().size())
                .build();
    }
    
    // ===== MÉTODOS PRIVADOS =====
    
    private CarritoEntity crearNuevoCarrito(Long usuarioId, boolean esDuoc) {
        log.info("Creando nuevo carrito para usuario: {}", usuarioId);
        
        CarritoEntity carrito = new CarritoEntity();
        carrito.setUsuarioId(usuarioId);
        carrito.setEstado(EstadoCarrito.ACTIVO);
        carrito.setDescuentoAplicado(esDuoc ? new BigDecimal(descuentoDuocPorcentaje) : BigDecimal.ZERO);
        carrito.setTotal(BigDecimal.ZERO);
        
        return carritoRepository.save(carrito);
    }
    
    private void calcularTotalCarrito(CarritoEntity carrito) {
        BigDecimal subtotal = carrito.getItems().stream()
                .map(ItemCarritoEntity::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal descuento = subtotal.multiply(carrito.getDescuentoAplicado())
                .divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
        
        BigDecimal total = subtotal.subtract(descuento);
        carrito.setTotal(total);
    }
    
    private CarritoDTO convertirADTO(CarritoEntity carrito) {
        List<ItemCarritoDTO> itemsDTO = carrito.getItems().stream()
                .map(this::convertirItemADTO)
                .collect(Collectors.toList());
        
        BigDecimal subtotal = carrito.getItems().stream()
                .map(ItemCarritoEntity::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal descuento = subtotal.multiply(carrito.getDescuentoAplicado())
                .divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
        
        return CarritoDTO.builder()
                .id(carrito.getId())
                .usuarioId(carrito.getUsuarioId())
                .fechaCreacion(carrito.getFechaCreacion())
                .estado(carrito.getEstado())
                .descuentoAplicado(carrito.getDescuentoAplicado())
                .items(itemsDTO)
                .subtotal(subtotal)
                .descuentoMonto(descuento)
                .total(carrito.getTotal())
                .cantidadItems(carrito.getItems().size())
                .build();
    }
    
    private ItemCarritoDTO convertirItemADTO(ItemCarritoEntity item) {
        return ItemCarritoDTO.builder()
                .id(item.getId())
                .productoId(item.getProductoId())
                .nombreProducto(item.getNombreProducto())
                .precioUnitario(item.getPrecioUnitario())
                .cantidad(item.getCantidad())
                .subtotal(item.getSubtotal())
                .build();
    }
}