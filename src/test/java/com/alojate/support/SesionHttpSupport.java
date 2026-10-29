package com.alojate.support;

import com.alojate.identidad.domain.usuario.repository.UsuarioRepository;
import com.alojate.identidad.domain.usuario.valueobject.Email;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public final class SesionHttpSupport {

    public static final String SUPERADMIN_EMAIL = "superadmin@alojate.bo";
    public static final String SUPERADMIN_PASSWORD = "secreta123";

    private SesionHttpSupport() {
    }

    public static Actor sesionSuperadmin(MockMvc mvc, ObjectMapper json) throws Exception {
        return sesion(mvc, json, SUPERADMIN_EMAIL, SUPERADMIN_PASSWORD);
    }

    public static int idRol(MockMvc mvc, ObjectMapper json, Actor admin, String nombre) throws Exception {
        JsonNode roles = json.readTree(mvc.perform(get("/roles")
                        .header(HttpHeaders.AUTHORIZATION, admin.bearer()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());
        for (JsonNode rol : roles) {
            if (nombre.equals(rol.get("nombre").asText())) {
                return rol.get("id").asInt();
            }
        }
        throw new IllegalStateException("No está el rol " + nombre);
    }

    public record Actor(UUID id, String email, String token) {
        public String bearer() {
            return "Bearer " + token;
        }
    }

    public static Actor registrarPublicoYSesion(MockMvc mvc, UsuarioRepository usuarios, ObjectMapper json)
            throws Exception {
        String email = "host." + UUID.randomUUID() + "@ejemplo.com";
        mvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Ana","apellido":"Perez","email":"%s","telefono":"70000000","contrasena":"secreta123"}
                                """.formatted(email)))
                .andExpect(status().isCreated());
        String codigo = usuarios.buscarPorCorreoPublico(new Email(email)).orElseThrow().codigoVerificacion().codigo();
        mvc.perform(post("/usuarios/verificacion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\",\"codigo\":\"%s\"}".formatted(email, codigo)))
                .andExpect(status().isOk());
        return sesion(mvc, json, email, "secreta123");
    }

    public static Actor sesion(MockMvc mvc, ObjectMapper json, String email, String contrasena) throws Exception {
        MvcResult resultado = mvc.perform(post("/sesiones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\",\"contrasena\":\"%s\"}".formatted(email, contrasena)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode cuerpo = json.readTree(resultado.getResponse().getContentAsString());
        return new Actor(
                UUID.fromString(cuerpo.get("usuarioId").asText()),
                email,
                cuerpo.get("token").asText());
    }

    public static int tipoPropiedad(MockMvc mvc, ObjectMapper json, Actor actor, String nombre) throws Exception {
        MvcResult catalogos = mvc.perform(get("/catalogos-alojamiento")
                        .header(HttpHeaders.AUTHORIZATION, actor.bearer()))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode tipos = json.readTree(catalogos.getResponse().getContentAsString()).get("tiposPropiedad");
        for (JsonNode tipo : tipos) {
            if (nombre.equals(tipo.get("nombre").asText())) {
                return tipo.get("id").asInt();
            }
        }
        throw new IllegalStateException("Falta el tipo " + nombre);
    }
}
