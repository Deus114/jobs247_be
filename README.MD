# Recruitment Management System

Dự án bắt đầu lại với **module Admin User** — đăng nhập quản trị.

Code phiên bản đầy đủ (user, role, company, ...) được lưu tại [`reference/full-recruitment-api/`](reference/full-recruitment-api/) để tham khảo.

## Module hiện tại: Admin Auth

| Endpoint | Mô tả |
|----------|--------|
| `POST /api/v1/admin/auth/login` | Đăng nhập admin |
| `POST /api/v1/admin/auth/refresh` | Làm mới access token |
| `POST /api/v1/admin/auth/logout` | Đăng xuất |
| `GET /api/v1/admin/auth/me` | Thông tin admin đang đăng nhập |

Swagger: http://localhost:{SERVER_PORT}/swagger-ui.html (mặc định port `8080`)

## Cấu trúc

```
src/main/java/com/recruitment/
├── config/          Security, JWT, OpenAPI
├── seeder/          AdminRbacSeeder (Super Admin + tài khoản mặc định)
├── controller/      AdminAuthController
├── service/         AdminAuthService
├── entity/          AdminUser, AdminRole, AdminPermission, RefreshToken
├── repository/      AdminUserRepository, AdminRoleRepository, AdminPermissionRepository, ...
├── dto/request|response/
├── mapper/          AdminUserMapper
├── exception/       GlobalExceptionHandler, ApiResponse
└── security/        CustomUserDetailsService
```

## Chạy dự án

1. Copy `.env.example` → `.env` và điền giá trị
2. Chạy:

```bash
set JAVA_HOME=C:\Program Files\Java\jdk-25.0.2
.\mvnw.cmd spring-boot:run
```

Cấu hình nhạy cảm (DB, JWT, ...) lấy từ file `.env` ở thư mục gốc project.

Tài khoản admin mặc định (khi `app.seed-admin=true`):

- Username: `admin`
- Password: `admin`
- Role: Super Admin (`fullAccess: true`)

## Kế thừa từ phiên bản cũ

- JWT (OAuth2 Resource Server)
- `ApiResponse` chuẩn hóa response
- `GlobalExceptionHandler`
- Refresh token (cookie + body)
- SpringDoc OpenAPI
