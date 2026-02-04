# Nails App - Arquitectura de Microservicios

Sistema distribuido construido con **Java Spring Boot** y **Spring Cloud**, compuesto por 7 microservicios independientes que se comunican mediante REST, RabbitMQ y comparten datos a través de 5 bases de datos PostgreSQL.

---

## 🏗️ Arquitectura General

```
┌─────────────────────────────────────────────────────────────────────────┐
│                              CLIENTE                                     │
│                     (Web App / Mobile App)                              │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                        API GATEWAY                                      │
│              (Spring Cloud Gateway - Puerto 8080)                      │
│                                                                         │
│  • Enrutamiento de peticiones                                           │
│  • Balanceo de carga                                                    │
│  • Rate limiting                                                        │
│  • Autenticación inicial (JWT validation)                               │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                ┌───────────────────┼───────────────────┐
                ▼                   ▼                   ▼
    ┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐
    │  EUREKA SERVER   │  │  CONFIG SERVER   │  │    RABBITMQ    │
    │   (Puerto 8761)  │  │   (Spring Cloud) │  │   (Puerto 5672)│
    │                  │  │                  │  │                │
    │  Service         │  │  Configuración   │  │  Mensajería    │
    │  Discovery       │  │  Centralizada    │  │  Asíncrona     │
    └──────────────────┘  └──────────────────┘  └──────────────────┘
                │                   │                   │
                └───────────────────┼───────────────────┘
                                    │
    ┌───────────────────────────────┼───────────────────────────────┐
    │                               │                               │
    ▼                               ▼                               ▼
┌──────────────┐          ┌──────────────┐          ┌──────────────┐
│   IDENTITY   │          │   CATALOG    │          │   BOOKING    │
│   SERVICE    │          │   SERVICE    │          │   SERVICE    │
│   :8081      │          │   :8082      │          │   :8083      │
│              │          │              │          │              │
│ • JWT Auth   │          │ • Servicios  │          │ • Citas      │
│ • Perfiles   │          │ • Portfolio  │          │ • Schedule   │
│ • Ratings    │          │ • Precios    │          │ • Conflictos │
└──────┬───────┘          └──────┬───────┘          └──────┬───────┘
       │                         │                         │
       ▼                         ▼                         ▼
┌──────────────┐          ┌──────────────┐          ┌──────────────┐
│   REVIEWS    │          │    SEARCH    │          │   PostgreSQL │
│   SERVICE    │          │   SERVICE    │          │   Databases  │
│   :8084      │          │   :8085      │          │              │
│              │          │              │          │ • identity   │
│ • Reseñas    │          │ • Búsqueda   │          │ • catalog    │
│ • Ratings    │          │   geoespacial│          │ • booking    │
│ • Comentarios│          │ • Index      │          │ • reviews    │
└──────────────┘          └──────────────┘          │ • search     │
                                                      └──────────────┘
```

---

## 📊 Detalle de Microservicios

### 1. 🚪 API Gateway (Spring Cloud Gateway)
**Puerto:** `8080`

**Responsabilidades:**
- Punto de entrada único para todos los clientes
- Enrutamiento de requests a microservicios
- Balanceo de carga entre instancias
- Rate limiting y throttling
- Validación inicial de JWT tokens
- CORS handling

**Endpoints expuestos:**
```
/api/auth/*       → identity-service
/api/profile/*    → identity-service
/api/services/*   → catalog-service
/api/portfolio/*  → catalog-service
/api/bookings/*   → booking-service
/api/reviews/*    → reviews-service
/api/search/*     → search-service
```

