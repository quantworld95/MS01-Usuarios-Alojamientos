package com.alojate.alojamientos.application.services.registro;

import com.alojate.alojamientos.application.dto.AlojamientoRegistradoResponse;
import com.alojate.alojamientos.application.dto.GuardarFotografiasCommand;
import com.alojate.alojamientos.application.dto.AgregarUnidadCommand;
import com.alojate.alojamientos.application.dto.GuardarOfertaUnicaCommand;
import com.alojate.alojamientos.application.dto.GuardarUbicacionCommand;
import com.alojate.alojamientos.application.dto.PublicarFotografiaCommand;
import com.alojate.alojamientos.application.dto.RegistrarAlojamientoCommand;
import com.alojate.alojamientos.application.ports.output.AccesoAnfitrion;
import com.alojate.alojamientos.application.services.AlojamientosDelAnfitrion;
import com.alojate.alojamientos.application.services.gestion.GestionarMisAlojamientosService;
import com.alojate.alojamientos.application.ports.output.AlmacenDeImagenes;
import com.alojate.alojamientos.application.ports.output.ContenidoImagen;
import com.alojate.alojamientos.application.ports.output.Geocodificador;
import com.alojate.alojamientos.application.ports.output.ImagenAlmacenada;
import com.alojate.alojamientos.application.ports.output.LugarGeocodificado;
import com.alojate.alojamientos.application.ports.output.UnitOfWork;
import com.alojate.alojamientos.domain.alojamiento.aggregate.Alojamiento;
import com.alojate.alojamientos.domain.alojamiento.repository.AlojamientoRepository;
import com.alojate.alojamientos.domain.alojamiento.valueobject.AlojamientoId;
import com.alojate.alojamientos.domain.alojamiento.valueobject.AnfitrionId;
import com.alojate.alojamientos.domain.alojamiento.valueobject.EstadoPublicacion;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegistrarAlojamientoServiceTest {

    private static final Instant AHORA = Instant.parse("2026-10-23T16:00:00Z");
    private final UUID anfitrion = UUID.randomUUID();
    private final AlojamientosEnMemoria alojamientos = new AlojamientosEnMemoria();
    private final AlmacenFijo almacen = new AlmacenFijo();
    private final CatalogoFijo catalogo = new CatalogoFijo();
    private final UnitOfWork unitOfWork = new UnitOfWork() {
        @Override
        public <T> T ejecutar(Supplier<T> trabajo) {
            return trabajo.get();
        }
    };
    private final AlojamientosDelAnfitrion propios = new AlojamientosDelAnfitrion(alojamientos, new AccesoFijo(anfitrion));
    private final PresentadorAlojamiento presentador = new PresentadorAlojamiento(catalogo);
    private final RegistrarAlojamientoService servicio = new RegistrarAlojamientoService(
            alojamientos,
            catalogo,
            propios,
            almacen,
            new CompletarUbicacion(new MapaFijo()),
            presentador,
            () -> AHORA,
            unitOfWork);
    private final GestionarMisAlojamientosService gestion = new GestionarMisAlojamientosService(
            alojamientos, catalogo, propios, presentador, () -> AHORA, unitOfWork);

    @Test
    void registraUnBorradorConElTipoYElHistorial() {
        var respuesta = servicio.registrar(new RegistrarAlojamientoCommand(anfitrion, 1));

        assertEquals("BORRADOR", respuesta.estado());
        assertEquals("Casa", respuesta.tipoPropiedad());
        assertNull(respuesta.titulo());
        assertFalse(respuesta.listoParaRevision());
        assertEquals(1, respuesta.historial().size());
        assertEquals(anfitrion, respuesta.historial().getFirst().actorId());
    }

    @Test
    void guardaLaUbicacionDelMismoAnfitrion() {
        var creado = servicio.registrar(new RegistrarAlojamientoCommand(anfitrion, 1));

        var respuesta = servicio.guardarUbicacion(new GuardarUbicacionCommand(
                anfitrion,
                creado.id(),
                "Calle Comercio 100, zona central",
                "La Paz",
                "La Paz",
                new BigDecimal("-16.5"),
                new BigDecimal("-68.15")));

        assertEquals("La Paz", respuesta.ciudad());
        assertEquals("BORRADOR", respuesta.estado());
        assertEquals(0, new BigDecimal("-16.5").compareTo(respuesta.latitud()));
        assertFalse(respuesta.listoParaRevision());
    }

    @Test
    void completaLasCoordenadasAPartirDeLaDireccion() {
        var creado = servicio.registrar(new RegistrarAlojamientoCommand(anfitrion, 1));

        var respuesta = servicio.guardarUbicacion(new GuardarUbicacionCommand(
                anfitrion, creado.id(), "Calle Comercio 100, zona central", "La Paz", "La Paz", null, null));

        assertEquals("Calle Comercio 100, zona central", respuesta.direccion());
        assertEquals(0, new BigDecimal("-16.100000").compareTo(respuesta.latitud()));
        assertEquals(0, new BigDecimal("-68.100000").compareTo(respuesta.longitud()));
    }

    @Test
    void completaLaDireccionAPartirDelPunto() {
        var creado = servicio.registrar(new RegistrarAlojamientoCommand(anfitrion, 1));

        var respuesta = servicio.guardarUbicacion(new GuardarUbicacionCommand(
                anfitrion,
                creado.id(),
                null,
                null,
                null,
                new BigDecimal("-17.783300"),
                new BigDecimal("-63.182100")));

        assertEquals("Avenida Busch 50", respuesta.direccion());
        assertEquals("Santa Cruz", respuesta.ciudad());
        assertEquals("Santa Cruz", respuesta.departamento());
        assertEquals(0, new BigDecimal("-17.783300").compareTo(respuesta.latitud()));
    }

    @Test
    void guardaOfertaUnicaConHorariosYPoliticas() {
        var creado = servicio.registrar(new RegistrarAlojamientoCommand(anfitrion, 1));

        var respuesta = servicio.guardarOfertaUnica(new GuardarOfertaUnicaCommand(
                anfitrion,
                creado.id(),
                4,
                2,
                2,
                new BigDecimal("1.5"),
                new BigDecimal("350.00"),
                "No se admiten fiestas.",
                "15:00",
                "11:00"));

        assertEquals(1, respuesta.unidades().size());
        assertEquals(1, respuesta.unidades().getFirst().inventarioTotal());
        assertEquals("No se admiten fiestas.", respuesta.politicas());
        assertEquals("15:00", respuesta.horarioEntrada());
        assertEquals("11:00", respuesta.horarioSalida());
    }

    @Test
    void rechazaAgregarHabitacionEnCasaODepartamento() {
        var creado = servicio.registrar(new RegistrarAlojamientoCommand(anfitrion, 1));

        ReglaNegocioException error = assertThrows(
                ReglaNegocioException.class,
                () -> servicio.agregarUnidad(new AgregarUnidadCommand(
                        anfitrion,
                        creado.id(),
                        1,
                        "Suite",
                        "Suite con vista y baño privado.",
                        2,
                        1,
                        1,
                        new BigDecimal("1.0"),
                        1,
                        new BigDecimal("200.00"),
                        List.of(),
                        List.of())));

        assertEquals("DATO_INVALIDO", error.codigo());
    }

    @Test
    void rechazaAQuienNoPuedePublicar() {
        RegistrarAlojamientoCommand comando = new RegistrarAlojamientoCommand(UUID.randomUUID(), 1);

        ReglaNegocioException error = assertThrows(ReglaNegocioException.class, () -> servicio.registrar(comando));

        assertEquals("PERMISO_DENEGADO", error.codigo());
    }

    @Test
    void noMuestraElBorradorDeOtroAnfitrion() {
        var creado = servicio.registrar(new RegistrarAlojamientoCommand(anfitrion, 1));

        ReglaNegocioException error = assertThrows(
                ReglaNegocioException.class,
                () -> gestion.obtener(anfitrion, UUID.randomUUID()));

        assertEquals("ALOJAMIENTO_NO_ENCONTRADO", error.codigo());
    }

    @Test
    void publicarGuardaLaUrlYElIdentificadorDelArchivo() {
        var creado = servicio.registrar(new RegistrarAlojamientoCommand(anfitrion, 1));

        AlojamientoRegistradoResponse guardado = servicio.publicarFotografia(
                new PublicarFotografiaCommand(anfitrion, creado.id(), new byte[] {1, 2, 3}, "image/jpeg"));

        assertEquals(1, guardado.fotografias().size());
        assertEquals("alojate/casa", guardado.fotografias().getFirst().identificadorAlmacen());
        assertEquals("https://cdn.alojate.bo/casa.jpg", guardado.fotografias().getFirst().url());
        assertTrue(almacen.retirados.isEmpty());
    }

    @Test
    void alQuitarLaFotoLaRetiraDelAlmacen() {
        var creado = servicio.registrar(new RegistrarAlojamientoCommand(anfitrion, 1));
        var publicada = servicio.publicarFotografia(
                new PublicarFotografiaCommand(anfitrion, creado.id(), new byte[] {1, 2, 3}, "image/jpeg"));

        servicio.guardarFotografias(new GuardarFotografiasCommand(anfitrion, creado.id(), List.of()));

        assertEquals(List.of(publicada.fotografias().getFirst().identificadorAlmacen()), almacen.retirados);
    }

    private static final class MapaFijo implements Geocodificador {
        @Override
        public LugarGeocodificado porDireccion(String direccion, String ciudad, String departamento) {
            return new LugarGeocodificado(
                    direccion, "La Paz", "La Paz", new BigDecimal("-16.100000"), new BigDecimal("-68.100000"));
        }

        @Override
        public LugarGeocodificado porCoordenadas(BigDecimal latitud, BigDecimal longitud) {
            return new LugarGeocodificado(
                    "Avenida Busch 50", "Santa Cruz", "Santa Cruz", latitud, longitud);
        }
    }

    private static final class AlmacenFijo implements AlmacenDeImagenes {
        private final List<String> retirados = new ArrayList<>();

        @Override
        public ImagenAlmacenada publicar(ContenidoImagen contenido) {
            return new ImagenAlmacenada("alojate/casa", "https://cdn.alojate.bo/casa.jpg");
        }

        @Override
        public void retirar(String identificadorAlmacen) {
            retirados.add(identificadorAlmacen);
        }
    }

    private static final class AccesoFijo implements AccesoAnfitrion {
        private final UUID permitido;

        private AccesoFijo(UUID permitido) {
            this.permitido = permitido;
        }

        @Override
        public void exigirPuedeRegistrar(UUID anfitrionId) {
            if (!permitido.equals(anfitrionId)) {
                throw new ReglaNegocioException("PERMISO_DENEGADO", "La cuenta no puede registrar alojamientos");
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
            return porId.values().stream()
                    .filter(alojamiento -> alojamiento.estado() == EstadoPublicacion.PUBLICADO)
                    .toList();
        }

        @Override
        public List<Alojamiento> listarTodos() {
            return new ArrayList<>(porId.values());
        }
    }
}
