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
class RegistrarHabitacionesEstablecimientoHttpTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UsuarioRepository usuarios;

    private final ObjectMapper json = new ObjectMapper();

    @Test
    void hotelRegistraTipoDeHabitacionEstandar() throws Exception {
        Actor anfitrion = SesionHttpSupport.registrarPublicoYSesion(mvc, usuarios, json);
        String alojamientoId = crearEstablecimiento(anfitrion, "Hotel");
        int estandar = idCatalogo(anfitrion, "tiposEspacio", "Estandar");
        int wifi = idCatalogo(anfitrion, "servicios", "Wifi");

        mvc.perform(post("/alojamientos/" + alojamientoId + "/unidades")
                        .header(HttpHeaders.AUTHORIZATION, anfitrion.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tipoEspacioId":%d,
                                  "nombre":"Habitacion Estandar",
                                  "descripcion":"Habitacion estandar con cama matrimonial y bano privado.",
                                  "capacidadHuespedes":2,
                                  "cantidadDormitorios":1,
                                  "cantidadCamas":1,
                                  "cantidadBanos":1.0,
                                  "inventarioTotal":8,
                                  "precioNoche":180.00,
                                  "fotografias":[{"identificadorAlmacen":"alojate/estandar","url":"https://cdn.alojate.bo/estandar.jpg","orden":1}],
                                  "servicios":[%d]
                                }
                                """.formatted(estandar, wifi)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unidades.length()").value(1))
                .andExpect(jsonPath("$.unidades[0].tipoEspacio").value("Estandar"))
                .andExpect(jsonPath("$.unidades[0].nombre").value("Habitacion Estandar"))
                .andExpect(jsonPath("$.unidades[0].descripcion").value(
                        "Habitacion estandar con cama matrimonial y bano privado."))
                .andExpect(jsonPath("$.unidades[0].inventarioTotal").value(8))
                .andExpect(jsonPath("$.unidades[0].capacidadHuespedes").value(2));
    }

    @Test
    void hostalRegistraHabitacionCompartidaConInventarioDeCamas() throws Exception {
        Actor anfitrion = SesionHttpSupport.registrarPublicoYSesion(mvc, usuarios, json);
        String alojamientoId = crearEstablecimiento(anfitrion, "Hostal");
        int compartida = idCatalogo(anfitrion, "tiposEspacio", "Habitación compartida");
        int lavanderia = idCatalogo(anfitrion, "servicios", "Lavandería");

        mvc.perform(post("/alojamientos/" + alojamientoId + "/unidades")
                        .header(HttpHeaders.AUTHORIZATION, anfitrion.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tipoEspacioId":%d,
                                  "nombre":"Litera compartida",
                                  "descripcion":"Cama en dormitorio compartido con casillero incluido.",
                                  "capacidadHuespedes":1,
                                  "cantidadDormitorios":1,
                                  "cantidadCamas":1,
                                  "cantidadBanos":1.0,
                                  "inventarioTotal":12,
                                  "precioNoche":55.00,
                                  "fotografias":[{"identificadorAlmacen":"alojate/litera","url":"https://cdn.alojate.bo/litera.jpg","orden":1}],
                                  "servicios":[%d]
                                }
                                """.formatted(compartida, lavanderia)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipoPropiedad").value("Hostal"))
                .andExpect(jsonPath("$.unidades[0].tipoEspacio").value("Habitación compartida"))
                .andExpect(jsonPath("$.unidades[0].inventarioTotal").value(12))
                .andExpect(jsonPath("$.unidades[0].capacidadHuespedes").value(1));
    }

    private String crearEstablecimiento(Actor anfitrion, String tipoPropiedad) throws Exception {
        int tipo = idCatalogo(anfitrion, "tiposPropiedad", tipoPropiedad);
        MvcResult alta = mvc.perform(post("/alojamientos")
                        .header(HttpHeaders.AUTHORIZATION, anfitrion.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tipoPropiedadId\":%d}".formatted(tipo)))
                .andExpect(status().isCreated())
                .andReturn();
        String id = json.readTree(alta.getResponse().getContentAsString()).get("id").asText();

        mvc.perform(put("/alojamientos/" + id + "/presentacion")
                        .header(HttpHeaders.AUTHORIZATION, anfitrion.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titulo":"%s Centro","descripcion":"Establecimiento céntrico con buena atención."}
                                """.formatted(tipoPropiedad)))
                .andExpect(status().isOk());
        mvc.perform(put("/alojamientos/" + id + "/ubicacion")
                        .header(HttpHeaders.AUTHORIZATION, anfitrion.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"direccion":"Av. Arce 100","ciudad":"La Paz","departamento":"La Paz","latitud":-16.5,"longitud":-68.15}
                                """))
                .andExpect(status().isOk());
        mvc.perform(put("/alojamientos/" + id + "/politicas")
                        .header(HttpHeaders.AUTHORIZATION, anfitrion.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"politicas":"Cancelacion gratuita hasta 24 horas antes.","horarioEntrada":"14:00","horarioSalida":"11:00"}
                                """))
                .andExpect(status().isOk());
        mvc.perform(put("/alojamientos/" + id + "/fotografias")
                        .header(HttpHeaders.AUTHORIZATION, anfitrion.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fotografias":[{"identificadorAlmacen":"alojate/fachada","url":"https://cdn.alojate.bo/fachada.jpg","orden":1}]}
                                """))
                .andExpect(status().isOk());
        mvc.perform(post("/alojamientos/" + id + "/finalizar-registro")
                        .header(HttpHeaders.AUTHORIZATION, anfitrion.bearer()))
                .andExpect(status().isOk());
        return id;
    }

    private int idCatalogo(Actor actor, String lista, String nombre) throws Exception {
        MvcResult catalogos = mvc.perform(get("/catalogos-alojamiento")
                        .header(HttpHeaders.AUTHORIZATION, actor.bearer()))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode items = json.readTree(catalogos.getResponse().getContentAsString()).get(lista);
        for (JsonNode item : items) {
            if (nombre.equals(item.get("nombre").asText())) {
                return item.get("id").asInt();
            }
        }
        throw new IllegalStateException("Falta " + lista + " " + nombre);
    }
}