**Configuración clave:**
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: identity-service
          uri: lb://identity-service
          predicates:
            - Path=/api/auth/**, /api/profile/**
```

---

### 2. 📍 Eureka Server (Service Discovery)
**Puerto:** `8761`

**Responsabilidades:**
- Registro y descubrimiento de servicios
- Health checking de instancias
- Balanceo de carga del lado del cliente
- Dashboard de servicios disponibles

**Servicios registrados:**
- `IDENTITY-SERVICE` (instancias: n)
- `CATALOG-SERVICE` (instancias: n)
- `BOOKING-SERVICE` (instancias: n)
- `REVIEWS-SERVICE` (instancias: n)
- `SEARCH-SERVICE` (instancias: n)
- `API-GATEWAY` (instancias: n)

**URL Dashboard:** http://localhost:8761

---

### 3. 👤 Identity Service
**Puerto:** `8081`
**Base de datos:** `nails_identity`

**Responsabilidades:**
- Autenticación y autorización (JWT)
- Gestión de usuarios y perfiles
- Manejo de roles (CLIENT, SPECIALIST, ADMIN)
- Cálculo y actualización de ratings

**Entidades JPA:**
```java
@Entity
public class User {
    @Id @GeneratedValue
    private Long id;
    private String email;
    private String passwordHash;
    @Enumerated(EnumType.STRING)
    private UserRole role;
}

@Entity
public class Profile {
    @Id @GeneratedValue
    private Long id;
    @OneToOne
    private User user;
    private String fullName;
    private String bio;
    private BigDecimal avgRating;
    private Integer ratingCount;
}
```

**API Endpoints:**
```
POST /api/auth/register          → Registro de usuarios
POST /api/auth/login             → Login (JWT)
GET  /api/auth/me                → Perfil del usuario autenticado
PUT  /api/profile/{id}           → Actualizar perfil
GET  /api/profile/{id}/rating    → Obtener rating del especialista
```

**Eventos RabbitMQ:**
- **Consume:** `review.created` → Actualiza `avgRating` y `ratingCount`

**Dependencias:**
- Eureka Client
- Spring Security + JWT
- Spring Data JPA
- PostgreSQL
- RabbitMQ Listener

---

### 4. 📦 Catalog Service
**Puerto:** `8082`
**Base de datos:** `nails_catalog`

**Responsabilidades:**
- Gestión de servicios ofrecidos por especialistas
- Portfolio de imágenes (trabajos realizados)
- Precios y duración de servicios
- Validación de existencia de especialistas (vía Feign)

**Entidades JPA:**
```java
@Entity
public class Service {
    @Id @GeneratedValue
    private Long id;
    private String title;
    private String description;
    private BigDecimal price;
    private Integer durationMinutes;
    @ManyToOne
    private User specialist;
}

@Entity
public class PortfolioImage {
    @Id @GeneratedValue
    private Long id;
    private String imageUrl;
    private String description;
    @ManyToOne
    private User specialist;
}
```

**API Endpoints:**
```
POST /api/services              → Crear servicio
GET  /api/services?specialistId=X → Listar servicios
GET  /api/services/{id}          → Obtener servicio
DELETE /api/services/{id}        → Eliminar servicio
POST /api/portfolio              → Agregar imagen
GET  /api/portfolio?specialistId=X → Listar portfolio
```

**Comunicación síncrona (Feign Client):**
```java
@FeignClient(name = "identity-service")
public interface IdentityClient {
    @GetMapping("/api/profile/{id}/exists")
    boolean checkUserExists(@PathVariable Long id);
}
```

**Eventos RabbitMQ:**
- **Publica:** `price.changed` → Cuando cambia el precio de un servicio

**Dependencias:**
- Eureka Client
- OpenFeign (para llamar a identity-service)
- Spring Data JPA
- PostgreSQL
- RabbitMQ Template

---

### 5. 📅 Booking Service
**Puerto:** `8083`
**Base de datos:** `nails_booking`

**Responsabilidades:**
- Gestión de citas/appointments
- Detección de conflictos de horario (doble-booking protection)
- Gestión de disponibilidad de especialistas
- Estados de citas: PENDING, CONFIRMED, REJECTED, CANCELLED, COMPLETED

**Entidades JPA:**
```java
@Entity
public class Appointment {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private User client;
    @ManyToOne
    private User specialist;
    private Long serviceId;  // Solo ID, no relación JPA
    private LocalDateTime scheduledAt;
    private LocalDateTime endTime;
    @Enumerated(EnumType.STRING)
    private AppointmentStatus status;
}

@Entity
public class AvailabilitySlot {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private User specialist;
    @Enumerated(EnumType.STRING)
    private DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
}
```

**Auditoría (Envers):**
```java
@Entity
@Audited
public class Appointment {
    // Campos auditados automáticamente
}
```

**API Endpoints:**
```
POST /api/bookings                    → Crear cita
GET  /api/bookings?clientId=X         → Listar citas del cliente
GET  /api/bookings/{id}               → Obtener cita
PATCH /api/bookings/{id}/status       → Actualizar estado
POST /api/bookings/availability       → Crear slot disponible
GET  /api/bookings/availability?specialistId=X → Ver disponibilidad
```

**Lógica anti-conflictos:**
```java
public boolean hasConflicts(Appointment newAppointment) {
    return appointmentRepository.findConflictingAppointments(
        newAppointment.getSpecialistId(),
        newAppointment.getScheduledAt(),
        newAppointment.getEndTime()
    ).size() > 0;
}
```

**Eventos RabbitMQ:**
- **Publica:** `appointment.status_changed` → Cuando cambia el estado de una cita

**Dependencias:**
- Eureka Client
- Spring Data JPA
- Hibernate Envers (auditoría)
- PostgreSQL
- RabbitMQ Template

---

### 6. ⭐ Reviews Service
**Puerto:** `8084`
**Base de datos:** `nails_reviews`

**Responsabilidades:**
- Gestión de reseñas y ratings
- Validación de citas completadas antes de permitir reseña
- Cálculo de promedios (delegado a identity-service vía eventos)

**Entidades JPA:**
```java
@Entity
public class Review {
    @Id @GeneratedValue
    private Long id;
    @Column(unique = true)
    private Long appointmentId;  // Solo ID, no relación JPA
    private Long specialistId;  // Solo ID, no relación JPA
    private Integer rating;  // 1-5
    private String comment;
    private LocalDateTime createdAt;
}
```

**API Endpoints:**
```
POST /api/reviews                     → Crear reseña
GET  /api/reviews/specialist/{id}     → Listar reseñas del especialista
GET  /api/reviews/appointment/{id}    → Obtener reseña por cita
```

**Comunicación síncrona (Feign Client):**
```java
@FeignClient(name = "booking-service")
public interface BookingClient {
    @GetMapping("/api/bookings/{id}")
    AppointmentDTO getAppointment(@PathVariable Long id);
}
```

**Eventos RabbitMQ:**
- **Publica:** `review.created` → Con los datos de la reseña
  ```json
  {
    "specialistId": 123,
    "rating": 5,
    "reviewId": 456,
    "appointmentId": 789
  }
  ```

**Dependencias:**
- Eureka Client
- OpenFeign (para validar citas)
- Spring Data JPA
- PostgreSQL
- RabbitMQ Template

---

### 7. 🔍 Search Service
**Puerto:** `8085`
**Base de datos:** `nails_search` (PostgreSQL + PostGIS)

**Responsabilidades:**
- Búsqueda geoespacial de especialistas
- Filtrado por precio, rating, distancia
- Indexación de ubicaciones

**Entidades JPA:**
```java
@Entity
public class SpecialistLocation {
    @Id @GeneratedValue
    private Long id;
    private Long specialistId;  // Solo ID
    private String specialistName;
    private BigDecimal minPrice;
    private BigDecimal avgRating;
    private Boolean isAvailable;
    
    @Column(columnDefinition = "geometry(Point, 4326)")
    private Point location;  // PostGIS
}
```

**API Endpoints:**
```
GET /api/search?lat=X&lon=Y&radius=10        → Buscar especialistas cercanos
GET /api/search?minPrice=X&maxPrice=Y        → Filtrar por precio
GET /api/search?minRating=X                   → Filtrar por rating
```

**Query geoespacial (PostGIS):**
```sql
SELECT * FROM specialist_locations
WHERE ST_DWithin(
    location,
    ST_SetSRID(ST_MakePoint(?, ?), 4326),
    ?  -- radio en metros
)
AND is_available = true
ORDER BY ST_Distance(location, ST_SetSRID(ST_MakePoint(?, ?), 4326));
```

**Eventos RabbitMQ:**
- **Consume:** `price.changed` → Actualiza `minPrice` en el índice
- **Consume:** `review.created` → Actualiza `avgRating` en el índice

**Dependencias:**
- Eureka Client
- Spring Data JPA
- Hibernate Spatial (PostGIS)
- PostgreSQL + PostGIS
- RabbitMQ Listener

---

## 🔄 Patrones de Comunicación

### 1. Comunicación Síncrona (REST + Feign Client)

**Flujo típico:**
```
Cliente → API Gateway → Microservicio A → Feign → Microservicio B
```

**Ejemplos:**
- Catalog Service → Identity Service (validar especialista)
- Reviews Service → Booking Service (validar cita completada)

**Configuración Feign:**
```java
@EnableFeignClients
@SpringBootApplication
public class CatalogServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(CatalogServiceApplication.class, args);
    }
}
```

### 2. Comunicación Asíncrona (RabbitMQ)

**Arquitectura de Mensajería:**

```
┌──────────────────┐
│   Reviews        │
│   Service        │──(review.created)──┐
└──────────────────┘                   │
                                       ▼
                              ┌──────────────────┐
                              │  reviews.exchange│
                              │   (fanout)       │
                              └────────┬─────────┘
                                       │
                    ┌──────────────────┼──────────────────┐
                    │                  │                  │
                    ▼                  ▼                  ▼
          ┌──────────────┐   ┌──────────────┐   ┌──────────────┐
          │ identity.sr  │   │ notification │   │ search.sr    │
          │ rating.queue   │   │ queue        │   │ queue        │
          └──────┬───────┘   └──────┬───────┘   └──────┬───────┘
                 │                  │                  │
                 ▼                  ▼                  ▼
          ┌──────────────┐   ┌──────────────┐   ┌──────────────┐
          │  Identity    │   │  Notification│   │   Search     │
          │  Service       │   │  Service       │   │   Service    │
          └──────────────┘   └──────────────┘   └──────────────┘
```

**Eventos y Colas:**

| Evento | Publicador | Suscriptores | Acción |
|--------|-----------|--------------|--------|
| `review.created` | Reviews | Identity | Actualizar rating |
| `review.created` | Reviews | Search | Actualizar índice |
| `price.changed` | Catalog | Search | Actualizar min_price |
| `appointment.status_changed` | Booking | Notification | Enviar email |

**Configuración RabbitMQ (Spring):**
```java
@Configuration
public class RabbitConfig {
    
    @Bean
    public TopicExchange reviewsExchange() {
        return new TopicExchange("reviews.exchange");
    }
    
    @Bean
    public Queue ratingQueue() {
        return new Queue("specialist.rating.queue");
    }
    
    @Bean
    public Binding ratingBinding(Queue ratingQueue, TopicExchange reviewsExchange) {
        return BindingBuilder.bind(ratingQueue)
            .to(reviewsExchange)
            .with("review.created");
    }
}
```

---

## 🗄️ Bases de Datos

Cada microservicio tiene su propia base de datos (Database-per-Service pattern):

| Microservicio | Base de Datos | Tablas Principales |
|--------------|---------------|-------------------|
| Identity | `nails_identity` | users, profiles |
| Catalog | `nails_catalog` | services, portfolio_images |
| Booking | `nails_booking` | appointments, availability_slots, appointments_aud |
| Reviews | `nails_reviews` | reviews |
| Search | `nails_search` | specialist_locations (PostGIS) |

**Configuración de Conexión:**
```properties
# application.properties en cada servicio
spring.datasource.url=jdbc:postgresql://localhost:5432/nails_identity
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.jpa.hibernate.ddl-auto=update
```

---

## 🔐 Seguridad

### Autenticación JWT

**Flujo:**
```
1. Cliente POST /api/auth/login (email, password)
                    ↓
2. Identity Service valida credenciales
                    ↓
3. Genera JWT firmado
                    ↓
4. Retorna token al cliente
                    ↓
5. Cliente incluye token en header: Authorization: Bearer <token>
                    ↓
6. API Gateway valida JWT en cada request
                    ↓
7. Si válido, enruta al microservicio correspondiente
```

**Configuración Spring Security:**
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}
```

---

## 📊 Configuración Centralizada (Spring Cloud Config)

**Repositorio Git con configuraciones:**
```
config-repo/
├── application.yml          # Configuración compartida
├── identity-service.yml     # Config específica
├── catalog-service.yml
├── booking-service.yml
├── reviews-service.yml
└── search-service.yml
```

**Ejemplo application.yml:**
```yaml
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
  
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka
```

---

## 🚀 Despliegue

### Requisitos

- Java 17+
- Maven/Gradle
- PostgreSQL 14+ (con PostGIS para search-service)
- RabbitMQ 3.8+
- Docker (opcional)

### Orden de Inicio

```bash
# 1. Infraestructura
./start-infrastructure.sh
# - PostgreSQL (5 bases de datos)
# - RabbitMQ
# - Config Server
# - Eureka Server

# 2. Microservicios
./start-services.sh
# - API Gateway
# - Identity Service
# - Catalog Service
# - Booking Service
# - Reviews Service
# - Search Service
```

### Docker Compose (Simplificado)

```yaml
version: '3.8'
services:
  postgres-identity:
    image: postgres:14
    environment:
      POSTGRES_DB: nails_identity
    
  postgres-catalog:
    image: postgres:14
    environment:
      POSTGRES_DB: nails_catalog
  
  postgres-booking:
    image: postgres:14
    environment:
      POSTGRES_DB: nails_booking
  
  postgres-reviews:
    image: postgres:14
    environment:
      POSTGRES_DB: nails_reviews
  
  postgres-search:
    image: postgis/postgis:14-3.3
    environment:
      POSTGRES_DB: nails_search
  
  rabbitmq:
    image: rabbitmq:3-management
    ports:
      - "5672:5672"
      - "15672:15672"
  
  eureka-server:
    build: ./eureka-server
    ports:
      - "8761:8761"
  
  api-gateway:
    build: ./api-gateway
    ports:
      - "8080:8080"
    depends_on:
      - eureka-server
  
  identity-service:
    build: ./identity-service
    depends_on:
      - postgres-identity
      - rabbitmq
      - eureka-server
  
  catalog-service:
    build: ./catalog-service
    depends_on:
      - postgres-catalog
      - rabbitmq
      - eureka-server
  
  booking-service:
    build: ./booking-service
    depends_on:
      - postgres-booking
      - rabbitmq
      - eureka-server
  
  reviews-service:
    build: ./reviews-service
    depends_on:
      - postgres-reviews
      - rabbitmq
      - eureka-server
  
  search-service:
    build: ./search-service
    depends_on:
      - postgres-search
      - rabbitmq
      - eureka-server
```

---

## 📈 Ventajas de esta Arquitectura

1. **Escalabilidad independiente**: Cada servicio puede escalar según su carga
2. **Tecnología heterogénea**: Cada equipo puede elegir su stack (aunque aquí usamos Java uniformemente)
3. **Resiliencia**: Fallos aislados no afectan todo el sistema
4. **Despliegue independiente**: CI/CD separado para cada servicio
5. **Organización por dominio**: Cada equipo es responsable de su servicio

## ⚠️ Desventajas

1. **Complejidad operacional**: 7 servicios para desplegar y monitorear
2. **Latencia de red**: Llamadas HTTP entre servicios (3+ hops)
3. **Consistencia eventual**: Datos replicados entre servicios
4. **Debugging distribuido**: Stack traces fragmentados
5. **Costo de infraestructura**: Múltiples bases de datos, servidores

---

## 📚 Tecnologías Utilizadas

- **Java 17** + **Spring Boot 3.x**
- **Spring Cloud**: Gateway, Eureka, Config Server
- **Spring Data JPA** + **Hibernate**
- **PostgreSQL** + **PostGIS** (extensión geoespacial)
- **RabbitMQ** (mensajería)
- **Maven/Gradle** (build)
- **Docker** (containerización)
- **JWT** (autenticación)
- **Hibernate Envers** (auditoría)

---

## 🔗 Referencias

- [Spring Cloud Documentation](https://spring.io/projects/spring-cloud)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Microservices Patterns](https://microservices.io/patterns/)
- [PostGIS Documentation](https://postgis.net/documentation/)
