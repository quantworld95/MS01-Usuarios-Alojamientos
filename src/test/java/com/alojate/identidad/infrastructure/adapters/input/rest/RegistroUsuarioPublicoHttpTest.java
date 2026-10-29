package com.alojate.identidad.infrastructure.adapters.input.rest;

import com.alojate.config.Ms01UsuariosAlojamientosApplication;
import com.alojate.identidad.domain.usuario.repository.UsuarioRepository;
import com.alojate.identidad.domain.usuario.valueobject.Email;
import com.alojate.identidad.domain.usuario.valueobject.EstadoCuenta;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = Ms01UsuariosAlojamientosApplication.class)
@AutoConfigureMockMvc
@Transactional
class RegistroUsuarioPublicoHttpTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UsuarioRepository usuarios;

    private final ObjectMapper json = new ObjectMapper();

    @Test
    void registraVerificaYRechazaElCorreoDuplicado() throws Exception {
        String email = "ana." + UUID.randomUUID() + "@ejemplo.com";
        String registro = """
                {"nombre":"Ana","apellido":"Perez","email":"%s","telefono":"70000000","contrasena":"secreta123"}
                """.formatted(email);

        mvc.perform(post("/usuarios").contentType(MediaType.APPLICATION_JSON).content(registro))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("PENDIENTE_VERIFICACION"));

        String codigo = usuarios.buscarPorCorreoPublico(new Email(email))
                .orElseThrow()
                .codigoVerificacion()
                .codigo();

        mvc.perform(post("/usuarios/verificacion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\",\"codigo\":\"%s\"}".formatted(email, codigo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ACTIVA"));

        mvc.perform(post("/usuarios").contentType(MediaType.APPLICATION_JSON).content(registro))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("CORREO_REGISTRADO"));

        var guardado = usuarios.buscarPorCorreoPublico(new Email(email)).orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals(EstadoCuenta.ACTIVA, guardado.estado());
        org.junit.jupiter.api.Assertions.assertEquals(1, guardado.rolId().valor());
    }

    @Test
    void rechazaUnCorreoYaUsadoPorUnaCuentaCorporativa() throws Exception {
        Actor admin = SesionHttpSupport.sesionSuperadmin(mvc, json);
        String email = "trabajador." + UUID.randomUUID() + "@alojate.bo";
        mvc.perform(post("/usuarios/corporativos")
                        .header(HttpHeaders.AUTHORIZATION, admin.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Luis","apellido":"Quispe","email":"%s","telefono":"70000001","contrasena":"secreta123","idRol":%d}
                                """.formatted(email, SesionHttpSupport.idRol(mvc, json, admin, "SUPERADMIN"))))
                .andExpect(status().isCreated());

        mvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"Ana","apellido":"Perez","email":"%s","telefono":"70000000","contrasena":"secreta123"}
                                """.formatted(email)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("CORREO_REGISTRADO"));
    }
}
