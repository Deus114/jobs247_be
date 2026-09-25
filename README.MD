# Recruitment

# Deploy

http://3.1.196.187/swagger-ui/index.html

- Java 25
- Spring Boot 4
- Spring Security + JWT (OAuth2 Resource Server)
- Spring Data JPA
- MySQL
- RabbitMQ
- Redis
- Maven
- SpringDoc OpenAPI (Swagger)
- Docker Compose

## Môi trường

- JDK 25
- MySQL 8+
- Maven Wrapper (`mvnw`)

### Cấu hình

1. Copy `.env.example` → `.env`
2. Điền các biến môi trường trong `.env`

### Chạy app bằng Maven

Cần MySQL và, khi `MAIL_QUEUE_ENABLED=true`, RabbitMQ đang lắng nghe `localhost`.

```bash
.\mvnw.cmd spring-boot:run
```

### Docker local

`docker-compose.yml`: MySQL (cổng host `3307`), RabbitMQ (`5672`, Management UI `15672`), backend (`8080`).

```bash
docker compose up -d --build
```

RabbitMQ Management: http://localhost:15672 (`guest` / `guest`). App: http://localhost:8080.

### Docker prod

`docker-compose.prod.yml`: MySQL, Redis, RabbitMQ (không UI), backend lấy image `${DOCKERHUB_USERNAME}/jobs247-backend:latest`. Trên EC2 cần file `.env` tại `/opt/jobs247` và biến `DOCKERHUB_USERNAME`.

```bash
docker compose -f docker-compose.prod.yml pull
docker compose -f docker-compose.prod.yml up -d
```
