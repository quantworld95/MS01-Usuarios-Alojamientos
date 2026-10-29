package com.alojate.identidad.infrastructure.adapters.input.rest;

import com.alojate.config.Ms01UsuariosAlojamientosApplication;
import com.alojate.identidad.domain.rol.repository.RolRepository;
import com.alojate.identidad.domain.rol.valueobject.NombreRol;
import com.alojate.identidad.domain.usuario.repository.UsuarioRepository;
import com.alojate.identidad.domain.usuario.valueobject.Email;
import com.alojate.support.SesionHttpSupport;
import com.alojate.support.SesionHttpSupport.Actor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = Ms01UsuariosAlojamientosApplication.class)
@AutoConfigureMockMvc
@Transactional
class GestionUsuarioPublicoHttpTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UsuarioRepository usuarios;

    @Autowired
    private RolRepository roles;

    private final ObjectMapper json = new ObjectMapper();

    @Test
    void suspendeActivaYDesactivaLaCuenta() throws Exception {
        Actor admin = crearTrabajadorConSesion();
        String email = registrarYVerificar();
        String id = idPublico(email);

        mvc.perform(patch("/usuarios/publicos/" + id + "/estado")
                        .header(HttpHeaders.AUTHORIZATION, admin.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\":\"SUSPENDIDA\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("SUSPENDIDA"));
        sesion(email).andExpect(status().isForbidden()).andExpect(jsonPath("$.codigo").value("CUENTA_SUSPENDIDA"));

        mvc.perform(patch("/usuarios/publicos/" + id + "/estado")
                        .header(HttpHeaders.AUTHORIZATION, admin.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\":\"ACTIVA\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ACTIVA"));

        mvc.perform(patch("/usuarios/publicos/" + id + "/estado")
                        .header(HttpHeaders.AUTHORIZATION, admin.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\":\"DESACTIVADA\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("DESACTIVADA"));
        sesion(email).andExpect(status().isForbidden()).andExpect(jsonPath("$.codigo").value("CUENTA_DESACTIVADA"));
    }

    @Test
    void unaRestriccionQuitaLaFuncionSoloAEsaPersona() throws Exception {
        Actor admin = crearTrabajadorConSesion();
        String email = registrarYVerificar();
        String id = idPublico(email);
        int permiso = idPermiso("BUSCAR_ALOJAMIENTOS");
        int antes = permisosDelRolPublico();

        String alta = mvc.perform(post("/usuarios/publicos/" + id + "/restricciones")
                        .header(HttpHeaders.AUTHORIZATION, admin.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"idPermiso":%d,"motivo":"No puede buscar"}
                                """.formatted(permiso)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.activa").value(true))
                .andReturn().getResponse().getContentAsString();
        String restriccionId = json.readTree(alta).get("id").asText();

        sesion(email)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.permisos", not(hasItem("BUSCAR_ALOJAMIENTOS"))))
                .andExpect(jsonPath("$.permisos", hasItem("PUBLICAR_ALOJAMIENTO")));
        assertEquals(antes, permisosDelRolPublico());

        mvc.perform(post("/usuarios/publicos/" + id + "/restricciones/" + restriccionId + "/levantamiento")
                        .header(HttpHeaders.AUTHORIZATION, admin.bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activa").value(false));

        sesion(email)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.permisos", hasItem("BUSCAR_ALOJAMIENTOS")));
    }

    private org.springframework.test.web.servlet.ResultActions sesion(String email) throws Exception {
        return mvc.perform(post("/sesiones")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"%s\",\"contrasena\":\"secreta123\"}".formatted(email)));
    }

    private String registrarYVerificar() throws Exception {
        String email = "publica." + UUID.randomUUID() + "@ejemplo.com";
        mvc.perform(post("/usuarios").contentType(MediaType.APPLICATION_JSON).content("""
                {"nombre":"Ana","apellido":"Perez","email":"%s","telefono":"70000000","contrasena":"secreta123"}
                """.formatted(email))).andExpect(status().isCreated());
        String codigo = usuarios.buscarPorCorreoPublico(new Email(email)).orElseThrow().codigoVerificacion().codigo();
        mvc.perform(post("/usuarios/verificacion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\",\"codigo\":\"%s\"}".formatted(email, codigo)))
                .andExpect(status().isOk());
        return email;
    }

    private String idPublico(String email) {
        return usuarios.buscarPorCorreoPublico(new Email(email)).orElseThrow().id().valor().toString();
    }

    private Actor crearTrabajadorConSesion() throws Exception {
        return SesionHttpSupport.sesionSuperadmin(mvc, json);
    }

    private int idPermiso(String codigo) throws Exception {
        Actor admin = crearTrabajadorConSesion();
        JsonNode modulos = json.readTree(mvc.perform(get("/modulos")
                        .header(HttpHeaders.AUTHORIZATION, admin.bearer()))
                .andReturn().getResponse().getContentAsString());
        for (JsonNode modulo : modulos) {
            for (JsonNode permiso : modulo.get("permisos")) {
                if (codigo.equals(permiso.get("codigo").asText())) {
                    return permiso.get("id").asInt();
                }
            }
        }
        throw new IllegalStateException("No está el permiso " + codigo);
    }

    private int permisosDelRolPublico() {
        return roles.listar().stream()
                .filter(rol -> rol.nombre().valor().equals(NombreRol.USUARIO_PUBLICO))
                .findFirst()
                .orElseThrow()
                .permisos()
                .size();
    }
}
