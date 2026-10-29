package com.alojate.alojamientos.infrastructure.adapters.input.rest;

import com.alojate.alojamientos.domain.alojamiento.aggregate.Alojamiento;
import com.alojate.alojamientos.domain.alojamiento.aggregate.Fotografia;
import com.alojate.alojamientos.domain.alojamiento.aggregate.HistorialPublicacion;
import com.alojate.alojamientos.domain.alojamiento.aggregate.UnidadReservable;
import com.alojate.alojamientos.domain.alojamiento.repository.AlojamientoRepository;
import com.alojate.alojamientos.domain.alojamiento.valueobject.AlojamientoId;
import com.alojate.alojamientos.domain.alojamiento.valueobject.AnfitrionId;
import com.alojate.alojamientos.domain.alojamiento.valueobject.DescripcionAlojamiento;
import com.alojate.alojamientos.domain.alojamiento.valueobject.EstadoPublicacion;
import com.alojate.alojamientos.domain.alojamiento.valueobject.EstadoUnidad;
import com.alojate.alojamientos.domain.alojamiento.valueobject.PrecioNoche;
import com.alojate.alojamientos.domain.alojamiento.valueobject.Titulo;
import com.alojate.alojamientos.domain.alojamiento.valueobject.Ubicacion;
import com.alojate.alojamientos.domain.catalogo.valueobject.ServicioId;
import com.alojate.alojamientos.domain.catalogo.valueobject.TipoEspacioId;
import com.alojate.alojamientos.domain.catalogo.valueobject.TipoPropiedadId;
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

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = Ms01UsuariosAlojamientosApplication.class)
@AutoConfigureMockMvc
@Transactional
class ExplorarCatalogoHttpTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UsuarioRepository usuarios;

    @Autowired
    private AlojamientoRepository alojamientos;

    private final ObjectMapper json = new ObjectMapper();

    @Test
    void elHuespedVeSoloLoPublicado() throws Exception {
        Actor huesped = SesionHttpSupport.registrarPublicoYSesion(mvc, usuarios, json);
        int tipo = idCatalogo(huesped, "tiposPropiedad", "Casa completa");
        int espacio = idCatalogo(huesped, "tiposEspacio", "Alojamiento completo");
        int wifi = idCatalogo(huesped, "servicios", "Wifi");

        MvcResult alta = mvc.perform(post("/alojamientos")
                        .header(HttpHeaders.AUTHORIZATION, huesped.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tipoPropiedadId\":%d}".formatted(tipo)))
                .andExpect(status().isCreated())
                .andReturn();
        String borrador = json.readTree(alta.getResponse().getContentAsString()).get("id").asText();

        Alojamiento publicado = alojamientos.guardar(oferta(huesped.id(), tipo, espacio, wifi));

        MvcResult lista = mvc.perform(get("/catalogo/alojamientos")
                        .header(HttpHeaders.AUTHORIZATION, huesped.bearer()))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode ofertas = json.readTree(lista.getResponse().getContentAsString());
        assertTrue(contiene(ofertas, publicado.id().valor().toString()));
        assertFalse(contiene(ofertas, borrador));

        mvc.perform(get("/catalogo/alojamientos/" + borrador)
                        .header(HttpHeaders.AUTHORIZATION, huesped.bearer()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("ALOJAMIENTO_NO_ENCONTRADO"));

        MvcResult detalle = mvc.perform(get("/catalogo/alojamientos/" + publicado.id().valor())
                        .header(HttpHeaders.AUTHORIZATION, huesped.bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Casa del lago"))
                .andExpect(jsonPath("$.ciudad").value("La Paz"))
                .andExpect(jsonPath("$.fotografias[0].url").value("https://ejemplo.com/casa.jpg"))
                .andExpect(jsonPath("$.fotografias[0].identificadorAlmacen").doesNotExist())
                .andExpect(jsonPath("$.unidades.length()").value(1))
                .andExpect(jsonPath("$.unidades[0].nombre").value("Suite"))
                .andExpect(jsonPath("$.historial").doesNotExist())
                .andReturn();
        String unidadId = json.readTree(detalle.getResponse().getContentAsString())
                .get("unidades")
                .get(0)
                .get("id")
                .asText();

        mvc.perform(get("/catalogo/alojamientos/" + publicado.id().valor() + "/unidades/" + unidadId)
                        .header(HttpHeaders.AUTHORIZATION, huesped.bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Suite"))
                .andExpect(jsonPath("$.descripcion").value("Suite con vista y bano privado."))
                .andExpect(jsonPath("$.capacidadHuespedes").value(2))
                .andExpect(jsonPath("$.precioNoche").value(250.00));

        mvc.perform(get("/catalogo/alojamientos"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("SESION_REQUERIDA"));
    }

    private boolean contiene(JsonNode ofertas, String id) {
        for (JsonNode oferta : ofertas) {
            if (id.equals(oferta.get("id").asText())) {
                return true;
            }
        }
        return false;
    }

    private Alojamiento oferta(UUID anfitrion, int tipo, int espacio, int servicio) {
        Instant ahora = Instant.parse("2026-10-23T16:00:00Z");
        return Alojamiento.reconstituir(
                AlojamientoId.nuevo(),
                new AnfitrionId(anfitrion),
                new TipoPropiedadId(tipo),
                new Titulo("Casa del lago"),
                new DescripcionAlojamiento("Casa con vista al lago y desayuno"),
                null,
                null,
                null,
                new Ubicacion(
                        "Calle Comercio 100, zona central",
                        "La Paz",
                        "La Paz",
                        new BigDecimal("-16.5"),
                        new BigDecimal("-68.15")),
                EstadoPublicacion.PUBLICADO,
                ahora,
                List.of(Fotografia.reconstituir(
                        UUID.randomUUID(), "casa-1", "https://ejemplo.com/casa.jpg", 1)),
                Set.of(new ServicioId(servicio)),
                List.of(
                        UnidadReservable.reconstituir(
                                UUID.randomUUID(),
                                new TipoEspacioId(espacio),
                                "Suite",
                                new com.alojate.alojamientos.domain.alojamiento.valueobject.DescripcionUnidad(
                                        "Suite con vista y bano privado."),
                                2,
                                1,
                                1,
                                new BigDecimal("1.0"),
                                1,
                                new PrecioNoche(new BigDecimal("250.00")),
                                EstadoUnidad.BORRADOR,
                                List.of(),
                                Set.of(),
                                List.of()),
                        UnidadReservable.reconstituir(
                                UUID.randomUUID(),
                                new TipoEspacioId(espacio),
                                "Cuarto cerrado",
                                new com.alojate.alojamientos.domain.alojamiento.valueobject.DescripcionUnidad(
                                        "Cuarto cerrado temporalmente para mantenimiento."),
                                2,
                                1,
                                1,
                                new BigDecimal("1.0"),
                                1,
                                new PrecioNoche(new BigDecimal("80.00")),
                                EstadoUnidad.DESACTIVADO,
                                List.of(),
                                Set.of(),
                                List.of())),
                List.of(HistorialPublicacion.reconstituir(
                        UUID.randomUUID(), EstadoPublicacion.PUBLICADO.name(), null, anfitrion, ahora)));
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
