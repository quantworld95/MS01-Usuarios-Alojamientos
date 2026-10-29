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
import com.alojate.alojamientos.domain.catalogo.valueobject.TipoEspacioId;
import com.alojate.alojamientos.domain.catalogo.valueobject.TipoPropiedadId;
import com.alojate.config.Ms01UsuariosAlojamientosApplication;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = Ms01UsuariosAlojamientosApplication.class)
@AutoConfigureMockMvc
@Transactional
class AdministrarAlojamientosHttpTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UsuarioRepository usuarios;

    @Autowired
    private AlojamientoRepository alojamientos;

    private final ObjectMapper json = new ObjectMapper();

    @Test
    void elAdministradorAceptaYElCatalogoLoMuestra() throws Exception {
        Actor anfitrion = huespedActivo("Ana", "Perez");
        Actor administrador = administrador();
        int tipo = idCatalogo(anfitrion, "tiposPropiedad", "Casa completa");
        int espacio = idCatalogo(anfitrion, "tiposEspacio", "Alojamiento completo");
        Alojamiento revision = alojamientos.guardar(oferta(anfitrion.id(), tipo, espacio, EstadoPublicacion.EN_REVISION));

        mvc.perform(get("/admin/alojamientos/" + revision.id().valor())
                        .header(HttpHeaders.AUTHORIZATION, administrador.bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreAnfitrion").value("Ana Perez"))
                .andExpect(jsonPath("$.estado").value("EN_REVISION"));

        mvc.perform(post("/admin/alojamientos/" + revision.id().valor() + "/aceptar")
                        .header(HttpHeaders.AUTHORIZATION, administrador.bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("PUBLICADO"))
                .andExpect(jsonPath("$.motivo").value(nullValue()))
                .andExpect(jsonPath("$.historial[-1].actorId").value(administrador.id().toString()));

        assertTrue(catalogoContiene(anfitrion, revision.id().valor().toString()));

        mvc.perform(post("/admin/alojamientos/" + revision.id().valor() + "/aceptar")
                        .header(HttpHeaders.AUTHORIZATION, anfitrion.bearer()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("PERMISO_DENEGADO"));
    }

    @Test
    void rechazaConMotivoYSuspendeLoPublicado() throws Exception {
        Actor anfitrion = huespedActivo("Luis", "Quispe");
        Actor administrador = administrador();
        int tipo = idCatalogo(anfitrion, "tiposPropiedad", "Casa completa");
        int espacio = idCatalogo(anfitrion, "tiposEspacio", "Alojamiento completo");
        Alojamiento revision = alojamientos.guardar(oferta(anfitrion.id(), tipo, espacio, EstadoPublicacion.EN_REVISION));
        Alojamiento publicado = alojamientos.guardar(oferta(anfitrion.id(), tipo, espacio, EstadoPublicacion.PUBLICADO));

        mvc.perform(post("/admin/alojamientos/" + revision.id().valor() + "/rechazar")
                        .header(HttpHeaders.AUTHORIZATION, administrador.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo\":\"Fotos borrosas\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("RECHAZADO"))
                .andExpect(jsonPath("$.motivo").value("Fotos borrosas"));
        assertFalse(catalogoContiene(anfitrion, revision.id().valor().toString()));

        mvc.perform(post("/admin/alojamientos/" + publicado.id().valor() + "/suspender")
                        .header(HttpHeaders.AUTHORIZATION, administrador.bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo\":\"Queja de huéspedes\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("SUSPENDIDO"))
                .andExpect(jsonPath("$.motivo").value("Queja de huéspedes"));
        assertFalse(catalogoContiene(anfitrion, publicado.id().valor().toString()));
    }

    private boolean catalogoContiene(Actor huesped, String alojamientoId) throws Exception {
        MvcResult lista = mvc.perform(get("/catalogo/alojamientos")
                        .header(HttpHeaders.AUTHORIZATION, huesped.bearer()))
                .andExpect(status().isOk())
                .andReturn();
        for (JsonNode oferta : json.readTree(lista.getResponse().getContentAsString())) {
            if (alojamientoId.equals(oferta.get("id").asText())) {
                return true;
            }
        }
        return false;
    }

    private Alojamiento oferta(UUID anfitrion, int tipo, int espacio, EstadoPublicacion estado) {
        Instant ahora = Instant.parse("2026-10-23T16:00:00Z");
        return Alojamiento.reconstituir(
                AlojamientoId.nuevo(),
                new AnfitrionId(anfitrion),
                new TipoPropiedadId(tipo),
                new Titulo("Casa del lago"),
                new DescripcionAlojamiento("Casa con vista al lago y desayuno"),
                new com.alojate.alojamientos.domain.alojamiento.valueobject.Politicas(
                        "Cancelacion gratuita hasta 24 horas antes."),
                new com.alojate.alojamientos.domain.alojamiento.valueobject.HorarioEstadia("15:00"),
                new com.alojate.alojamientos.domain.alojamiento.valueobject.HorarioEstadia("11:00"),
                new Ubicacion(
                        "Calle Comercio 100, zona central",
                        "La Paz",
                        "La Paz",
                        new BigDecimal("-16.5"),
                        new BigDecimal("-68.15")),
                estado,
                ahora,
                List.of(Fotografia.reconstituir(
                        UUID.randomUUID(), UUID.randomUUID().toString(), "https://ejemplo.com/casa.jpg", 1)),
                Set.of(),
                List.of(UnidadReservable.reconstituir(
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
                        List.of())),
                List.of(HistorialPublicacion.reconstituir(
                        UUID.randomUUID(), estado.name(), null, anfitrion, ahora)));
    }

    private Actor administrador() throws Exception {
        return SesionHttpSupport.sesionSuperadmin(mvc, json);
    }

    private Actor huespedActivo(String nombre, String apellido) throws Exception {
        String email = "host." + UUID.randomUUID() + "@ejemplo.com";
        mvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"%s","apellido":"%s","email":"%s","telefono":"70000000","contrasena":"secreta123"}
                                """.formatted(nombre, apellido, email)))
                .andExpect(status().isCreated());
        String codigo = usuarios.buscarPorCorreoPublico(new Email(email)).orElseThrow().codigoVerificacion().codigo();
        mvc.perform(post("/usuarios/verificacion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\",\"codigo\":\"%s\"}".formatted(email, codigo)))
                .andExpect(status().isOk());
        return SesionHttpSupport.sesion(mvc, json, email, "secreta123");
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
