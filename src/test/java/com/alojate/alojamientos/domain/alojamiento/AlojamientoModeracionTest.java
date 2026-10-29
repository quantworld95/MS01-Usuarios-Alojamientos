package com.alojate.alojamientos.domain.alojamiento;

import com.alojate.alojamientos.domain.alojamiento.aggregate.Alojamiento;
import com.alojate.alojamientos.domain.alojamiento.aggregate.HistorialPublicacion;
import com.alojate.alojamientos.domain.alojamiento.valueobject.AnfitrionId;
import com.alojate.alojamientos.domain.alojamiento.valueobject.DescripcionAlojamiento;
import com.alojate.alojamientos.domain.alojamiento.valueobject.DescripcionUnidad;
import com.alojate.alojamientos.domain.alojamiento.valueobject.EstadoPublicacion;
import com.alojate.alojamientos.domain.alojamiento.valueobject.EstadoUnidad;
import com.alojate.alojamientos.domain.alojamiento.valueobject.HorarioEstadia;
import com.alojate.alojamientos.domain.alojamiento.valueobject.Politicas;
import com.alojate.alojamientos.domain.alojamiento.valueobject.PrecioNoche;
import com.alojate.alojamientos.domain.alojamiento.valueobject.Titulo;
import com.alojate.alojamientos.domain.alojamiento.valueobject.Ubicacion;
import com.alojate.alojamientos.domain.catalogo.valueobject.TipoEspacioId;
import com.alojate.alojamientos.domain.catalogo.valueobject.TipoPropiedadId;
import com.alojate.alojamientos.domain.exception.ReglaNegocioException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AlojamientoModeracionTest {

    private static final Instant AHORA = Instant.parse("2026-10-23T16:00:00Z");
    private static final Instant DESPUES = AHORA.plusSeconds(60);
    private final AnfitrionId anfitrion = new AnfitrionId(UUID.randomUUID());
    private final UUID administrador = UUID.randomUUID();

    @Test
    void aceptarPublicaYDejaElMotivoVacio() {
        Alojamiento alojamiento = enRevision(true);

        alojamiento.aceptar(administrador, DESPUES);

        assertEquals(EstadoPublicacion.PUBLICADO, alojamiento.estado());
        assertNull(alojamiento.motivoVigente());
        assertEquals(2, alojamiento.historial().size());
        assertEquals(administrador, alojamiento.historial().getLast().actorId());
        assertEquals("PUBLICADO", alojamiento.historial().getLast().estado());
    }

    @Test
    void rechazarExigeMotivoYSoloDesdeRevision() {
        Alojamiento alojamiento = enRevision(true);

        alojamiento.rechazar(administrador, "  Fotos borrosas  ", DESPUES);

        assertEquals(EstadoPublicacion.RECHAZADO, alojamiento.estado());
        assertEquals("Fotos borrosas", alojamiento.motivoVigente());
        assertEquals("EN_REVISION", alojamiento.historial().getFirst().estado());

        ReglaNegocioException sinMotivo = assertThrows(
                ReglaNegocioException.class, () -> enRevision(true).rechazar(administrador, "  ", DESPUES));
        assertEquals("DATO_INVALIDO", sinMotivo.codigo());

        ReglaNegocioException publicado = assertThrows(
                ReglaNegocioException.class,
                () -> publicado().rechazar(administrador, "No corresponde", DESPUES));
        assertEquals("PUBLICACION_NO_PERMITIDA", publicado.codigo());
    }

    @Test
    void noPublicaUnaFichaIncompletaNiUnBorrador() {
        ReglaNegocioException incompleto = assertThrows(
                ReglaNegocioException.class, () -> enRevision(false).aceptar(administrador, DESPUES));
        assertEquals("PUBLICACION_NO_PERMITIDA", incompleto.codigo());

        Alojamiento borrador = Alojamiento.registrar(anfitrion, new TipoPropiedadId(1), AHORA);
        ReglaNegocioException estado = assertThrows(
                ReglaNegocioException.class, () -> borrador.aceptar(administrador, DESPUES));
        assertEquals("PUBLICACION_NO_PERMITIDA", estado.codigo());
        assertEquals(EstadoPublicacion.BORRADOR, borrador.estado());
    }

    @Test
    void suspendeSoloPublicadoYElAnfitrionNoModera() {
        Alojamiento publicado = publicado();
        publicado.suspender(administrador, "Queja de huespedes", DESPUES);
        assertEquals(EstadoPublicacion.SUSPENDIDO, publicado.estado());
        assertEquals("Queja de huespedes", publicado.motivoVigente());

        Alojamiento desactivado = copia(publicado(), EstadoPublicacion.DESACTIVADO);
        ReglaNegocioException desdeDesactivado = assertThrows(
                ReglaNegocioException.class,
                () -> desactivado.suspender(administrador, "No aplica", DESPUES));
        assertEquals("PUBLICACION_NO_PERMITIDA", desdeDesactivado.codigo());

        ReglaNegocioException propio = assertThrows(
                ReglaNegocioException.class, () -> enRevision(true).aceptar(anfitrion.valor(), DESPUES));
        assertEquals("PUBLICACION_NO_PERMITIDA", propio.codigo());

        ReglaNegocioException enRevision = assertThrows(
                ReglaNegocioException.class, () -> enRevision(true).suspender(administrador, "No", DESPUES));
        assertEquals("PUBLICACION_NO_PERMITIDA", enRevision.codigo());
    }

    private Alojamiento enRevision(boolean completo) {
        Alojamiento borrador = Alojamiento.registrar(anfitrion, new TipoPropiedadId(1), AHORA);
        if (completo) {
            completar(borrador);
        }
        return copia(borrador, EstadoPublicacion.EN_REVISION);
    }

    private Alojamiento publicado() {
        Alojamiento borrador = Alojamiento.registrar(anfitrion, new TipoPropiedadId(1), AHORA);
        completar(borrador);
        return copia(borrador, EstadoPublicacion.PUBLICADO);
    }

    private Alojamiento copia(Alojamiento origen, EstadoPublicacion estado) {
        return Alojamiento.reconstituir(
                origen.id(),
                origen.anfitrionId(),
                origen.tipoPropiedadId(),
                origen.titulo(),
                origen.descripcion(),
                origen.politicas(),
                origen.horarioEntrada(),
                origen.horarioSalida(),
                origen.ubicacion(),
                estado,
                origen.fechaCreacion(),
                origen.fotografias(),
                origen.servicios(),
                origen.unidades(),
                List.of(HistorialPublicacion.reconstituir(
                        UUID.randomUUID(), estado.name(), null, anfitrion.valor(), AHORA)));
    }

    private void completar(Alojamiento alojamiento) {
        alojamiento.guardarUbicacion(new Ubicacion(
                "Calle Comercio 100, zona central",
                "La Paz",
                "La Paz",
                new BigDecimal("-16.5"),
                new BigDecimal("-68.15")));
        alojamiento.guardarPresentacion(
                new Titulo("Casa en Sopocachi"),
                new DescripcionAlojamiento("Casa con patio y vista a la montana."));
        alojamiento.guardarPoliticas(new Politicas("Cancelacion gratuita hasta 24 horas antes."));
        alojamiento.guardarHorarios(new HorarioEstadia("15:00"), new HorarioEstadia("11:00"));
        alojamiento.guardarFotografias(List.of(
                new DatosFotografia(null, "alojate/casa", "https://cdn.alojate.bo/casa.jpg", 1)));
        alojamiento.guardarOfertaUnica(new DatosUnidad(
                null,
                new TipoEspacioId(1),
                "Casa completa",
                new DescripcionUnidad("Casa completa con patio y buena luz."),
                4,
                2,
                2,
                new BigDecimal("1.5"),
                1,
                new PrecioNoche(new BigDecimal("350.00")),
                EstadoUnidad.BORRADOR,
                List.of(),
                Set.of()), AHORA);
    }
}
