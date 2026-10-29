# MS-01 Usuarios y Alojamientos

Backend Spring Boot del microservicio de identidad y alojamientos de Aloja-Te.

## Requisitos

- Java 21+
- Maven 3.9+
- PostgreSQL

## Configuración local

Copia o crea `src/main/resources/application-local.yml` (no se versiona) con correo, Cloudinary y mapas.

## Arranque

```bash
mvn spring-boot:run
```

- API: `http://localhost:8081`
- Swagger: `http://localhost:8081/swagger-ui.html`
