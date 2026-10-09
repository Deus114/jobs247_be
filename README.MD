# Recruitment

# Deploy

https://jobs247-demo.duckdns.org/swagger-ui/index.html

- Java 25
- Spring Boot 4
- Spring Security + JWT (OAuth2 Resource Server)
- Spring Data JPA
- Spring Mail
- Spring Cache
- MySQL
- RabbitMQ
- Redis
- Amazon S3
- Firebase Cloud Messaging
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

Cần MySQL, Redis và, khi `MAIL_QUEUE_ENABLED=true`, RabbitMQ đang lắng nghe `localhost`. Push chỉ gửi khi `FCM_ENABLED=true`.

```bash
.\mvnw.cmd spring-boot:run
```

### Docker local

`docker-compose.yml`: MySQL (cổng host `3307`), Redis (`6379`), RabbitMQ (`5672`, Management UI `15672`), backend (`8080`).

```bash
docker compose up -d --build
```

RabbitMQ Management: http://localhost:15672 (`guest` / `guest`). App: http://localhost:8080.

```bash
docker compose -f docker-compose.prod.yml pull
docker compose -f docker-compose.prod.yml up -d
```
