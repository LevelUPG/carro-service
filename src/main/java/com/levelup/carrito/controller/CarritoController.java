package com.levelup.carrito.controller;

import com.levelup.carrito.dto.*;
import com.levelup.carrito.service.CarritoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carrito")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class CarritoController {
    
    private final CarritoService carritoService;
    
    /**
     * GET /api/carrito/{usuarioId}?esDuoc=true
     * Obtener carrito activo del usuario
     */
    @GetMapping("/{usuarioId}")
    public ResponseEntity<CarritoDTO> obtenerCarrito(
            @PathVariable Long usuarioId,
            @RequestParam(defaultValue = "false") boolean esDuoc) {
        
        log.info("GET /api/carrito/{} - esDuoc: {}", usuarioId, esDuoc);
        
        try {
            CarritoDTO carrito = carritoService.obtenerCarritoActivo(usuarioId, esDuoc);
            return ResponseEntity.ok(carrito);
        } catch (Exception e) {
            log.error("Error al obtener carrito: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * POST /api/carrito/{usuarioId}/items?esDuoc=true
     * Agregar producto al carrito
     */
    @PostMapping("/{usuarioId}/items")
    public ResponseEntity<CarritoDTO> agregarProducto(
            @PathVariable Long usuarioId,
            @Valid @RequestBody AgregarItemDTO agregarItemDTO,
            @RequestParam(defaultValue = "false") boolean esDuoc) {
        
        log.info("POST /api/carrito/{}/items - Producto: {}, Cantidad: {}", 
                 usuarioId, agregarItemDTO.getProductoId(), agregarItemDTO.getCantidad());
        
        try {
            CarritoDTO carrito = carritoService.agregarProducto(usuarioId, agregarItemDTO, esDuoc);
            return ResponseEntity.status(HttpStatus.CREATED).body(carrito);
        } catch (IllegalArgumentException e) {
            log.error("Error de validación: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Error al agregar producto: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * PUT /api/carrito/items/{itemId}
     * Actualizar cantidad de un item
     */
    @PutMapping("/items/{itemId}")
    public ResponseEntity<CarritoDTO> actualizarCantidad(
            @PathVariable Long itemId,
            @Valid @RequestBody ActualizarCantidadDTO actualizarDTO) {
        
        log.info("PUT /api/carrito/items/{} - Nueva cantidad: {}", itemId, actualizarDTO.getCantidad());
        
        try {
            CarritoDTO carrito = carritoService.actualizarCantidad(itemId, actualizarDTO.getCantidad());
            return ResponseEntity.ok(carrito);
        } catch (IllegalArgumentException e) {
            log.error("Error de validación: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (RuntimeException e) {
            log.error("Error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("Error al actualizar cantidad: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * DELETE /api/carrito/items/{itemId}
     * Eliminar item del carrito
     */
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<CarritoDTO> eliminarItem(@PathVariable Long itemId) {
        log.info("DELETE /api/carrito/items/{}", itemId);
        
        try {
            CarritoDTO carrito = carritoService.eliminarItem(itemId);
            return ResponseEntity.ok(carrito);
        } catch (RuntimeException e) {
            log.error("Error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("Error al eliminar item: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * DELETE /api/carrito/{usuarioId}/limpiar
     * Vaciar carrito
     */
    @DeleteMapping("/{usuarioId}/limpiar")
    public ResponseEntity<Void> vaciarCarrito(@PathVariable Long usuarioId) {
        log.info("DELETE /api/carrito/{}/limpiar", usuarioId);
        
        try {
            carritoService.vaciarCarrito(usuarioId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            log.error("Error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("Error al vaciar carrito: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * POST /api/carrito/{usuarioId}/cerrar
     * Cerrar carrito (después de compra)
     */
    @PostMapping("/{usuarioId}/cerrar")
    public ResponseEntity<Void> cerrarCarrito(@PathVariable Long usuarioId) {
        log.info("POST /api/carrito/{}/cerrar", usuarioId);
        
        try {
            carritoService.cerrarCarrito(usuarioId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            log.error("Error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("Error al cerrar carrito: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * GET /api/carrito/{usuarioId}/total
     * Obtener resumen del total
     */
    @GetMapping("/{usuarioId}/total")
    public ResponseEntity<ResumenTotalDTO> obtenerTotal(@PathVariable Long usuarioId) {
        log.info("GET /api/carrito/{}/total", usuarioId);
        
        try {
            ResumenTotalDTO resumen = carritoService.obtenerTotal(usuarioId);
            return ResponseEntity.ok(resumen);
        } catch (RuntimeException e) {
            log.error("Error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("Error al obtener total: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}