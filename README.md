# BankU Gateway Service

API Gateway for BankU microservices.

## Description

This service acts as a single entry point for all BankU microservices, providing:

- Request routing to the corresponding microservices
- Authentication and authorization via JWT
- Request filtering
- Monitoring and metrics

## Connected Microservices

- **User Service** (port 8081): User management and authentication
- **Engine Service** (port 8083): Financial intelligence analysis
- **OpenBanking Service** (port 8082): Integration with financial providers

## Configuration

The gateway is configured to run on port 8080 and redirect requests to the corresponding microservices:

- `/api/v1/users/**` -> User Service
- `/api/v1/intelligence/**` -> Engine Service
- `/api/v1/openbanking/**` -> OpenBanking Service

## Security

- Authentication endpoints (`/api/v1/users/auth/**` and `/api/v1/users/register`) are public
- All other endpoints require a valid JWT token in the `Authorization: Bearer <token>` header
- The JWT secret is configured in `application.yml`

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
