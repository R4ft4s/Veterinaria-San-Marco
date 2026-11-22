# Veterinaria San Marcos · Backend

API REST independiente para la tienda Veterinaria San Marcos. Reemplaza la persistencia local del navegador por una base de datos MariaDB y mantiene los productos, categorías, cuentas, roles y compras del proyecto frontend.

**Tecnologías:** Java 17 · Spring Boot 3.5 · Spring Security · JWT · Spring Data JPA · MariaDB · Maven

## Requisitos

- JDK 17 o superior.
- Maven 3.9 o superior.
- MariaDB o MySQL (XAMPP sirve para desarrollo).

## Preparar la base de datos

1. Inicia MySQL/MariaDB en XAMPP.
2. Por defecto, Connector/J crea `veterinaria_san_marcos` al iniciar la API si el usuario de la base de datos tiene permiso para crear esquemas. Si prefieres crearla manualmente, ejecuta `sql/01_crear_base_de_datos.sql` desde phpMyAdmin.
3. Si tu instalación usa otro puerto o contraseña, cambia `spring.datasource.url` o `spring.datasource.password` en `src/main/resources/application.properties`. Conserva `createDatabaseIfNotExist=true` si quieres que Connector/J intente crear la base automáticamente.
4. Define `JWT_SECRET` con al menos 32 caracteres antes de iniciar. Hibernate crea las tablas y carga categorías, catálogo y cuentas demo al iniciar por primera vez.

## Ejecutar

Desde esta carpeta puedes usar el wrapper Maven, sin tener Maven en el `PATH`:

```powershell
$env:JWT_SECRET="clave-local-de-al-menos-32-caracteres"
.\mvnw.cmd spring-boot:run
```

También quedó instalado Maven 3.9.9 en `C:\Tools\apache-maven-3.9.9`. Reinicia IntelliJ si aún no reconoce `mvn`; en IntelliJ abre el panel **Maven**, selecciona el proyecto y pulsa **Reload All Maven Projects**. La configuración Maven de la terminal queda disponible después de abrir una nueva terminal.

La API queda en `http://localhost:8080`. Verifica el servicio en `/api/health` y el catálogo en `/api/productos`.

La documentación interactiva Swagger UI queda en `http://localhost:8080/swagger-ui/index.html`; el esquema OpenAPI JSON está en `http://localhost:8080/v3/api-docs`. En Swagger, usa **Authorize** y pega el JWT recibido desde `/api/auth/login` para probar rutas protegidas.

El frontend Vite usa normalmente `http://localhost:5173`. Para otros orígenes cambia `app.cors.allowed-origins` en `application.properties`. Para producción configura un `JWT_SECRET` secreto de al menos 32 caracteres, una contraseña de base de datos segura y `app.seed.enabled=false`.

## Cuentas demo

| Rol | Correo | Contraseña |
|---|---|---|
| Administrador | admin@duoc.cl | Admin123 |
| Vendedor | vendedor@duoc.cl | Vendedor123 |
| Cliente | cliente@duoc.cl | Cliente123 |

Las contraseñas se guardan con BCrypt. En las peticiones protegidas usa `Authorization: Bearer <token>` con el token devuelto por login.

## Rutas principales

| Método | Ruta | Acceso | Uso |
|---|---|---|---|
| POST | `/api/auth/login` | Público | Inicia sesión y entrega JWT; 3 intentos fallidos bloquean la cuenta |
| POST | `/api/auth/refresh` | Público | Rota un refresh token válido y entrega una nueva sesión |
| POST | `/api/auth/logout` | Sesión | Revoca los refresh tokens del usuario |
| POST | `/api/auth/registro` | Público | Registra una cuenta cliente |
| GET | `/api/auth/me` | Sesión | Devuelve la cuenta activa |
| PUT | `/api/auth/perfil` | Sesión | Actualiza el perfil propio |
| GET | `/api/productos?texto=&categoria=&precioMax=` | Público | Lista y filtra el catálogo |
| GET | `/api/productos/{id}` | Público | Detalle de producto |
| POST / PUT / DELETE | `/api/productos[/{id}]` | Administrador | Administra catálogo |
| PATCH | `/api/productos/{id}/stock` | Administrador o vendedor | Actualiza stock con `{ "stock": 12 }` |
| GET | `/api/categorias` | Público | Lista categorías como `{id,nombre}` |
| POST / PUT / DELETE | `/api/categorias[/{id}]` | Administrador | Administra categorías; no elimina categorías con productos |
| GET | `/api/usuarios` | Administrador | Lista cuentas sin exponer contraseñas |
| POST | `/api/usuarios` | Administrador o vendedor | Crea cuentas; vendedor solo puede crear clientes |
| PUT / DELETE | `/api/usuarios/{id}` | Administrador | Actualiza o elimina cuentas |
| POST | `/api/usuarios/{id}/desbloquear` | Administrador | Reinicia intentos fallidos |
| POST | `/api/compras` | Sesión | Confirma compra y la asocia al usuario autenticado |
| GET | `/api/compras/mias` | Sesión | Historial de la cuenta activa |
| GET | `/api/compras` | Administrador | Historial completo |
| PATCH | `/api/compras/{id}/estado?estado=` | Administrador | Actualiza el estado de una compra |
| GET | `/api/reportes?periodo=semana\|mes\|ano` | Administrador | Entrega indicadores calculados desde las compras persistidas |

Al confirmar compra, el servidor calcula precios y descuenta stock dentro de una transacción con bloqueo de filas. El carrito sigue del lado del cliente y se envían únicamente IDs y cantidades. El pago es académico/simulado; no se almacenan datos de tarjeta. Para simular rechazo, el número de prueba puede terminar en `0002`.

## Ejemplos

```sh
curl -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" -d '{"correo":"admin@duoc.cl","password":"Admin123"}'
curl http://localhost:8080/api/productos
curl -X POST http://localhost:8080/api/compras -H "Content-Type: application/json" -d '{"items":[{"productoId":"ME004","cantidad":1}],"tipoEntrega":"retiro","metodoPago":"transferencia"}'
```

Los errores de negocio se responden como `{ "error": "mensaje", "campos": null }`. El frontend aún debe conectarse a esta API y adaptar sus llamadas desde `localStorage`; este backend se creó como proyecto separado y no cambia la aplicación React actual.

## Pruebas

```powershell
mvn test
```

Las pruebas usan H2 en memoria y cubren servicio de salud, rechazo sin autenticación, login correcto e incorrecto.
