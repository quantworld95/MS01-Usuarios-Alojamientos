package com.alojate.identidad.infrastructure.adapters.input.rest;

import com.alojate.config.Ms01UsuariosAlojamientosApplication;
import com.alojate.support.SesionHttpSupport;
import com.alojate.support.SesionHttpSupport.Actor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = Ms01UsuariosAlojamientosApplication.class)
@AutoConfigureMockMvc
@Transactional
class GestionRolesHttpTest {

    @Autowired
    private MockMvc mvc;

    private final ObjectMapper json = new ObjectMapper();
    private Actor admin;

    @BeforeEach
    void iniciarSesionAdmin() throws Exception {
        admin = SesionHttpSupport.sesionSuperadmin(mvc, json);
    }

    @Test
    void sinSesionNoPermiteListarRoles() throws Exception {
        mvc.perform(get("/roles"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("SESION_REQUERIDA"));
    }

    @Test
    void creaUnRolConPermisosDelModuloIdentidad() throws Exception {
        int primero = permiso(0);
        int segundo = permiso(1);

        mvc.perform(post("/roles")
                        .header(HttpHeaders.AUTHORIZATION, admin.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo("Soporte " + UUID.randomUUID(), "Incidencias", primero, segundo)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.esSistema").value(false))
                .andExpect(jsonPath("$.permisos.length()").value(2));
    }

    @Test
    void rechazaUnNombreDuplicado() throws Exception {
        String nombre = "Duplicado " + UUID.randomUUID();
        String cuerpo = cuerpo(nombre, null, permiso(0));
        mvc.perform(post("/roles")
                        .header(HttpHeaders.AUTHORIZATION, admin.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isCreated());

        mvc.perform(post("/roles")
                        .header(HttpHeaders.AUTHORIZATION, admin.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("ROL_DUPLICADO"));
    }

    @Test
    void rechazaUnPermisoInexistente() throws Exception {
        mvc.perform(post("/roles")
                        .header(HttpHeaders.AUTHORIZATION, admin.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo("Ajeno " + UUID.randomUUID(), null, 999999)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("PERMISO_NO_ASIGNABLE"));
    }

    @Test
    void modificarReemplazaElConjuntoDePermisos() throws Exception {
        int primero = permiso(0);
        int segundo = permiso(1);
        MvcResult alta = mvc.perform(post("/roles")
                        .header(HttpHeaders.AUTHORIZATION, admin.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo("Reemplazo " + UUID.randomUUID(), null, primero)))
                .andExpect(status().isCreated())
                .andReturn();
        int id = json.readTree(alta.getResponse().getContentAsString()).get("id").asInt();

        mvc.perform(put("/roles/" + id)
                        .header(HttpHeaders.AUTHORIZATION, admin.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo("Reemplazo actualizado", "Nuevo", segundo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.permisos.length()").value(1))
                .andExpect(jsonPath("$.permisos[0]").value(segundo));
    }

    @Test
    void rechazaModificarLosRolesDeSistema() throws Exception {
        JsonNode roles = json.readTree(mvc.perform(get("/roles")
                        .header(HttpHeaders.AUTHORIZATION, admin.bearer()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());
        for (String nombre : new String[] {"USUARIO_PUBLICO", "SUPERADMIN"}) {
            int id = idDe(roles, nombre);
            mvc.perform(put("/roles/" + id)
                            .header(HttpHeaders.AUTHORIZATION, admin.bearer())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(cuerpo(nombre, "Cambio", permiso(0))))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.codigo").value("ROL_PROTEGIDO"));
        }
    }

    @Test
    void obtieneUnRolPorId() throws Exception {
        MvcResult alta = mvc.perform(post("/roles")
                        .header(HttpHeaders.AUTHORIZATION, admin.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo("Consulta " + UUID.randomUUID(), "Detalle", permiso(0))))
                .andExpect(status().isCreated())
                .andReturn();
        int id = json.readTree(alta.getResponse().getContentAsString()).get("id").asInt();

        mvc.perform(get("/roles/" + id)
                        .header(HttpHeaders.AUTHORIZATION, admin.bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.activo").value(true))
                .andExpect(jsonPath("$.permisos.length()").value(1));
    }

    @Test
    void desactivaYReactivaUnRolCorporativo() throws Exception {
        MvcResult alta = mvc.perform(post("/roles")
                        .header(HttpHeaders.AUTHORIZATION, admin.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo("Estado " + UUID.randomUUID(), null, permiso(0))))
                .andExpect(status().isCreated())
                .andReturn();
        int id = json.readTree(alta.getResponse().getContentAsString()).get("id").asInt();

        mvc.perform(patch("/roles/" + id + "/activo")
                        .header(HttpHeaders.AUTHORIZATION, admin.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"activo\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(false));

        mvc.perform(get("/roles/" + id)
                        .header(HttpHeaders.AUTHORIZATION, admin.bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(false));

        mvc.perform(patch("/roles/" + id + "/activo")
                        .header(HttpHeaders.AUTHORIZATION, admin.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"activo\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(true));
    }

    @Test
    void rechazaDesactivarUnRolDeSistema() throws Exception {
        JsonNode roles = json.readTree(mvc.perform(get("/roles")
                        .header(HttpHeaders.AUTHORIZATION, admin.bearer()))
                .andReturn().getResponse().getContentAsString());
        int id = idDe(roles, "SUPERADMIN");

        mvc.perform(patch("/roles/" + id + "/activo")
                        .header(HttpHeaders.AUTHORIZATION, admin.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"activo\":false}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("ROL_PROTEGIDO"));
    }

    private int permiso(int posicion) throws Exception {
        String cuerpo = mvc.perform(get("/modulos")
                        .header(HttpHeaders.AUTHORIZATION, admin.bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("IDENTIDAD"))
                .andReturn().getResponse().getContentAsString();
        return json.readTree(cuerpo).get(0).get("permisos").get(posicion).get("id").asInt();
    }

    private int idDe(JsonNode roles, String nombre) {
        for (JsonNode rol : roles) {
            if (nombre.equals(rol.get("nombre").asText())) {
                return rol.get("id").asInt();
            }
        }
        throw new IllegalStateException("No está el rol " + nombre);
    }

    private String cuerpo(String nombre, String descripcion, int... permisos) {
        StringBuilder lista = new StringBuilder();
        for (int permiso : permisos) {
            if (!lista.isEmpty()) {
                lista.append(',');
            }
            lista.append(permiso);
        }
        String texto = descripcion == null ? "null" : "\"" + descripcion + "\"";
        return "{\"nombre\":\"%s\",\"descripcion\":%s,\"permisos\":[%s]}".formatted(nombre, texto, lista);
    }
}
