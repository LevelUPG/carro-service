#  Microservicio Carrito de Compras - Level-Up Gamer

##  Descripción
Microservicio independiente para la gestión del carrito de compras de la tienda online Level-Up Gamer.

##  Arquitectura
- **Framework**: Spring Boot 3.3.4
- **Java**: 17
- **Base de datos**: MySQL 8.0
- **Puerto**: 8082
- **Package base**: com.levelup.carrito

##  Configuración Inicial

### 1. Requisitos Previos
- Java 17 o superior
- Maven 3.8+
- MySQL 8.0
- Postman (para pruebas)

### 2. Configuración de Base de Datos

-- La base de datos se crea automáticamente al iniciar la aplicación
-- Usuario: root
-- Password: admin
-- Database: levelup_carrito

### 3. Instalación

# Clonar o crear el proyecto
mvn clean install

# Ejecutar la aplicación
mvn spring-boot:run

La aplicación estará disponible en: `http://localhost:8082`

##  Modelo de Datos

### Entidades

#### CarritoEntity
- `id`: Long (PK, Auto)
- `usuarioId`: Long
- `fechaCreacion`: LocalDateTime
- `estado`: EstadoCarrito (ACTIVO/CERRADO)
- `descuentoAplicado`: BigDecimal
- `total`: BigDecimal

#### ItemCarritoEntity
- `id`: Long (PK, Auto)
- `carritoId`: Long (FK → carritos)
- `productoId`: Long
- `nombreProducto`: String
- `precioUnitario`: BigDecimal
- `cantidad`: Integer
- `subtotal`: BigDecimal

### Relaciones
- **Carrito → Items**: OneToMany
- Un usuario solo puede tener un carrito ACTIVO a la vez

##  Endpoints API

### 1. Obtener Carrito Activo
GET /api/carrito/{usuarioId}?esDuoc={true|false}
**Response**: CarritoDTO con todos los items

### 2. Agregar Producto al Carrito
POST /api/carrito/{usuarioId}/items?esDuoc={true|false}
Content-Type: application/json

{
  "productoId": 101,
  "nombreProducto": "Mouse Gamer Logitech G502",
  "precioUnitario": 45990.00,
  "cantidad": 2
}

### 3. Actualizar Cantidad de Item
PUT /api/carrito/items/{itemId}
Content-Type: application/json

{
  "cantidad": 5
}

### 4. Eliminar Item del Carrito
DELETE /api/carrito/items/{itemId}

### 5. Vaciar Carrito
DELETE /api/carrito/{usuarioId}/limpiar

### 6. Cerrar Carrito (después de compra)
POST /api/carrito/{usuarioId}/cerrar

### 7. Obtener Total del Carrito
GET /api/carrito/{usuarioId}/total

**Response**:
{
  "subtotal": 391970.00,
  "descuentoPorcentaje": 20.00,
  "descuentoMonto": 78394.00,
  "total": 313576.00,
  "cantidadItems": 3
}

##  Reglas de Negocio

### Descuentos
- **Usuarios @duoc.cl**: 20% de descuento automático
- Se aplica sobre el subtotal total del carrito

### Gestión de Items
- Si se agrega un producto existente, se suman las cantidades
- No se permiten cantidades ≤ 0
- Al modificar un item, se recalcula automáticamente el total

### Estados del Carrito
- **ACTIVO**: Carrito en uso
- **CERRADO**: Carrito procesado después de compra

## 🧪 Casos de Prueba

### Flujo Básico (Usuario Normal)
1. GET `/api/carrito/1?esDuoc=false` → Crea carrito sin descuento
2. POST `/api/carrito/1/items` → Agregar Mouse (2 unidades)
3. POST `/api/carrito/1/items` → Agregar Silla Gamer (1 unidad)
4. GET `/api/carrito/1` → Ver carrito completo
5. PUT `/api/carrito/items/1` → Cambiar cantidad a 5
6. GET `/api/carrito/1/total` → Ver resumen de totales
7. POST `/api/carrito/1/cerrar` → Cerrar carrito

### Flujo con Descuento DUOC
1. GET `/api/carrito/2?esDuoc=true` → Crea carrito con 20% descuento
2. POST `/api/carrito/2/items?esDuoc=true` → Agregar Teclado
3. Verificar que el total aplique el 20% de descuento

### Suma de Cantidades
1. POST `/api/carrito/1/items` → Agregar Mouse (cantidad: 2)
2. POST `/api/carrito/1/items` → Agregar mismo Mouse (cantidad: 1)
3. Verificar que la cantidad total sea 3

##  Estructura del Proyecto

src/main/java/com/levelup/carrito/
├── entity/
│   ├── CarritoEntity.java
│   └── ItemCarritoEntity.java
├── repository/
│   ├── CarritoRepository.java
│   └── ItemCarritoRepository.java
├── model/
│   └── EstadoCarrito.java
├── dto/
│   ├── CarritoDTO.java
│   ├── ItemCarritoDTO.java
│   ├── AgregarItemDTO.java
│   ├── ActualizarCantidadDTO.java
│   └── ResumenTotalDTO.java
├── service/
│   └── CarritoService.java
├── controller/
│   └── CarritoController.java
└── CarritoServiceApplication.java

##  Tecnologías Utilizadas

- **Spring Boot Starter Web**: API REST
- **Spring Boot Starter Data JPA**: Persistencia
- **Spring Boot Starter Validation**: Validaciones
- **MySQL Connector**: Driver de base de datos
- **Lombok**: Reducción de código boilerplate
- **SLF4J**: Logging

##  Características Implementadas

 Solo un carrito activo por usuario  
 Agregar productos con validación  
 Modificar cantidades  
 Eliminar items  
 Calcular totales con descuentos  
 Descuento DUOC automático (20%)  
 Limpiar carrito  
 Cerrar carrito después de compra  
 Suma automática de cantidades para productos existentes  
 Queries nativas y objetuales en repositories  
 Validaciones de negocio  
 Logs detallados  

##  Notas Adicionales

- **Sin JWT**: La autenticación se implementará en la evaluación 3
- **Arquitectura independiente**: No depende del microservicio de usuarios
- **Validación por ID**: Por ahora se valida la existencia de productos por ID
- **Cálculos automáticos**: Subtotales y totales se recalculan en cada operación

##  Autor
Microservicio desarrollado para el proyecto Level-Up Gamer

##  Licencia
Proyecto académico - DuocUC