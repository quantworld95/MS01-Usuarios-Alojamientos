package com.alojate.alojamientos.domain.alojamiento;

import com.alojate.alojamientos.domain.alojamiento.aggregate.Alojamiento;
import com.alojate.alojamientos.domain.alojamiento.DatosFotografia;
import com.alojate.alojamientos.domain.alojamiento.DatosUnidad;
import com.alojate.alojamientos.domain.alojamiento.politica.PoliticaCicloPublicacion;
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
import com.alojate.alojamientos.domain.catalogo.valueobject.ServicioId;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AlojamientoRegistroTest {

    private static final Instant AHORA = Instant.parse("2026-10-23T16:00:00Z");
    private final AnfitrionId anfitrion = new AnfitrionId(UUID.randomUUID());
    private final UUID moderador = UUID.randomUUID();

    @Test
    void elRegistroNaceEnBorradorSinFichaCompleta() {
        Alojamiento alojamiento = Alojamiento.registrar(anfitrion, new TipoPropiedadId(1), AHORA);

        assertEquals(EstadoPublicacion.BORRADOR, alojamiento.estado());
        assertNull(alojamiento.titulo());
        assertNull(alojamiento.ubicacion());
        assertFalse(alojamiento.listoParaRevision());
        assertEquals(1, alojamiento.historial().size());
        assertEquals("BORRADOR", alojamiento.historial().getFirst().estado());
        assertEquals(anfitrion.valor(), alojamiento.historial().getFirst().actorId());
    }

    @Test
    void laUbicacionTieneQueCaerEnBolivia() {
        Alojamiento alojamiento = borrador();

        ReglaNegocioException error = assertThrows(ReglaNegocioException.class, () -> alojamiento.guardarUbicacion(
                new Ubicacion("Calle falsa 123", "Lima", "La Paz", new BigDecimal("-12.0"), new BigDecimal("-77.0"))));

        assertEquals("DATO_INVALIDO", error.codigo());
        assertNull(alojamiento.ubicacion());
    }

    @Test
    void aceptaElPinDelMapaAunqueTraigaMasDeSeisDecimales() {
        Alojamiento alojamiento = borrador();

        alojamiento.guardarUbicacion(new Ubicacion(
                "Av. San Martín 1234",
                "Santa Cruz de la Sierra",
                "Santa Cruz",
                new BigDecimal("-17.783327412345"),
                new BigDecimal("-63.182145678901")));

        assertEquals(0, new BigDecimal("-17.783327").compareTo(alojamiento.ubicacion().latitud()));
        assertEquals(0, new BigDecimal("-63.182146").compareTo(alojamiento.ubicacion().longitud()));
    }

    @Test
    void elEstablecimientoQuedaCompletoSinUnidades() {
        Alojamiento alojamiento = borrador();
        completarEstablecimiento(alojamiento);

        assertTrue(alojamiento.establecimientoCompleto());
        assertFalse(alojamiento.listoParaRevision());
    }

    @Test
    void quedaListoCuandoTieneEstablecimientoYUnidadActiva() {
        Alojamiento alojamiento = borrador();
        completar(alojamiento);

        assertTrue(alojamiento.establecimientoCompleto());
        assertTrue(alojamiento.listoParaRevision());
        assertEquals(EstadoPublicacion.BORRADOR, alojamiento.estado());
    }

    @Test
    void alPublicarPasaDeBorradorAEnRevision() {
        Alojamiento alojamiento = borrador();
        completar(alojamiento);

        alojamiento.enviarARevision(AHORA.plusSeconds(10));

        assertEquals(EstadoPublicacion.EN_REVISION, alojamiento.estado());
    }

    @Test
    void casaAlFinalizarRegistroPasaAEnRevision() {
        Alojamiento alojamiento = borrador();
        completar(alojamiento);
        var politica = PoliticaCicloPublicacion.desdeNombreTipo("Casa");

        alojamiento.finalizarRegistro(politica, AHORA.plusSeconds(10));

        assertEquals(EstadoPublicacion.EN_REVISION, alojamiento.estado());
    }

    @Test
    void hotelAlFinalizarRegistroPasaAActivoSinHabitaciones() {
        Alojamiento alojamiento = borrador();
        completarEstablecimiento(alojamiento);
        var politica = PoliticaCicloPublicacion.desdeNombreTipo("Hotel");

        alojamiento.finalizarRegistro(politica, AHORA.plusSeconds(10));

        assertEquals(EstadoPublicacion.ACTIVO, alojamiento.estado());
        assertFalse(alojamiento.tieneUnidadActiva());
    }

    @Test
    void hotelActivoAgregaUnaHabitacionSinTocarLasExistentes() {
        Alojamiento alojamiento = borrador();
        completarEstablecimiento(alojamiento);
        var politica = PoliticaCicloPublicacion.Establecimiento.INSTANCE;
        alojamiento.finalizarRegistro(politica, AHORA.plusSeconds(5));
        alojamiento.agregarUnidad(unidadDatos(null, EstadoUnidad.BORRADOR), AHORA.plusSeconds(8));
        UUID primera = alojamiento.unidades().getFirst().id();

        alojamiento.agregarUnidad(
                unidadDatos(null, EstadoUnidad.BORRADOR), AHORA.plusSeconds(12));

        assertEquals(2, alojamiento.unidades().size());
        assertEquals(primera, alojamiento.unidades().getFirst().id());
        assertEquals(EstadoUnidad.BORRADOR, alojamiento.unidades().getLast().estado());
        assertEquals(EstadoPublicacion.ACTIVO, alojamiento.estado());
    }

    @Test
    void alAgregarYEnviarHabitacionQuedaEnRevision() {
        Alojamiento alojamiento = borrador();
        completarEstablecimiento(alojamiento);
        var politica = PoliticaCicloPublicacion.Establecimiento.INSTANCE;
        alojamiento.finalizarRegistro(politica, AHORA.plusSeconds(5));
        alojamiento.agregarUnidad(unidadDatos(null, EstadoUnidad.BORRADOR), AHORA.plusSeconds(8));
        UUID unidadId = alojamiento.unidades().getFirst().id();

        alojamiento.enviarUnidadARevision(unidadId, AHORA.plusSeconds(10));

        assertEquals(EstadoUnidad.EN_REVISION, alojamiento.unidades().getFirst().estado());
        assertEquals(EstadoPublicacion.ACTIVO, alojamiento.estado());
    }

    @Test
    void hotelActivoEnviaHabitacionARevisionSinCambiarEstablecimiento() {
        Alojamiento alojamiento = borrador();
        completarEstablecimiento(alojamiento);
        var politica = PoliticaCicloPublicacion.desdeNombreTipo("Hostal");

        alojamiento.finalizarRegistro(politica, AHORA.plusSeconds(5));
        alojamiento.agregarUnidad(unidadDatos(null, EstadoUnidad.BORRADOR), AHORA.plusSeconds(8));
        UUID unidadId = alojamiento.unidades().getFirst().id();

        alojamiento.enviarUnidadARevision(unidadId, AHORA.plusSeconds(15));

        assertEquals(EstadoPublicacion.ACTIVO, alojamiento.estado());
        assertEquals(EstadoUnidad.EN_REVISION, alojamiento.unidades().getFirst().estado());
    }

    @Test
    void hotelNoPuedeEnviarAlojamientoARevision() {
        Alojamiento alojamiento = borrador();
        completarEstablecimiento(alojamiento);
        var politica = PoliticaCicloPublicacion.desdeNombreTipo("Hotel");
        alojamiento.finalizarRegistro(politica, AHORA.plusSeconds(5));
        alojamiento.agregarUnidad(unidadDatos(null, EstadoUnidad.BORRADOR), AHORA.plusSeconds(8));

        assertThrows(
                ReglaNegocioException.class,
                () -> alojamiento.enviarARevision(politica, AHORA.plusSeconds(15)));
        assertEquals(EstadoPublicacion.ACTIVO, alojamiento.estado());
    }

    @Test
    void hotelNoPuedeFinalizarSiFaltaEstablecimiento() {
        Alojamiento alojamiento = borrador();
        var politica = PoliticaCicloPublicacion.desdeNombreTipo("Hotel");

        assertThrows(
                ReglaNegocioException.class,
                () -> alojamiento.finalizarRegistro(politica, AHORA));
        assertEquals(EstadoPublicacion.BORRADOR, alojamiento.estado());
    }

    @Test
    void noSePuedeEnviarARevisionSiFaltaCompletar() {
        Alojamiento alojamiento = borrador();

        assertThrows(ReglaNegocioException.class, () -> alojamiento.enviarARevision(AHORA));
        assertEquals(EstadoPublicacion.BORRADOR, alojamiento.estado());
    }

    @Test
    void casaDesactivarYActivar() {
        Alojamiento alojamiento = borrador();
        completar(alojamiento);
        var politica = PoliticaCicloPublicacion.OfertaUnica.INSTANCE;
        alojamiento.enviarARevision(politica, AHORA.plusSeconds(5));
        alojamiento.aceptar(moderador, AHORA.plusSeconds(10));

        alojamiento.desactivar(politica, AHORA.plusSeconds(20));
        assertEquals(EstadoPublicacion.DESACTIVADO, alojamiento.estado());

        alojamiento.activar(politica, AHORA.plusSeconds(30));
        assertEquals(EstadoPublicacion.PUBLICADO, alojamiento.estado());
    }

    @Test
    void hotelDesactivarCascadaHabitacionesPublicadas() {
        Alojamiento alojamiento = borrador();
        completarEstablecimiento(alojamiento);
        var politica = PoliticaCicloPublicacion.Establecimiento.INSTANCE;
        alojamiento.finalizarRegistro(politica, AHORA.plusSeconds(5));
        alojamiento.agregarUnidad(unidadDatos(null, EstadoUnidad.BORRADOR), AHORA.plusSeconds(8));
        UUID unidadId = alojamiento.unidades().getFirst().id();
        alojamiento.enviarUnidadARevision(unidadId, AHORA.plusSeconds(10));
        alojamiento.aceptarUnidad(unidadId, moderador, AHORA.plusSeconds(15));
        assertEquals(EstadoUnidad.PUBLICADO, alojamiento.unidades().getFirst().estado());

        alojamiento.desactivar(politica, AHORA.plusSeconds(20));

        assertEquals(EstadoPublicacion.DESACTIVADO, alojamiento.estado());
        assertEquals(EstadoUnidad.DESACTIVADO, alojamiento.unidades().getFirst().estado());
        assertEquals("ESTABLECIMIENTO", alojamiento.unidades().getFirst().motivoVigente());

        alojamiento.activar(politica, AHORA.plusSeconds(30));
        assertEquals(EstadoPublicacion.ACTIVO, alojamiento.estado());
        assertEquals(EstadoUnidad.PUBLICADO, alojamiento.unidades().getFirst().estado());
    }

    @Test
    void guardaPoliticasDelAlojamiento() {
        Alojamiento alojamiento = borrador();

        alojamiento.guardarPoliticas(new Politicas("Check-in desde las 15:00 y no se admiten mascotas."));

        assertEquals("Check-in desde las 15:00 y no se admiten mascotas.", alojamiento.politicas().valor());
    }

    @Test
    void laOfertaUnicaCreaUnaSolaUnidadConInventarioUno() {
        Alojamiento alojamiento = borrador();

        alojamiento.guardarOfertaUnica(unidadDatos(null, EstadoUnidad.BORRADOR, 1), AHORA);

        assertEquals(1, alojamiento.unidades().size());
        assertEquals(1, alojamiento.unidades().getFirst().inventarioTotal());
        assertEquals(EstadoUnidad.BORRADOR, alojamiento.unidades().getFirst().estado());
    }

    @Test
    void laOfertaUnicaRechazaInventarioDistintoDeUno() {
        Alojamiento alojamiento = borrador();

        ReglaNegocioException error = assertThrows(ReglaNegocioException.class, () -> alojamiento.guardarOfertaUnica(
                unidadDatos(null, EstadoUnidad.BORRADOR, 3), AHORA));

        assertEquals("DATO_INVALIDO", error.codigo());
        assertTrue(alojamiento.unidades().isEmpty());
    }

    @Test
    void laUnidadNaceConHistorialYPuedeTenerFotosYServiciosPropios() {
        Alojamiento alojamiento = borrador();

        alojamiento.agregarUnidad(new DatosUnidad(
                null,
                new TipoEspacioId(1),
                "Suite doble",
                new DescripcionUnidad("Suite doble con cama queen y bano privado."),
                2,
                1,
                1,
                new BigDecimal("1.0"),
                10,
                new PrecioNoche(new BigDecimal("200.00")),
                EstadoUnidad.BORRADOR,
                List.of(new DatosFotografia(null, "alojate/suite", "https://cdn.alojate.bo/suite.jpg", 1)),
                Set.of(new ServicioId(1))), AHORA);

        var unidad = alojamiento.unidades().getFirst();
        assertEquals(1, unidad.historial().size());
        assertEquals("BORRADOR", unidad.historial().getFirst().estado());
        assertEquals(anfitrion.valor(), unidad.historial().getFirst().actorId());
        assertEquals(1, unidad.fotografias().size());
        assertEquals(Set.of(new ServicioId(1)), unidad.servicios());
    }

    @Test
    void alEnviarHabitacionARevisionSeAgregaHistorial() {
        Alojamiento alojamiento = borrador();
        completarEstablecimiento(alojamiento);
        var politica = PoliticaCicloPublicacion.Establecimiento.INSTANCE;
        alojamiento.finalizarRegistro(politica, AHORA.plusSeconds(5));
        alojamiento.agregarUnidad(unidadDatos(null, EstadoUnidad.BORRADOR), AHORA.plusSeconds(8));
        UUID unidadId = alojamiento.unidades().getFirst().id();

        alojamiento.enviarUnidadARevision(unidadId, AHORA.plusSeconds(30));

        var historial = alojamiento.unidades().getFirst().historial();
        assertEquals(2, historial.size());
        assertEquals("BORRADOR", historial.getFirst().estado());
        assertEquals("EN_REVISION", historial.getLast().estado());
    }

    private Alojamiento borrador() {
        return Alojamiento.registrar(anfitrion, new TipoPropiedadId(1), AHORA);
    }

    private void completar(Alojamiento alojamiento) {
        completarEstablecimiento(alojamiento);
        alojamiento.guardarOfertaUnica(unidadDatos(null, EstadoUnidad.BORRADOR, 1), AHORA);
    }

    private void completarEstablecimiento(Alojamiento alojamiento) {
        alojamiento.guardarUbicacion(new Ubicacion(
                "Calle Comercio 100, zona central",
                "La Paz",
                "la paz",
                new BigDecimal("-16.5"),
                new BigDecimal("-68.15")));
        alojamiento.guardarPresentacion(
                new Titulo("Casa en Sopocachi"), new DescripcionAlojamiento("Casa con patio y vista a la montana."));
        alojamiento.guardarServicios(Set.of(new ServicioId(1)));
        alojamiento.guardarPoliticas(new Politicas("Cancelacion gratuita hasta 24 horas antes."));
        alojamiento.guardarHorarios(new HorarioEstadia("15:00"), new HorarioEstadia("11:00"));
        alojamiento.guardarFotografias(List.of(
                new DatosFotografia(null, "alojate/casa", "https://cdn.alojate.bo/casa.jpg", 1)));
    }

    private DatosUnidad unidadDatos(UUID id, EstadoUnidad estado) {
        return unidadDatos(id, estado, 5);
    }

    private DatosUnidad unidadDatos(UUID id, EstadoUnidad estado, int inventario) {
        return new DatosUnidad(
                id,
                new TipoEspacioId(1),
                inventario == 1 ? "Casa completa" : "Habitación doble",
                new DescripcionUnidad("Unidad de prueba con baño privado."),
                2,
                1,
                1,
                new BigDecimal("1.0"),
                inventario,
                new PrecioNoche(new BigDecimal("180.00")),
                estado,
                List.of(),
                Set.of());
    }
}
