markdown
# Acme Insurance Order Service

This project is a Spring Boot microservice for managing insurance policy orders, including creation, retrieval, and cancellation of policy requests.

## Features

- Create, retrieve, and cancel insurance policy requests
- Reactive programming with Project Reactor (`Mono`, `Flux`)
- Clean architecture with use cases and mappers
- Enum-based status management
- Unit tests with JUnit 5 and Mockito

## Project Structure

- `com.acmeinsurance.order.application.service`  
  Application services and facades (e.g., `PolicyRequestFacade`)
- `com.acmeinsurance.order.application.dto`  
  Data transfer objects for requests and responses
- `com.acmeinsurance.order.application.mapper`  
  Mappers between DTOs and domain models
- `com.acmeinsurance.order.domain.model`  
  Domain entities (e.g., `PolicyRequest`)
- `com.acmeinsurance.order.domain.usecase`  
  Business use cases (e.g., create, get, cancel policy requests)
- `com.acmeinsurance.order.enums`  
  Enum types (e.g., `StatusEnum`)
- `com.acmeinsurance.order.utils`  
  Utility classes (e.g., `ValidateUtils`)

## Build & Run

This project uses Maven.

```bash
mvn clean install
mvn spring-boot:run