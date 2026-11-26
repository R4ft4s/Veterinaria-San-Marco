# Veterinaria San Marcos · Backend

API REST para la tienda Veterinaria San Marcos. Persiste productos, categorías, cuentas, roles y compras en MariaDB.

**Tecnologías:** Java 17 · Spring Boot 3.5 · Spring Security · JWT · Spring Data JPA · Flyway · MariaDB · Maven

## Requisitos

- JDK 17 o superior.
- Maven 3.9 o superior.
- MariaDB o MySQL (XAMPP sirve para desarrollo).

## Preparar la base de datos

1. Inicia MySQL/MariaDB en XAMPP.
2. Por defecto, Connector/J crea `veterinaria_san_marcos` al iniciar la API si el usuario de la base de datos tiene permiso para crear esquemas. Si prefieres crearla manualmente, ejecuta `sql/01_crear_base_de_datos.sql` desde phpMyAdmin.
3. Si tu instalación usa otro puerto o contraseña, cambia `spring.datasource.url` o `spring.datasource.password` en `src/main/resources/application.properties`. Conserva `createDatabaseIfNotExist=true` si quieres que Connector/J intente crear la base automáticamente.
4. Define `JWT_SECRET` con al menos 32 caracteres antes de iniciar. Flyway administra el esquema y la aplicación carga categorías, catálogo y cuentas demo al iniciar por primera vez.

## Ejecutar

Desde esta carpeta puedes usar el wrapper Maven, sin tener Maven en el `PATH`:

```powershell
$env:JWT_SECRET="clave-local-de-al-menos-32-caracteres"
.\mvnw.cmd spring-boot:run
```

También quedó instalado Maven 3.9.9 en `C:\Tools\apache-maven-3.9.9`. Reinicia IntelliJ si aún no reconoce `mvn`; en IntelliJ abre el panel **Maven**, selecciona el proyecto y pulsa **Reload All Maven Projects**. La configuración Maven de la terminal queda disponible después de abrir una nueva terminal.

La API queda en `http://localhost:8080`. Verifica el servicio en `/api/v1/health` y el catálogo en `/api/v1/productos`.

La documentación interactiva Swagger UI queda en `http://localhost:8080/swagger-ui/index.html`; el esquema OpenAPI JSON está en `http://localhost:8080/v3/api-docs`. La autenticación se realiza con cookies `HttpOnly`, no con tokens Bearer en el encabezado `Authorization`.

Flyway administra el esquema con migraciones en `src/main/resources/db/migration`. Una base de datos vacía recibe la migración inicial; una base existente creada por la versión anterior se marca como baseline 1 y conserva sus tablas y datos.

El frontend Vite usa normalmente `http://localhost:5173`. Para otros orígenes cambia `app.cors.allowed-origins` en `application.properties`. Para producción configura `COOKIE_SECURE=true`, un `JWT_SECRET` secreto de al menos 32 caracteres, una contraseña de base de datos segura y `app.seed.enabled=false`.

## Cuentas demo

| Rol | Correo | Contraseña |
|---|---|---|
| Administrador | admin@duoc.cl | Admin123 |
| Vendedor | vendedor@duoc.cl | Vendedor123 |
| Cliente | cliente@duoc.cl | Cliente123 |

Las contraseñas se guardan con BCrypt. El login responde con el usuario y establece cookies `HttpOnly` de access y refresh. Las peticiones protegidas incluyen esas cookies automáticamente; para cada operación mutable, primero solicita `/api/v1/auth/csrf` y envía el token de su respuesta en la cabecera `X-XSRF-TOKEN`.

## Rutas principales

| Método | Ruta | Acceso | Uso |
|---|---|---|---|
| GET | `/api/v1/auth/csrf` | Público | Entrega el token CSRF y establece su cookie |
| POST | `/api/v1/auth/login` | Público + CSRF | Inicia sesión y establece cookies HttpOnly; 3 intentos fallidos bloquean la cuenta |
| POST | `/api/v1/auth/refresh` | Refresh cookie + CSRF | Rota el refresh token y establece una nueva sesión |
| POST | `/api/v1/auth/logout` | Sesión + CSRF | Revoca el refresh token de la sesión y limpia sus cookies |
| POST | `/api/v1/auth/registro` | Público | Registra una cuenta cliente |
| GET | `/api/v1/auth/me` | Sesión | Devuelve la cuenta activa |
| PUT | `/api/v1/auth/perfil` | Sesión | Actualiza el perfil propio |
| GET | `/api/v1/productos?texto=&categoria=&precioMax=` | Público | Lista y filtra el catálogo |
| GET | `/api/v1/productos/{id}` | Público | Detalle de producto |
| POST / PUT / DELETE | `/api/v1/productos[/{id}]` | Administrador | Administra catálogo |
| PATCH | `/api/v1/productos/{id}/stock` | Administrador o vendedor | Actualiza stock con `{ "stock": 12 }` |
| GET | `/api/v1/categorias` | Público | Lista categorías como `{id,nombre}` |
| POST / PUT / DELETE | `/api/v1/categorias[/{id}]` | Administrador | Administra categorías; no elimina categorías con productos |
| GET | `/api/v1/usuarios` | Administrador | Lista cuentas sin exponer contraseñas |
| POST | `/api/v1/usuarios` | Administrador o vendedor | Crea cuentas; vendedor solo puede crear clientes |
| PUT / DELETE | `/api/v1/usuarios/{id}` | Administrador | Actualiza o desactiva cuentas |
| POST | `/api/v1/usuarios/{id}/desbloquear` | Administrador | Reinicia intentos fallidos |
| POST | `/api/v1/compras` | Sesión | Confirma compra y la asocia al usuario autenticado |
| GET | `/api/v1/compras/mias` | Sesión | Historial de la cuenta activa |
| GET | `/api/v1/compras` | Administrador | Historial completo |
| PATCH | `/api/v1/compras/{id}/estado?estado=` | Administrador | Actualiza el estado de una compra |
| GET | `/api/v1/reportes?periodo=semana\|mes\|ano` | Administrador | Entrega indicadores calculados desde las compras persistidas |

Al confirmar compra, el servidor calcula precios y descuenta stock dentro de una transacción con bloqueo de filas. El carrito sigue del lado del cliente y se envían únicamente IDs y cantidades. El pago es académico/simulado; no se almacenan datos de tarjeta. Para simular rechazo, el número de prueba puede terminar en `0002`.

## Ejemplo de autenticación

En PowerShell, conserva una misma sesión web para que las cookies se envíen automáticamente:

```powershell
$web = New-Object Microsoft.PowerShell.Commands.WebRequestSession
$csrf = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/csrf" -WebSession $web
$headers = @{ "X-XSRF-TOKEN" = $csrf.token }
Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/v1/auth/login" `
  -WebSession $web -Headers $headers -ContentType "application/json" `
  -Body '{"correo":"admin@duoc.cl","password":"Admin123"}'
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/usuarios" -WebSession $web
```

Los errores de negocio se responden como `{ "error": "mensaje", "campos": null }`. El frontend consume esta API versionada desde `src/services/api.ts`; configura `VITE_API_URL` con la URL base del backend.

## Pruebas

```powershell
mvn test
```

Las pruebas usan H2 en memoria y cubren salud, permisos por rol, cookies de autenticación, rotación de refresh, logout y protección CSRF.
