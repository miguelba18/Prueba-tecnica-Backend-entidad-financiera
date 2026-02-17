# Prueba-tecnica-Backend-entidad-financiera


API REST desarrollada en Java con Spring Boot para la administración de clientes, productos financieros y transacciones de una entidad financiera.

## Tecnologías utilizadas

- Java 17
- Spring Boot 3.5.10
- Spring Data JPA / Hibernate
- PostgreSQL
- Maven
- Lombok
- JUnit 5 + Mockito

## Requisitos previos

- Java 17+
- PostgreSQL
- Maven

## Configuración de la base de datos

Crear la base de datos y el usuario en PostgreSQL:
```sql
CREATE DATABASE financiera_db;
CREATE USER financiera_user WITH PASSWORD 'financiera123';
GRANT ALL PRIVILEGES ON DATABASE financiera_db TO financiera_user;
```

## Configuración del proyecto

Editar el archivo `src/main/resources/application.properties` si es necesario:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/financiera_db
spring.datasource.username=financiera_user
spring.datasource.password=financiera123
server.port=8080
```

## Ejecutar el proyecto
```bash
./mvnw spring-boot:run
```

## Ejecutar los tests
```bash
./mvnw test
```

## Estructura del proyecto
```
src/main/java/com/financiera/backend/
├── controller/      → Endpoints REST
├── service/         → Lógica de negocio
├── repository/      → Acceso a base de datos
├── entity/          → Entidades JPA
├── dto/             → Objetos de transferencia
└── exception/       → Manejo de errores
```

## Endpoints disponibles

### Clientes
| Método | URL | Descripción |
|--------|-----|-------------|
| POST | `/api/clientes` | Crear cliente |
| GET | `/api/clientes` | Listar clientes |
| GET | `/api/clientes/{id}` | Obtener cliente |
| PUT | `/api/clientes/{id}` | Actualizar cliente |
| DELETE | `/api/clientes/{id}` | Eliminar cliente |

### Productos (Cuentas)
| Método | URL | Descripción |
|--------|-----|-------------|
| POST | `/api/productos` | Crear cuenta |
| GET | `/api/productos` | Listar cuentas |
| GET | `/api/productos/{id}` | Obtener cuenta |
| GET | `/api/productos/cliente/{id}` | Cuentas por cliente |
| PATCH | `/api/productos/{id}/estado` | Cambiar estado |
| DELETE | `/api/productos/{id}` | Eliminar cuenta |

### Transacciones
| Método | URL | Descripción |
|--------|-----|-------------|
| POST | `/api/transacciones/consignacion` | Consignar dinero |
| POST | `/api/transacciones/retiro` | Retirar dinero |
| POST | `/api/transacciones/transferencia` | Transferir entre cuentas |
| GET | `/api/transacciones/{id}` | Obtener transacción |
| GET | `/api/transacciones/estado-cuenta/{id}` | Historial de cuenta |

## Reglas de negocio principales

- Los clientes deben ser mayores de edad
- Un cliente no puede eliminarse si tiene productos vinculados
- Los números de cuenta se generan automáticamente (ahorros inicia en "53", corriente en "33")
- Las cuentas de ahorro no pueden tener saldo negativo
- Solo se pueden cancelar cuentas con saldo $0
- Las transferencias generan movimiento débito y crédito automáticamente

## Autor
Miguel Bahamon
Prueba técnica — Prácticas profesionales
