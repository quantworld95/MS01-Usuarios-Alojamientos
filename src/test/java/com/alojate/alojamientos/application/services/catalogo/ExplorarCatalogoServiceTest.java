package com.alojate.alojamientos.application.services.catalogo;

import com.alojate.alojamientos.application.ports.output.AccesoHuesped;
import com.alojate.alojamientos.application.ports.output.UnitOfWork;
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
import com.alojate.alojamientos.domain.catalogo.aggregate.Servicio;
import com.alojate.alojamientos.domain.catalogo.aggregate.TipoEspacio;
import com.alojate.alojamientos.domain.catalogo.aggregate.TipoPropiedad;
import com.alojate.alojamientos.domain.catalogo.repository.CatalogoAlojamientoRepository;
import com.alojate.alojamientos.domain.catalogo.valueobject.ServicioId;
import com.alojate.alojamientos.domain.catalogo.valueobject.TipoEspacioId;
import com.alojate.alojamientos.domain.catalogo.valueobject.TipoPropiedadId;
import com.alojate.alojamientos.domain.exception.ReglaNegocioException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExplorarCatalogoServiceTest {

    private static final Instant AHORA = Instant.parse("2026-10-23T16:00:00Z");
    private final UUID huesped = UUID.randomUUID();
    private final UUID anfitrion = UUID.randomUUID();
    private final AlojamientosEnMemoria alojamientos = new AlojamientosEnMemoria();
    private final ExplorarCatalogoService servicio = new ExplorarCatalogoService(
            alojamientos,
            new AccesoFijo(huesped),
            new PresentadorCatalogo(new CatalogoFijo()),
            new UnitOfWork() {
                @Override
                public <T> T ejecutar(Supplier<T> trabajo) {
                    return trabajo.get();
                }
            });

    @Test
    void listaSoloLoPublicado() {
        Alojamiento publicado = alojamientos.guardar(oferta(EstadoPublicacion.PUBLICADO, "Casa del lago"));
        alojamientos.guardar(oferta(EstadoPublicacion.BORRADOR, "Borrador oculto"));
        alojamientos.guardar(oferta(EstadoPublicacion.DESACTIVADO, "Casa desactivada"));

        var lista = servicio.listar(huesped);

        assertEquals(1, lista.size());
        assertEquals(publicado.id().valor(), lista.getFirst().id());
        assertEquals("Casa del lago", lista.getFirst().titulo());
        assertEquals("La Paz", lista.getFirst().ciudad());
        assertEquals("Casa", lista.getFirst().tipoPropiedad());
    }

    @Test
    void elDetalleMuestraLaOfertaSinUnidadesInactivas() {
        Alojamiento publicado = alojamientos.guardar(oferta(EstadoPublicacion.PUBLICADO, "Casa del lago"));

        var detalle = servicio.obtener(huesped, publicado.id().valor());

        assertEquals("Casa con vista al lago y desayuno", detalle.descripcion());
        assertEquals(1, detalle.fotografias().size());
        assertEquals("https://ejemplo.com/casa.jpg", detalle.fotografias().getFirst().url());
        assertEquals(1, detalle.servicios().size());
        assertEquals("Wifi", detalle.servicios().getFirst().nombre());
        assertEquals(1, detalle.unidades().size());
        assertEquals("Suite", detalle.unidades().getFirst().nombre());
        assertEquals(0, new BigDecimal("250.00").compareTo(detalle.unidades().getFirst().precioNoche()));
    }

    @Test
    void unBorradorNoSePuedeAbrirDesdeElCatalogo() {
        Alojamiento borrador = alojamientos.guardar(oferta(EstadoPublicacion.BORRADOR, "Borrador oculto"));

        ReglaNegocioException error = assertThrows(
                ReglaNegocioException.class, () -> servicio.obtener(huesped, borrador.id().valor()));

        assertEquals("ALOJAMIENTO_NO_ENCONTRADO", error.codigo());
    }

    @Test
    void elDetalleDeHabitacionSeObtienePorSeparado() {
        Alojamiento publicado = alojamientos.guardar(oferta(EstadoPublicacion.PUBLICADO, "Casa del lago"));
        UUID unidadId = publicado.unidades().stream()
                .filter(unidad -> unidad.estado() == EstadoUnidad.BORRADOR)
                .findFirst()
                .orElseThrow()
                .id();

        var habitacion = servicio.obtenerUnidad(huesped, publicado.id().valor(), unidadId);

        assertEquals("Suite", habitacion.nombre());
        assertEquals("Suite con vista y bano privado.", habitacion.descripcion());
        assertEquals(0, new BigDecimal("250.00").compareTo(habitacion.precioNoche()));

        ReglaNegocioException ausente = assertThrows(
                ReglaNegocioException.class,
                () -> servicio.obtenerUnidad(huesped, publicado.id().valor(), UUID.randomUUID()));
        assertEquals("UNIDAD_NO_ENCONTRADA", ausente.codigo());
    }

    @Test
    void sinPermisoNoConsultaElCatalogo() {
        ReglaNegocioException error = assertThrows(
                ReglaNegocioException.class, () -> servicio.listar(UUID.randomUUID()));

        assertEquals("PERMISO_DENEGADO", error.codigo());
        assertTrue(servicio.listar(huesped).isEmpty());
    }

    private Alojamiento oferta(EstadoPublicacion estado, String titulo) {
        UUID id = UUID.randomUUID();
        return Alojamiento.reconstituir(
                new AlojamientoId(id),
                new AnfitrionId(anfitrion),
                new TipoPropiedadId(1),
                new Titulo(titulo),
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
                estado,
                AHORA,
                List.of(Fotografia.reconstituir(
                        UUID.randomUUID(), "casa-1", "https://ejemplo.com/casa.jpg", 1)),
                Set.of(new ServicioId(1)),
                List.of(
                        UnidadReservable.reconstituir(
                                UUID.randomUUID(),
                                new TipoEspacioId(1),
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
                                new TipoEspacioId(1),
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
                        UUID.randomUUID(), estado.name(), null, anfitrion, AHORA)));
    }

    private static final class AccesoFijo implements AccesoHuesped {
        private final UUID huesped;

        private AccesoFijo(UUID huesped) {
            this.huesped = huesped;
        }

        @Override
        public void exigirPuedeBuscar(UUID huespedId) {
            if (!huesped.equals(huespedId)) {
                throw new ReglaNegocioException("PERMISO_DENEGADO", "La cuenta no puede buscar alojamientos");
            }
        }
    }

    private static final class CatalogoFijo implements CatalogoAlojamientoRepository {
        @Override
        public List<TipoPropiedad> listarTiposPropiedad() {
            return List.of(TipoPropiedad.reconstituir(1, "Casa"));
        }

        @Override
        public List<TipoEspacio> listarTiposEspacio() {
            return List.of(TipoEspacio.reconstituir(1, "Alojamiento completo"));
        }

        @Override
        public List<Servicio> listarServicios() {
            return List.of(Servicio.reconstituir(1, "Wifi", "Conectividad"));
        }

        @Override
        public boolean existeTipoPropiedad(TipoPropiedadId id) {
            return id.valor() == 1;
        }

        @Override
        public boolean existeTipoEspacio(TipoEspacioId id) {
            return id.valor() == 1;
        }

        @Override
        public boolean existenServicios(Set<ServicioId> ids) {
            return ids.stream().allMatch(id -> id.valor() == 1);
        }
    }

    private static final class AlojamientosEnMemoria implements AlojamientoRepository {
        private final Map<UUID, Alojamiento> porId = new HashMap<>();

        @Override
        public Alojamiento guardar(Alojamiento alojamiento) {
            porId.put(alojamiento.id().valor(), alojamiento);
            return alojamiento;
        }

        @Override
        public Optional<Alojamiento> buscarPorId(AlojamientoId id) {
            return Optional.ofNullable(porId.get(id.valor()));
        }

        @Override
        public List<Alojamiento> listarPorAnfitrion(AnfitrionId anfitrionId) {
            return new ArrayList<>(porId.values().stream()
                    .filter(alojamiento -> alojamiento.anfitrionId().equals(anfitrionId))
                    .toList());
        }

        @Override
        public List<Alojamiento> listarPublicados() {
            return porId.values().stream().filter(Alojamiento::visibleEnCatalogo).toList();
        }

        @Override
        public List<Alojamiento> listarTodos() {
            return new ArrayList<>(porId.values());
        }
    }
}
