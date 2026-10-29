package com.alojate.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String BEARER = "bearer-jwt";

    private static final String DESCRIPCION = """
            API de identidad y alojamientos de Aloja-Te.

            ## 1. Usuarios y sesión

            ### Cuenta pública (anfitrión / huésped)
            1. `POST /usuarios` → registro (queda pendiente de verificación)
            2. `POST /usuarios/verificacion` `{ email, codigo }` → activa la cuenta
               (si expiró: `POST /usuarios/verificacion/reenvio`)
            3. `POST /sesiones` `{ email, contrasena }` → JWT
            4. **Authorize** (pegar solo el token, sin la palabra Bearer)

            ### Administración de identidad (token corporativo con permisos)
            - Trabajadores: `/usuarios/corporativos` (permiso ADMINISTRAR_TRABAJADORES)
            - Usuarios públicos / restricciones: `/usuarios/publicos` (ADMINISTRAR_USUARIOS_PUBLICOS)
            - Roles y módulos: `/roles`, `/modulos` (ADMINISTRAR_ROLES)

            ## 2. Cómo registrar un alojamiento (wizard)

            El registro **no es un solo POST**. Crea un borrador y completa los pasos en orden.
            Autenticación: Authorize con el JWT de `POST /sesiones`.

            ### 0. Preparación
            1. `POST /sesiones` → obtener token
            2. Authorize (Bearer JWT)
            3. `GET /catalogos-alojamiento` → ids de tipo, espacio y servicios

            ### A) Casa / Departamento (oferta única)
            1. `POST /alojamientos` `{ "tipoPropiedadId": N }` → crea BORRADOR (guarda el `id`)
            2. `PUT /alojamientos/{id}/presentacion`
            3. `PUT /alojamientos/{id}/ubicacion`
            4. `PUT /alojamientos/{id}/servicios`
            5. `PUT /alojamientos/{id}/oferta-unica` (capacidad, camas, precio, políticas, horarios)
            6. `POST /alojamientos/{id}/fotografias/archivo` (multipart, al menos una)
            7. `POST /alojamientos/{id}/finalizar-registro` → estado **EN_REVISION**

            ### B) Hotel / Hostal (establecimiento)
            1. `POST /alojamientos` `{ "tipoPropiedadId": N }` → BORRADOR
            2. `PUT /alojamientos/{id}/presentacion`
            3. `PUT /alojamientos/{id}/ubicacion`
            4. `PUT /alojamientos/{id}/servicios`
            5. `PUT /alojamientos/{id}/politicas`
            6. `POST /alojamientos/{id}/fotografias/archivo`
            7. `POST /alojamientos/{id}/finalizar-registro` → estado **ACTIVO**
            8. Por cada habitación: `POST /alojamientos/{id}/unidades` → unidad en **EN_REVISION**

            Ver el tag **Registro de alojamiento** para el detalle de cada operación.

            ## 3. Moderación admin (después del registro)

            Login con usuario corporativo que pueda moderar. Tag **Moderación de alojamientos**.

            - Casa/Depto en EN_REVISION: `POST /admin/alojamientos/{id}/aceptar` → PUBLICADO \
              o `.../rechazar` (body `{ "motivo": "..." }`)
            - Habitación de hotel en EN_REVISION: `POST /admin/alojamientos/{id}/unidades/{unidadId}/aceptar`
            - Publicados: `suspender` / `levantar-suspension` (alojamiento o unidad)

            ## 4. Catálogo público

            `GET /catalogo/alojamientos` solo muestra ofertas/habitaciones **PUBLICADO**.
            """;

    @Bean
    OpenAPI ms01OpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("MS-01 Usuarios y Alojamientos")
                        .description(DESCRIPCION)
                        .version("0.0.1"))
                .components(new Components().addSecuritySchemes(BEARER, new SecurityScheme()
                        .name(BEARER)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("Pega solo el token de POST /sesiones (sin la palabra Bearer)")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER));
    }
}
