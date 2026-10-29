package com.alojate.identidad.infrastructure.adapters.input.rest;

import com.alojate.config.Ms01UsuariosAlojamientosApplication;
import com.alojate.identidad.domain.usuario.aggregate.Usuario;
import com.alojate.identidad.domain.usuario.repository.UsuarioRepository;
import com.alojate.identidad.domain.usuario.valueobject.Email;
import com.alojate.identidad.domain.usuario.valueobject.EstadoCuenta;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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
class SesionPublicaHttpTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UsuarioRepository usuarios;

    @Test
    void unaCuentaActivaIniciaSesionGuest() throws Exception {
        String email = registrarYVerificar();

        mvc.perform(post("/sesiones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(sesion(email, "secreta123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contexto").value("GUEST"))
                .andExpect(jsonPath("$.estado").value("ACTIVA"))
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.permisos.length()").value(2))
                .andExpect(jsonPath("$.permisos", hasItem("BUSCAR_ALOJAMIENTOS")))
                .andExpect(jsonPath("$.permisos", hasItem("PUBLICAR_ALOJAMIENTO")));
    }

    @Test
    void unaContrasenaIncorrectaNoEntra() throws Exception {
        String email = registrarYVerificar();

        mvc.perform(post("/sesiones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(sesion(email, "otraClave1")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("CREDENCIALES_INVALIDAS"));
    }

    @Test
    void unaCuentaPendienteNoEntra() throws Exception {
        String email = "pendiente." + UUID.randomUUID() + "@ejemplo.com";
        mvc.perform(post("/usuarios").contentType(MediaType.APPLICATION_JSON).content(registro(email)))
                .andExpect(status().isCreated());

        mvc.perform(post("/sesiones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(sesion(email, "secreta123")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("CUENTA_PENDIENTE"));
    }

    @Test
    void unaCuentaSuspendidaNoEntra() throws Exception {
        String email = registrarYVerificar();
        Usuario activa = usuarios.buscarPorCorreoPublico(new Email(email)).orElseThrow();
        usuarios.guardar(Usuario.reconstituir(
                activa.id(),
                activa.nombre(),
                activa.email(),
                activa.telefono(),
                activa.passwordHash(),
                activa.tipoCuenta(),
                EstadoCuenta.SUSPENDIDA,
                activa.rolId(),
                activa.fechaRegistro(),
                null));

        mvc.perform(post("/sesiones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(sesion(email, "secreta123")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("CUENTA_SUSPENDIDA"));
    }

    private String registrarYVerificar() throws Exception {
        String email = "activa." + UUID.randomUUID() + "@ejemplo.com";
        mvc.perform(post("/usuarios").contentType(MediaType.APPLICATION_JSON).content(registro(email)))
                .andExpect(status().isCreated());
        String codigo = usuarios.buscarPorCorreoPublico(new Email(email)).orElseThrow().codigoVerificacion().codigo();
        mvc.perform(post("/usuarios/verificacion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\",\"codigo\":\"%s\"}".formatted(email, codigo)))
                .andExpect(status().isOk());
        return email;
    }

    private String registro(String email) {
        return """
                {"nombre":"Ana","apellido":"Perez","email":"%s","telefono":"70000000","contrasena":"secreta123"}
                """.formatted(email);
    }

    private String sesion(String email, String contrasena) {
        return "{\"email\":\"%s\",\"contrasena\":\"%s\"}".formatted(email, contrasena);
    }
}
