package com.alojate.alojamientos.infrastructure.adapters.input.rest;

import com.alojate.config.Ms01UsuariosAlojamientosApplication;
import com.alojate.identidad.domain.usuario.repository.UsuarioRepository;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = Ms01UsuariosAlojamientosApplication.class)
@AutoConfigureMockMvc
@Transactional
class RegistrarAlojamientoHttpTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UsuarioRepository usuarios;

    private final ObjectMapper json = new ObjectMapper();

    @Test
    void elAnfitrionRegistraUnBorradorYGuardaLaUbicacion() throws Exception {
        Actor anfitrion = SesionHttpSupport.registrarPublicoYSesion(mvc, usuarios, json);
        int tipo = SesionHttpSupport.tipoPropiedad(mvc, json, anfitrion, "Casa completa");

        MvcResult alta = mvc.perform(post("/alojamientos")
                        .header(HttpHeaders.AUTHORIZATION, anfitrion.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tipoPropiedadId\":%d}".formatted(tipo)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("BORRADOR"))
                .andExpect(jsonPath("$.listoParaRevision").value(false))
                .andExpect(jsonPath("$.historial.length()").value(1))
                .andReturn();
        String id = json.readTree(alta.getResponse().getContentAsString()).get("id").asText();

        mvc.perform(put("/alojamientos/" + id + "/ubicacion")
                        .header(HttpHeaders.AUTHORIZATION, anfitrion.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"direccion":"Calle Comercio 100, zona central","ciudad":"La Paz","departamento":"La Paz","latitud":-16.5,"longitud":-68.15}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ciudad").value("La Paz"))
                .andExpect(jsonPath("$.departamento").value("La Paz"))
                .andExpect(jsonPath("$.estado").value("BORRADOR"));

        mvc.perform(get("/alojamientos").header(HttpHeaders.AUTHORIZATION, anfitrion.bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].estado").value("BORRADOR"));
    }
}
