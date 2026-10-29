package com.alojate.alojamientos.infrastructure.adapters.input.rest;

import com.alojate.config.Ms01UsuariosAlojamientosApplication;
import com.alojate.identidad.domain.usuario.repository.UsuarioRepository;
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
class RegistrarEstablecimientoHotelHttpTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UsuarioRepository usuarios;

    private final ObjectMapper json = new ObjectMapper();

    @Test
    void registraElEstablecimientoDeHotelEnFormularioProgresivo() throws Exception {
        Actor anfitrion = SesionHttpSupport.registrarPublicoYSesion(mvc, usuarios, json);
        int hotel = idCatalogo(anfitrion, "tiposPropiedad", "Hotel");
        int desayuno = idCatalogo(anfitrion, "servicios", "Desayuno");
        int estacionamiento = idCatalogo(anfitrion, "servicios", "Estacionamiento");

        MvcResult alta = mvc.perform(post("/alojamientos")
                        .header(HttpHeaders.AUTHORIZATION, anfitrion.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tipoPropiedadId\":%d}".formatted(hotel)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipoPropiedad").value("Hotel"))
                .andExpect(jsonPath("$.estado").value("BORRADOR"))
                .andExpect(jsonPath("$.establecimientoCompleto").value(false))
                .andExpect(jsonPath("$.listoParaRevision").value(false))
                .andReturn();
        String id = json.readTree(alta.getResponse().getContentAsString()).get("id").asText();

        mvc.perform(put("/alojamientos/" + id + "/presentacion")
                        .header(HttpHeaders.AUTHORIZATION, anfitrion.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titulo":"Hotel Illimani","descripcion":"Hotel céntrico con desayuno incluido."}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Hotel Illimani"));

        mvc.perform(put("/alojamientos/" + id + "/ubicacion")
                        .header(HttpHeaders.AUTHORIZATION, anfitrion.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"direccion":"Av. Arce 1230","ciudad":"La Paz","departamento":"La Paz","latitud":-16.5,"longitud":-68.15}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.direccion").value("Av. Arce 1230"));

        mvc.perform(put("/alojamientos/" + id + "/servicios")
                        .header(HttpHeaders.AUTHORIZATION, anfitrion.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"servicios\":[%d,%d]}".formatted(desayuno, estacionamiento)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.servicios.length()").value(2));

        mvc.perform(put("/alojamientos/" + id + "/politicas")
                        .header(HttpHeaders.AUTHORIZATION, anfitrion.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"politicas":"Cancelacion gratuita hasta 24 horas antes.","horarioEntrada":"14:00","horarioSalida":"11:00"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.politicas").value("Cancelacion gratuita hasta 24 horas antes."))
                .andExpect(jsonPath("$.horarioEntrada").value("14:00"))
                .andExpect(jsonPath("$.horarioSalida").value("11:00"));

        mvc.perform(put("/alojamientos/" + id + "/fotografias")
                        .header(HttpHeaders.AUTHORIZATION, anfitrion.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fotografias":[{"identificadorAlmacen":"alojate/hotel-fachada","url":"https://cdn.alojate.bo/hotel.jpg","orden":1}]}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fotografias.length()").value(1))
                .andExpect(jsonPath("$.establecimientoCompleto").value(true))
                .andExpect(jsonPath("$.listoParaRevision").value(false));
    }

    private int idCatalogo(Actor actor, String lista, String nombre) throws Exception {
        MvcResult catalogos = mvc.perform(get("/catalogos-alojamiento")
                        .header(HttpHeaders.AUTHORIZATION, actor.bearer()))
                .andExpect(status().isOk())
                .andReturn();
        for (JsonNode item : json.readTree(catalogos.getResponse().getContentAsString()).get(lista)) {
            if (nombre.equals(item.get("nombre").asText())) {
                return item.get("id").asInt();
            }
        }
        throw new IllegalStateException("Falta " + nombre);
    }
}
