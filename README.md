# BankU Gateway Service

API Gateway for BankU microservices.

## Description

This service acts as a single entry point for all BankU microservices, providing:

- Request routing to the corresponding microservices
- Authentication and authorization via JWT
- Request filtering
- Monitoring and metrics
- Rate limiting
- Circuit breaking
- Request/Response transformation

## Connected Microservices

- **User Service** (port 8081): User management and authentication
- **Engine Service** (port 8083): Financial intelligence analysis
- **OpenBanking Service** (port 8082): Integration with financial providers

## Configuration

The gateway is configured to run on port 8080 and redirect requests to the corresponding microservices:

- `/api/v1/users/**` -> User Service
- `/api/v1/intelligence/**` -> Engine Service
- `/api/v1/openbanking/**` -> OpenBanking Service

### Rate Limiting

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: http://localhost:8081
          predicates:
            - Path=/api/v1/users/**
          filters:
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 10
                redis-rate-limiter.burstCapacity: 20
```

### Circuit Breaking

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: http://localhost:8081
          predicates:
            - Path=/api/v1/users/**
          filters:
            - name: CircuitBreaker
              args:
                name: userService
                fallbackUri: forward:/fallback
```

## Security

- Authentication endpoints (`/api/v1/users/auth/**` and `/api/v1/users/register`) are public
- All other endpoints require a valid JWT token in the `Authorization: Bearer <token>` header
- The JWT secret is configured in `application.yml`
- CORS configuration for cross-origin requests
- Rate limiting to prevent abuse
- Request validation and sanitization

## Execution

### Locally

```bash
./mvnw spring-boot:run
```

### With Docker

```bash
docker-compose up -d
```

## Actuator Endpoints

- `/actuator/health`: Service status
- `/actuator/info`: Service information
- `/actuator/gateway`: Gateway routes information
- `/actuator/metrics`: Performance metrics
- `/actuator/circuitbreakers`: Circuit breaker status

## Monitoring

The gateway service provides monitoring capabilities through:

- Spring Boot Actuator
- Prometheus metrics
- Grafana dashboards
- Distributed tracing with Spring Cloud Sleuth

## Development

### Requirements

- Java 17
- Docker
- Docker Compose

### Local Setup

1. Clone the repository
2. Configure the microservices URLs in `application.yml`
3. Run `docker-compose up -d` to start required services
4. Run the application with `./mvnw spring-boot:run`

## License

This project is private and confidential.
