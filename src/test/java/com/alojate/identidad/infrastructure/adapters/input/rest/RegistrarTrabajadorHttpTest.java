package com.alojate.identidad.infrastructure.adapters.input.rest;

import com.alojate.config.Ms01UsuariosAlojamientosApplication;
import com.alojate.support.SesionHttpSupport;
import com.alojate.support.SesionHttpSupport.Actor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = Ms01UsuariosAlojamientosApplication.class)
@AutoConfigureMockMvc
@Transactional
class RegistrarTrabajadorHttpTest {

    @Autowired
    private MockMvc mvc;

    private final ObjectMapper json = new ObjectMapper();
    private Actor admin;

    @BeforeEach
    void iniciarSesionAdmin() throws Exception {
        admin = SesionHttpSupport.sesionSuperadmin(mvc, json);
    }

    @Test
    void sinSesionNoPermiteRegistrar() throws Exception {
        mvc.perform(post("/usuarios/corporativos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo("sin.sesion." + UUID.randomUUID() + "@alojate.bo", 1)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("SESION_REQUERIDA"));
    }

    @Test
    void incorporaUnTrabajadorActivo() throws Exception {
        mvc.perform(post("/usuarios/corporativos")
                        .header(HttpHeaders.AUTHORIZATION, admin.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo("activo." + UUID.randomUUID() + "@alojate.bo", idRol("SUPERADMIN"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("ACTIVA"));
    }

    @Test
    void rechazaElCorreoCorporativoDuplicado() throws Exception {
        String email = "duplicado." + UUID.randomUUID() + "@alojate.bo";
        String cuerpo = cuerpo(email, idRol("SUPERADMIN"));
        mvc.perform(post("/usuarios/corporativos")
                        .header(HttpHeaders.AUTHORIZATION, admin.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isCreated());

        mvc.perform(post("/usuarios/corporativos")
                        .header(HttpHeaders.AUTHORIZATION, admin.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("CORREO_REGISTRADO"));
    }

    @Test
    void rechazaUnCorreoYaUsadoPorUnaCuentaPublica() throws Exception {
        String email = "ambos." + UUID.randomUUID() + "@alojate.bo";
        mvc.perform(post("/usuarios").contentType(MediaType.APPLICATION_JSON).content("""
                {"nombre":"Ana","apellido":"Perez","email":"%s","telefono":"70000000","contrasena":"secreta123"}
                """.formatted(email))).andExpect(status().isCreated());

        mvc.perform(post("/usuarios/corporativos")
                        .header(HttpHeaders.AUTHORIZATION, admin.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo(email, idRol("SUPERADMIN"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("CORREO_REGISTRADO"));
    }

    @Test
    void editaDatosRolYDesactiva() throws Exception {
        String email = "editable." + UUID.randomUUID() + "@alojate.bo";
        String alta = mvc.perform(post("/usuarios/corporativos")
                        .header(HttpHeaders.AUTHORIZATION, admin.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo(email, idRol("SUPERADMIN"))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String id = json.readTree(alta).get("usuarioId").asText();
        int rolNuevo = crearRol();
        String correoNuevo = "editado." + UUID.randomUUID() + "@alojate.bo";

        mvc.perform(put("/usuarios/corporativos/" + id)
                        .header(HttpHeaders.AUTHORIZATION, admin.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(edicion("Maria", "Lopez", correoNuevo, "72222222", rolNuevo, false)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Maria"))
                .andExpect(jsonPath("$.email").value(correoNuevo))
                .andExpect(jsonPath("$.rolId").value(rolNuevo))
                .andExpect(jsonPath("$.estado").value("DESACTIVADA"));

        mvc.perform(get("/usuarios/corporativos/" + id)
                        .header(HttpHeaders.AUTHORIZATION, admin.bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("DESACTIVADA"))
                .andExpect(jsonPath("$.rolId").value(rolNuevo));

        mvc.perform(post("/sesiones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\",\"contrasena\":\"secreta123\"}".formatted(correoNuevo)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("CUENTA_DESACTIVADA"));

        mvc.perform(put("/usuarios/corporativos/" + id)
                        .header(HttpHeaders.AUTHORIZATION, admin.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(edicion("Maria", "Lopez", correoNuevo, "72222222", rolNuevo, true)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ACTIVA"));
    }

    @Test
    void rechazaElRolPublico() throws Exception {
        mvc.perform(post("/usuarios/corporativos")
                        .header(HttpHeaders.AUTHORIZATION, admin.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo("publico." + UUID.randomUUID() + "@alojate.bo", idRol("USUARIO_PUBLICO"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("ROL_NO_ASIGNABLE"));
    }

    private int idRol(String nombre) throws Exception {
        return SesionHttpSupport.idRol(mvc, json, admin, nombre);
    }

    private int crearRol() throws Exception {
        int permiso = json.readTree(mvc.perform(get("/modulos")
                        .header(HttpHeaders.AUTHORIZATION, admin.bearer()))
                .andReturn().getResponse().getContentAsString())
                .get(0).get("permisos").get(0).get("id").asInt();
        String cuerpo = mvc.perform(post("/roles")
                        .header(HttpHeaders.AUTHORIZATION, admin.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Recepcion %s","descripcion":null,"permisos":[%d]}
                                """.formatted(UUID.randomUUID(), permiso)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return json.readTree(cuerpo).get("id").asInt();
    }

    private String cuerpo(String email, int idRol) {
        return """
                {"nombre":"Luis","apellido":"Quispe","email":"%s","telefono":"70000001","contrasena":"secreta123","idRol":%d}
                """.formatted(email, idRol);
    }

    private String edicion(String nombre, String apellido, String email, String telefono, int idRol, boolean activo) {
        return """
                {"nombre":"%s","apellido":"%s","email":"%s","telefono":"%s","idRol":%d,"activo":%s}
                """.formatted(nombre, apellido, email, telefono, idRol, activo);
    }
}
