package com.alojate.identidad.infrastructure.adapters.input.rest;

import com.alojate.config.Ms01UsuariosAlojamientosApplication;
import com.alojate.support.SesionHttpSupport;
import com.alojate.support.SesionHttpSupport.Actor;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = Ms01UsuariosAlojamientosApplication.class)
@AutoConfigureMockMvc
@Transactional
class SesionAdministrativaHttpTest {

    @Autowired
    private MockMvc mvc;

    private final ObjectMapper json = new ObjectMapper();

    @Test
    void unTrabajadorActivoIniciaSesionAdmin() throws Exception {
        String email = trabajador();

        mvc.perform(post("/sesiones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credenciales(email, "secreta123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contexto").value("ADMIN"))
                .andExpect(jsonPath("$.estado").value("ACTIVA"))
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.permisos", hasItem("ADMINISTRAR_ROLES")));
    }

    @Test
    void unaCuentaPublicaPendienteNoEntra() throws Exception {
        String email = "publica." + UUID.randomUUID() + "@alojate.bo";
        mvc.perform(post("/usuarios").contentType(MediaType.APPLICATION_JSON).content("""
                {"nombre":"Ana","apellido":"Perez","email":"%s","telefono":"70000000","contrasena":"secreta123"}
                """.formatted(email))).andExpect(status().isCreated());

        mvc.perform(post("/sesiones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credenciales(email, "secreta123")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("CUENTA_PENDIENTE"));
    }

    @Test
    void unaContrasenaIncorrectaNoEntra() throws Exception {
        String email = trabajador();

        mvc.perform(post("/sesiones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credenciales(email, "otraClave1")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("CREDENCIALES_INVALIDAS"));
    }

    private String trabajador() throws Exception {
        Actor admin = SesionHttpSupport.sesionSuperadmin(mvc, json);
        String email = "admin." + UUID.randomUUID() + "@alojate.bo";
        mvc.perform(post("/usuarios/corporativos")
                        .header(HttpHeaders.AUTHORIZATION, admin.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Luis","apellido":"Quispe","email":"%s","telefono":"70000001","contrasena":"secreta123","idRol":%d}
                                """.formatted(email, SesionHttpSupport.idRol(mvc, json, admin, "SUPERADMIN"))))
                .andExpect(status().isCreated());
        return email;
    }

    private String credenciales(String email, String contrasena) {
        return "{\"email\":\"%s\",\"contrasena\":\"%s\"}".formatted(email, contrasena);
    }
}
