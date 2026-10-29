# MS-01 Usuarios y Alojamientos

Microservicio backend de **Aloja-Te** encargado de:

- Identidad: registro, verificación de correo, sesión (JWT), roles, permisos y restricciones
- Alojamientos: publicación de ofertas (casa/departamento) y establecimientos (hotel/hostal) con tipos de habitación
- Moderación administrativa y catálogo de lo publicado

Puerto por defecto: **8081**  
API docs: [http://localhost:8081/swagger-ui.html](http://localhost:8081/swagger-ui.html)

---

## Qué puede hacer un anfitrión

Un usuario **público** (anfitrión) puede publicar de dos maneras:

### Casa o departamento (oferta única)

1. Registra la propiedad como **una sola unidad** (casa completa o departamento).
2. Completa ubicación, servicios, capacidad, precio, políticas y fotos.
3. Finaliza el registro → el anuncio queda en **EN_REVISION**.
4. Un administrador lo **acepta** → pasa a **PUBLICADO** y aparece en el catálogo.
5. Quien reserva, reserva **ese alojamiento entero**.

### Hotel o hostal (establecimiento + habitaciones)

1. Registra el **establecimiento** (datos generales, políticas, fotos del hotel).
2. Finaliza el registro → el hotel queda **ACTIVO** (aún sin habitaciones visibles en catálogo).
3. Agrega **tipos de habitación**, cada uno con su inventario. Ejemplo:
   - Habitación estándar → 10 unidades  
   - Habitación doble → 12 unidades  
4. Cada tipo se envía a revisión y, al aceptarse, queda **PUBLICADO**.
5. Los huéspedes pueden **reservar distintos tipos de habitación** del mismo hotel (según disponibilidad).

> Las reservas de calendario/pago pueden vivir en otros microservicios; MS-01 define usuarios, publicación y el inventario de unidades.

---

## Arquitectura

Estilo **hexagonal / ports & adapters**, con reglas de negocio en dominio (DDD ligero). Hay dos bounded contexts en el mismo jar:

| Contexto | Paquete | Responsabilidad |
|----------|---------|-----------------|
| Identidad | `com.alojate.identidad` | Usuarios, roles, permisos, sesión, restricciones |
| Alojamientos | `com.alojate.alojamientos` | Registro, unidades, fotos, moderación, catálogo |

Dentro de cada contexto:

```text
domain/           → agregados, value objects, políticas, repositorios (interfaces)
application/      → casos de uso (services), DTOs, ports de entrada/salida
infrastructure/   → REST, JPA/PostgreSQL, correo, Cloudinary, JWT/sesión
```

Flujo típico de una petición:

```text
Controller (REST)
  → Application Service (caso de uso)
    → Dominio (reglas / agregado)
      → Puerto de salida (repositorio, almacén de imágenes, mail…)
        → Adapter (JPA, Cloudinary, SMTP…)
```

Configuración transversal: `com.alojate.config` (OpenAPI, filtro de sesión, permisos).

Las migraciones de base de datos van con **Flyway** en `src/main/resources/db/migration/`. Hibernate usa `ddl-auto: validate` (no crea tablas solo; Flyway es la fuente de verdad del esquema).

---

## Stack y dependencias

| Tecnología | Uso |
|------------|-----|
| Java 21 | Lenguaje |
| Spring Boot 3.5 | Framework |
| spring-boot-starter-web | API REST |
| spring-boot-starter-data-jpa | Persistencia |
| PostgreSQL + driver | Base de datos |
| Flyway | Migraciones |
| spring-security-crypto | Hash de contraseñas (BCrypt) |
| spring-boot-starter-mail | Correo (verificación) |
| springdoc-openapi | Swagger UI |
| ArchUnit (test) | Reglas de arquitectura |
| Cloudinary (HTTP) | Almacenamiento de imágenes (adapter propio) |

Build: **Maven** (`pom.xml`).

---

## Requisitos para ejecutarlo

1. **JDK 21+**
2. **Maven 3.9+**
3. **PostgreSQL** con una base vacía (por defecto `alojate_usuarios`)
4. (Opcional pero recomendado) **Cloudinary** si vas a subir fotos
5. (Opcional) **SMTP** (Gmail u otro) si quieres verificación de correo real

Crear la base (ejemplo):

```sql
CREATE DATABASE alojate_usuarios;
```

---

## Configuración

El perfil por defecto es `local`. La configuración base está en `src/main/resources/application.yml`.

Los secretos **no se versionan**. Crea:

`src/main/resources/application-local.yml`

Ejemplo (rellena con tus valores):

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/alojate_usuarios
    username: postgres
    password: tu_password
  mail:
    host: smtp.gmail.com
    port: 587
    username: "tu-correo@gmail.com"
    password: "tu-app-password"
    properties:
      mail.smtp.auth: true
      mail.smtp.starttls.enable: true
      mail.smtp.starttls.required: true

alojate:
  mail:
    enabled: true          # false si no quieres enviar correos
    from: "tu-correo@gmail.com"
  imagenes:
    cloud-name: "tu_cloud_name"
    api-key: "tu_api_key"
    api-secret: "tu_api_secret"
  mapas:
    api-key: "tu_google_maps_key"   # opcional para geocoding en registro
```

También puedes usar variables de entorno (`MS01_DB_URL`, `MS01_DB_USERNAME`, `MS01_DB_PASSWORD`, `CLOUDINARY_*`, `MS01_MAIL_*`, etc.) en lugar del YAML local.

Sin Cloudinary, el alta de fotos fallará al subir archivos. Sin mail (`enabled: false`), el registro público sigue funcionando, pero el código de verificación no llegará por correo (revisa logs / estrategia de pruebas).

---

## Arranque

```bash
git clone https://github.com/quantworld95/MS01-Usuarios-Alojamientos.git
cd MS01-Usuarios-Alojamientos

# Crea application-local.yml (ver arriba)

mvn spring-boot:run
```

Al subir, Flyway aplica las migraciones automáticamente.

- API: `http://localhost:8081`
- Swagger: `http://localhost:8081/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8081/v3/api-docs`

### Tests

```bash
mvn test
```

---

## Cuenta admin inicial (seed)

Tras las migraciones existe un superadmin de desarrollo:

| Campo | Valor |
|-------|--------|
| Email | `superadmin@alojate.bo` |
| Contraseña | `secreta123` |

Úsala para moderar alojamientos, gestionar roles/trabajadores, etc. **Cámbiala en entornos reales.**

---

## Flujos rápidos (Swagger)

1. **Authorize**  
   - Público: `POST /usuarios` → `POST /usuarios/verificacion` → `POST /sesiones` → Authorize con el JWT.  
   - Admin: `POST /sesiones` con `superadmin@alojate.bo` → Authorize.

2. **Publicar casa/departamento**  
   Tag *Registro de alojamiento*: borrador → presentación → ubicación → servicios → oferta-unica → fotos → `finalizar-registro` (EN_REVISION).  
   Tag *Moderación*: `aceptar` → PUBLICADO.

3. **Publicar hotel**  
   Mismo tag: borrador → presentación → ubicación → servicios → políticas → fotos → `finalizar-registro` (ACTIVO) → `POST .../unidades` por cada tipo de habitación → moderar unidades.

4. **Ver publicados**  
   Tag *Catálogo público*: `GET /catalogo/alojamientos`.

El detalle paso a paso de cada endpoint está documentado en la descripción de Swagger y en los tags de cada controlador.

---

## Estructura del repositorio

```text
pom.xml
README.md
src/main/java/com/alojate/
  config/                 # App, OpenAPI, filtro de sesión
  identidad/              # Usuarios y acceso
  alojamientos/           # Publicación y catálogo
src/main/resources/
  application.yml
  application-local.yml   # NO versionado (.gitignore)
  db/migration/           # Flyway (V1…Vn)
src/test/java/            # Unitarios, HTTP y ArchUnit
```

---

## Notas

- Autenticación: header `Authorization: Bearer <token>` (en Swagger, pega solo el token).
- Fotos: multipart hacia Cloudinary; tamaño máximo ~8 MB.
- Este repo es **solo el backend MS-01**; el frontend vive en otro proyecto.
