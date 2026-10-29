package com.alojate.alojamientos.application.services.administracion;

import com.alojate.alojamientos.application.dto.DecidirPublicacionCommand;
import com.alojate.alojamientos.application.ports.output.AccesoAdministrador;
import com.alojate.alojamientos.application.ports.output.NombreAnfitrion;
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
import java.util.Collection;
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

class AdministrarAlojamientosServiceTest {

    private static final Instant AHORA = Instant.parse("2026-10-23T16:00:00Z");
    private final UUID administrador = UUID.randomUUID();
    private final UUID anfitrion = UUID.randomUUID();
    private final AlojamientosEnMemoria alojamientos = new AlojamientosEnMemoria();
    private final CatalogoFijo catalogo = new CatalogoFijo();
    private final AdministrarAlojamientosService servicio = new AdministrarAlojamientosService(
            alojamientos,
            catalogo,
            new AccesoFijo(administrador),
            new PresentadorModeracion(catalogo, new NombresFijos(anfitrion, "Ana Perez")),
            () -> AHORA.plusSeconds(60),
            new UnitOfWork() {
                @Override
                public <T> T ejecutar(Supplier<T> trabajo) {
                    return trabajo.get();
                }
            });

    @Test
    void listaTodosLosEstadosYElDetalleTraeAlAnfitrion() {
        Alojamiento revision = alojamientos.guardar(oferta(EstadoPublicacion.EN_REVISION));
        alojamientos.guardar(oferta(EstadoPublicacion.BORRADOR));

        var lista = servicio.listar(administrador);
        var detalle = servicio.obtener(administrador, revision.id().valor());

        assertEquals(2, lista.size());
        assertTrue(lista.stream().anyMatch(item -> "BORRADOR".equals(item.estado())));
        assertEquals("Ana Perez", detalle.nombreAnfitrion());
        assertEquals("EN_REVISION", detalle.estado());
    }

    @Test
    void aceptarPublicaYRechazarGuardaElMotivo() {
        Alojamiento revision = alojamientos.guardar(oferta(EstadoPublicacion.EN_REVISION));

        var aceptado = servicio.aceptar(administrador, revision.id().valor());
        assertEquals("PUBLICADO", aceptado.estado());
        assertEquals(administrador, aceptado.historial().getLast().actorId());

        Alojamiento otro = alojamientos.guardar(oferta(EstadoPublicacion.EN_REVISION));
        var rechazado = servicio.rechazar(
                new DecidirPublicacionCommand(administrador, otro.id().valor(), "Fotos borrosas"));
        assertEquals("RECHAZADO", rechazado.estado());
        assertEquals("Fotos borrosas", rechazado.motivo());
    }

    @Test
    void sinPermisoOSinAlojamientoNoModera() {
        ReglaNegocioException permiso = assertThrows(
                ReglaNegocioException.class, () -> servicio.listar(UUID.randomUUID()));
        assertEquals("PERMISO_DENEGADO", permiso.codigo());

        ReglaNegocioException ausente = assertThrows(
                ReglaNegocioException.class, () -> servicio.aceptar(administrador, UUID.randomUUID()));
        assertEquals("ALOJAMIENTO_NO_ENCONTRADO", ausente.codigo());
    }

    private Alojamiento oferta(EstadoPublicacion estado) {
        return Alojamiento.reconstituir(
                AlojamientoId.nuevo(),
                new AnfitrionId(anfitrion),
                new TipoPropiedadId(1),
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
                AHORA,
                List.of(Fotografia.reconstituir(
                        UUID.randomUUID(), "casa-1", "https://ejemplo.com/casa.jpg", 1)),
                Set.of(),
                List.of(UnidadReservable.reconstituir(
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
                        List.of())),
                List.of(HistorialPublicacion.reconstituir(
                        UUID.randomUUID(), estado.name(), null, anfitrion, AHORA)));
    }

    private static final class AccesoFijo implements AccesoAdministrador {
        private final UUID administrador;

        private AccesoFijo(UUID administrador) {
            this.administrador = administrador;
        }

        @Override
        public void exigirPuedeModerar(UUID administradorId) {
            if (!administrador.equals(administradorId)) {
                throw new ReglaNegocioException("PERMISO_DENEGADO", "La cuenta no puede moderar alojamientos");
            }
        }
    }

    private static final class NombresFijos implements NombreAnfitrion {
        private final UUID anfitrion;
        private final String nombre;

        private NombresFijos(UUID anfitrion, String nombre) {
            this.anfitrion = anfitrion;
            this.nombre = nombre;
        }

        @Override
        public Map<UUID, String> de(Collection<UUID> anfitriones) {
            Map<UUID, String> nombres = new HashMap<>();
            if (anfitriones.contains(anfitrion)) {
                nombres.put(anfitrion, nombre);
            }
            return nombres;
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
            return true;
        }

        @Override
        public boolean existeTipoEspacio(TipoEspacioId id) {
            return true;
        }

        @Override
        public boolean existenServicios(Set<ServicioId> ids) {
            return true;
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
            return List.of();
        }

        @Override
        public List<Alojamiento> listarPublicados() {
            return List.of();
        }

        @Override
        public List<Alojamiento> listarTodos() {
            return new ArrayList<>(porId.values());
        }
    }
}
