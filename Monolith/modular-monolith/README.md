# Orders Modular Monolith Backend

Modular Monolith backend application built with Java, Spring Boot, Gradle, and Lombok, designed according to the architecture specifications in `component.puml`, `container.puml`, `ADR.md`, `Modularity.md`, and `NFR.md`.

---

## Architecture and Module Structure

The application is decomposed into 4 domain modules. Each module strictly isolates its public API contract (`*.api.*`) from internal infrastructure and business logic implementations (`*.infra.*`):

```
src/main/java/com/monolith/
├── MonolithApplication.java                     # Spring Boot entry point (@EnableScheduling, @EnableAsync)
├── common/                                      # Cross-cutting error models and exceptions
│   ├── dto/ErrorResponse.java
│   └── exception/GlobalExceptionHandler.java
│
├── orders/                                      # Orders domain module
│   ├── api/                                     # Public API contract
│   │   ├── OrderService.java                    # Public service interface
│   │   └── dto/                                 # Public DTOs (CreateOrderRequest, OrderResponse, ...)
│   └── infra/                                   # Internal infrastructure (hidden from other modules)
│       ├── OrderController.java                 # REST controller with pre-return latency tracking
│       ├── OrderServiceImpl.java                # Business logic (depends only on *.api.* of other modules)
│       ├── OrderRepository.java                 # JPA repository
│       ├── OrderEntity.java                     # Order JPA entity
│       └── OrderItemEntity.java                 # Order item JPA entity
│
├── payments/                                    # Payments domain module
│   ├── api/                                     # Public API contract
│   │   ├── PaymentService.java                  # Public service interface
│   │   └── dto/                                 # ProcessPaymentRequest, PaymentResponse, ...
│   └── infra/                                   # Internal infrastructure
│       ├── PaymentServiceImpl.java              # Payment business logic implementation
│       ├── PaymentClient.java                   # HTTP client for External Payment Provider
│       ├── PaymentRepository.java               # JPA repository
│       └── PaymentEntity.java                   # Payment JPA entity
│
├── users/                                       # Users domain module
│   ├── api/                                     # Public API contract
│   │   ├── UserService.java                     # Public service interface
│   │   └── dto/                                 # CreateUserRequest, UserResponse, ...
│   └── infra/                                   # Internal infrastructure
│       ├── UserController.java                  # REST controller for users
│       ├── UserServiceImpl.java                 # User business logic implementation
│       ├── UserRepository.java                  # JPA repository
│       └── UserEntity.java                      # User JPA entity
│
└── notifications/                               # Notifications domain module
    ├── api/                                     # Public API contract
    │   ├── NotificationService.java             # Public service interface
    │   └── dto/                                 # SendNotificationRequest, NotificationResponse, ...
    └── infra/                                   # Internal infrastructure
        ├── NotificationServiceImpl.java         # Notification business logic implementation
        ├── NotificationRepository.java          # JPA repository
        ├── NotificationWorker.java              # Background worker for dispatching pending notifications
        └── NotificationEntity.java              # Notification JPA entity
```

---

## Non-Functional Requirements (NFR) Implementation

- **Latency SLO (p95 < 0.8s, p99 < 1.3s)**:
  `OrderController` explicitly benchmarks request processing duration immediately before the `return` statement. Execution time is published to Micrometer under `orders.creation.latency` (with percentiles p50, p95, p99) and returned to the client in the `X-Response-Time-Millis` HTTP response header.
- **External Integrations**:
  - `PaymentClient` executes HTTP calls to the payment gateway with simulated network latency and failure handling.
  - `NotificationWorker` periodically polls and handles pending notifications through `NotificationService`.
- **Database Support**:
  Configured for PostgreSQL in production environments with an automatic in-memory H2 fallback for zero-dependency local testing and development.
- **Observability and Monitoring**:
  Spring Boot Actuator endpoints are exposed for health, metrics, and Prometheus scraping (`/actuator/health`, `/actuator/metrics`, `/actuator/prometheus`).

---

## Architecture Verification (ArchUnit)

Automated architectural fitness functions are implemented in `ModularityArchTest`:
- **API-Only Cross-Module Access**: Dependencies between modules are restricted exclusively to `*.api.*` packages. Accessing another module's `*.infra.*` (via imports, fields, method signatures, or calls) fails the test build.
- **Cycle-Free Modules**: Verifies that modules (`orders`, `payments`, `users`, `notifications`) have no circular dependency loops.

Run architecture tests:
```bash
./gradlew test --tests com.monolith.modularity.ModularityArchTest
```

---

## Build and Run Instructions

### Prerequisites
- JDK 21+

### Run Tests
```bash
./gradlew test
```

### Build Executable JAR
```bash
./gradlew bootJar
```

### Run the Application
```bash
./gradlew bootRun
```
The service will start on port `8080`.
