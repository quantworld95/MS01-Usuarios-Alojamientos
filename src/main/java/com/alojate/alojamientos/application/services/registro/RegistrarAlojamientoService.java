package com.alojate.alojamientos.application.services.registro;

import com.alojate.alojamientos.application.dto.AgregarUnidadCommand;
import com.alojate.alojamientos.application.dto.AlojamientoRegistradoResponse;
import com.alojate.alojamientos.application.dto.FinalizarRegistroCommand;
import com.alojate.alojamientos.application.dto.GuardarFotografiasCommand;
import com.alojate.alojamientos.application.dto.GuardarOfertaUnicaCommand;
import com.alojate.alojamientos.application.dto.GuardarPoliticasCommand;
import com.alojate.alojamientos.application.dto.GuardarPresentacionCommand;
import com.alojate.alojamientos.application.dto.GuardarServiciosCommand;
import com.alojate.alojamientos.application.dto.GuardarUbicacionCommand;
import com.alojate.alojamientos.application.dto.ImagenPublicadaResponse;
import com.alojate.alojamientos.application.dto.PublicarFotografiaCommand;
import com.alojate.alojamientos.application.dto.RegistrarAlojamientoCommand;
import com.alojate.alojamientos.application.ports.input.RegistrarAlojamiento;
import com.alojate.alojamientos.application.ports.output.AlmacenDeImagenes;
import com.alojate.alojamientos.application.ports.output.ContenidoImagen;
import com.alojate.alojamientos.application.ports.output.ImagenAlmacenada;
import com.alojate.alojamientos.application.ports.output.Reloj;
import com.alojate.alojamientos.application.ports.output.UnitOfWork;
import com.alojate.alojamientos.application.services.AlojamientosDelAnfitrion;
import com.alojate.alojamientos.domain.alojamiento.DatosFotografia;
import com.alojate.alojamientos.domain.alojamiento.DatosUnidad;
import com.alojate.alojamientos.domain.alojamiento.aggregate.Alojamiento;
import com.alojate.alojamientos.domain.alojamiento.aggregate.Fotografia;
import com.alojate.alojamientos.domain.alojamiento.aggregate.UnidadReservable;
import com.alojate.alojamientos.domain.alojamiento.repository.AlojamientoRepository;
import com.alojate.alojamientos.domain.alojamiento.valueobject.AnfitrionId;
import com.alojate.alojamientos.domain.alojamiento.valueobject.DescripcionAlojamiento;
import com.alojate.alojamientos.domain.alojamiento.valueobject.DescripcionUnidad;
import com.alojate.alojamientos.domain.alojamiento.valueobject.EstadoUnidad;
import com.alojate.alojamientos.domain.alojamiento.valueobject.HorarioEstadia;
import com.alojate.alojamientos.domain.alojamiento.valueobject.Politicas;
import com.alojate.alojamientos.domain.alojamiento.valueobject.PrecioNoche;
import com.alojate.alojamientos.domain.alojamiento.valueobject.Titulo;
import com.alojate.alojamientos.domain.alojamiento.valueobject.Ubicacion;
import com.alojate.alojamientos.domain.catalogo.aggregate.TipoEspacio;
import com.alojate.alojamientos.domain.catalogo.repository.CatalogoAlojamientoRepository;
import com.alojate.alojamientos.domain.catalogo.valueobject.ServicioId;
import com.alojate.alojamientos.domain.catalogo.valueobject.TipoEspacioId;
import com.alojate.alojamientos.domain.catalogo.valueobject.TipoPropiedadId;
import com.alojate.alojamientos.domain.exception.ReglaNegocioException;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public final class RegistrarAlojamientoService implements RegistrarAlojamiento {

    private final AlojamientoRepository alojamientos;
    private final CatalogoAlojamientoRepository catalogos;
    private final AlojamientosDelAnfitrion propios;
    private final AlmacenDeImagenes almacen;
    private final CompletarUbicacion completarUbicacion;
    private final PresentadorAlojamiento presentador;
    private final Reloj reloj;
    private final UnitOfWork unitOfWork;

    public RegistrarAlojamientoService(
            AlojamientoRepository alojamientos,
            CatalogoAlojamientoRepository catalogos,
            AlojamientosDelAnfitrion propios,
            AlmacenDeImagenes almacen,
            CompletarUbicacion completarUbicacion,
            PresentadorAlojamiento presentador,
            Reloj reloj,
            UnitOfWork unitOfWork) {
        this.alojamientos = alojamientos;
        this.catalogos = catalogos;
        this.propios = propios;
        this.almacen = almacen;
        this.completarUbicacion = completarUbicacion;
        this.presentador = presentador;
        this.reloj = reloj;
        this.unitOfWork = unitOfWork;
    }

    @Override
    public AlojamientoRegistradoResponse registrar(RegistrarAlojamientoCommand comando) {
        AnfitrionId anfitrion = propios.exigir(comando.anfitrionId());
        TipoPropiedadId tipo = tipo(comando.tipoPropiedadId());
        exigirTipo(tipo);
        Alojamiento guardado = unitOfWork.ejecutar(() ->
                alojamientos.guardar(Alojamiento.registrar(anfitrion, tipo, reloj.ahora())));
        return presentador.detalle(guardado);
    }

    @Override
    public AlojamientoRegistradoResponse guardarUbicacion(GuardarUbicacionCommand comando) {
        Ubicacion ubicacion = completarUbicacion.completar(comando);
        return modificar(comando.anfitrionId(), comando.alojamientoId(), alojamiento ->
                alojamiento.guardarUbicacion(ubicacion));
    }

    @Override
    public AlojamientoRegistradoResponse guardarPresentacion(GuardarPresentacionCommand comando) {
        return modificar(comando.anfitrionId(), comando.alojamientoId(), alojamiento -> {
            alojamiento.guardarPresentacion(
                    new Titulo(comando.titulo()), new DescripcionAlojamiento(comando.descripcion()));
            aplicarPoliticasYHorarios(
                    alojamiento, comando.politicas(), comando.horarioEntrada(), comando.horarioSalida());
        });
    }

    @Override
    public AlojamientoRegistradoResponse guardarPoliticas(GuardarPoliticasCommand comando) {
        if (comando.politicas() == null
                || comando.horarioEntrada() == null
                || comando.horarioSalida() == null) {
            throw new ReglaNegocioException(
                    "DATO_INVALIDO", "Las políticas y los horarios de entrada y salida son obligatorios");
        }
        return modificar(comando.anfitrionId(), comando.alojamientoId(), alojamiento ->
                aplicarPoliticasYHorarios(
                        alojamiento, comando.politicas(), comando.horarioEntrada(), comando.horarioSalida()));
    }

    @Override
    public AlojamientoRegistradoResponse guardarServicios(GuardarServiciosCommand comando) {
        Set<ServicioId> servicios = exigirServicios(comando.servicios());
        return modificar(comando.anfitrionId(), comando.alojamientoId(), alojamiento ->
                alojamiento.guardarServicios(servicios));
    }

    @Override
    public AlojamientoRegistradoResponse guardarFotografias(GuardarFotografiasCommand comando) {
        List<DatosFotografia> fotos = fotosAlojamiento(comando.fotografias());
        AnfitrionId anfitrion = propios.exigir(comando.anfitrionId());
        return unitOfWork.ejecutar(() -> {
            Alojamiento alojamiento = propios.propio(anfitrion, comando.alojamientoId());
            Set<String> antes = identificadores(alojamiento.fotografias());
            alojamiento.guardarFotografias(fotos);
            Set<String> despues = identificadores(alojamiento.fotografias());
            List<String> retiradas = antes.stream().filter(id -> !despues.contains(id)).toList();
            Alojamiento guardado = alojamientos.guardar(alojamiento);
            AlojamientoRegistradoResponse respuesta = presentador.detalle(guardado);
            retiradas.forEach(almacen::retirar);
            return respuesta;
        });
    }

    @Override
    public AlojamientoRegistradoResponse publicarFotografia(PublicarFotografiaCommand comando) {
        propios.exigir(comando.anfitrionId());
        ImagenAlmacenada imagen = almacen.publicar(new ContenidoImagen(comando.contenido(), comando.tipoContenido()));
        try {
            return modificar(comando.anfitrionId(), comando.alojamientoId(), alojamiento ->
                    alojamiento.agregarFotografia(imagen.identificadorAlmacen(), imagen.url()));
        } catch (RuntimeException error) {
            almacen.retirar(imagen.identificadorAlmacen());
            throw error;
        }
    }

    @Override
    public ImagenPublicadaResponse publicarImagen(PublicarFotografiaCommand comando) {
        AnfitrionId anfitrion = propios.exigir(comando.anfitrionId());
        unitOfWork.ejecutar(() -> propios.propio(anfitrion, comando.alojamientoId()));
        ImagenAlmacenada imagen = almacen.publicar(new ContenidoImagen(comando.contenido(), comando.tipoContenido()));
        return new ImagenPublicadaResponse(imagen.identificadorAlmacen(), imagen.url());
    }

    @Override
    public AlojamientoRegistradoResponse agregarUnidad(AgregarUnidadCommand comando) {
        DatosUnidad datos = datosHabitacionNueva(comando);
        return modificar(comando.anfitrionId(), comando.alojamientoId(), alojamiento -> {
            exigirEstablecimiento(alojamiento);
            publicarHabitacion(alojamiento, datos);
        });
    }

    @Override
    public AlojamientoRegistradoResponse guardarOfertaUnica(GuardarOfertaUnicaCommand comando) {
        exigirOfertaCompleta(comando);
        return modificar(comando.anfitrionId(), comando.alojamientoId(), alojamiento -> {
            exigirOfertaUnica(alojamiento);
            alojamiento.guardarOfertaUnica(datosOfertaUnica(alojamiento, comando), reloj.ahora());
            aplicarPoliticasYHorarios(
                    alojamiento, comando.politicas(), comando.horarioEntrada(), comando.horarioSalida());
        });
    }

    @Override
    public AlojamientoRegistradoResponse finalizarRegistro(FinalizarRegistroCommand comando) {
        return modificar(comando.anfitrionId(), comando.alojamientoId(), alojamiento -> {
            var politica = ClasificacionRegistro.politica(catalogos, alojamiento.tipoPropiedadId());
            alojamiento.finalizarRegistro(politica, reloj.ahora());
        });
    }

    /** Crea la habitación y la envía a moderación (BORRADOR → EN_REVISION). */
    private void publicarHabitacion(Alojamiento alojamiento, DatosUnidad datos) {
        Set<UUID> idsPrevios = idsUnidades(alojamiento);
        Instant ahora = reloj.ahora();
        alojamiento.agregarUnidad(datos, ahora);
        UUID nuevaId = idsUnidades(alojamiento).stream()
                .filter(id -> !idsPrevios.contains(id))
                .findFirst()
                .orElseThrow(() -> new ReglaNegocioException(
                        "DATO_INVALIDO", "No se pudo identificar la habitación creada"));
        alojamiento.enviarUnidadARevision(nuevaId, ahora);
    }

    private DatosUnidad datosHabitacionNueva(AgregarUnidadCommand comando) {
        if (comando.tipoEspacioId() == null
                || comando.capacidadHuespedes() == null
                || comando.cantidadDormitorios() == null
                || comando.cantidadCamas() == null
                || comando.inventarioTotal() == null) {
            throw new ReglaNegocioException("DATO_INVALIDO", "La unidad está incompleta");
        }
        TipoEspacioId tipo = new TipoEspacioId(comando.tipoEspacioId());
        if (!catalogos.existeTipoEspacio(tipo)) {
            throw new ReglaNegocioException("CATALOGO_INVALIDO", "El tipo de espacio no existe");
        }
        return new DatosUnidad(
                null,
                tipo,
                comando.nombre(),
                new DescripcionUnidad(comando.descripcion()),
                comando.capacidadHuespedes(),
                comando.cantidadDormitorios(),
                comando.cantidadCamas(),
                comando.cantidadBanos(),
                comando.inventarioTotal(),
                new PrecioNoche(comando.precioNoche()),
                EstadoUnidad.BORRADOR,
                fotosUnidad(comando.fotografias()),
                exigirServicios(comando.servicios()));
    }

    private DatosUnidad datosOfertaUnica(Alojamiento alojamiento, GuardarOfertaUnicaCommand comando) {
        UnidadReservable actual = alojamiento.unidades().stream().findFirst().orElse(null);
        String nombre = alojamiento.titulo() == null ? "Oferta completa" : alojamiento.titulo().valor();
        DescripcionUnidad descripcion = actual == null
                ? new DescripcionUnidad("Oferta completa del alojamiento.")
                : actual.descripcion();
        return new DatosUnidad(
                actual == null ? null : actual.id(),
                tipoEspacioAlojamientoCompleto(),
                nombre,
                descripcion,
                comando.capacidadHuespedes(),
                comando.cantidadDormitorios(),
                comando.cantidadCamas(),
                comando.cantidadBanos(),
                1,
                new PrecioNoche(comando.precioNoche()),
                EstadoUnidad.BORRADOR,
                fotosDeUnidad(actual),
                actual == null ? Set.of() : actual.servicios());
    }

    private static List<DatosFotografia> fotosDeUnidad(UnidadReservable unidad) {
        if (unidad == null) {
            return List.of();
        }
        return unidad.fotografias().stream()
                .map(foto -> new DatosFotografia(
                        foto.id(), foto.identificadorAlmacen(), foto.url(), foto.orden()))
                .toList();
    }

    private static Set<UUID> idsUnidades(Alojamiento alojamiento) {
        return alojamiento.unidades().stream().map(UnidadReservable::id).collect(Collectors.toSet());
    }

    private void exigirEstablecimiento(Alojamiento alojamiento) {
        if (ClasificacionRegistro.esOfertaUnica(catalogos, alojamiento.tipoPropiedadId())) {
            throw new ReglaNegocioException(
                    "DATO_INVALIDO", "Casa o departamento completo usa el registro de oferta única");
        }
    }

    private void exigirOfertaUnica(Alojamiento alojamiento) {
        if (!ClasificacionRegistro.esOfertaUnica(catalogos, alojamiento.tipoPropiedadId())) {
            throw new ReglaNegocioException(
                    "DATO_INVALIDO", "Solo casa o departamento completo admite oferta única");
        }
    }

    private void aplicarPoliticasYHorarios(
            Alojamiento alojamiento, String politicas, String horarioEntrada, String horarioSalida) {
        if (politicas != null) {
            alojamiento.guardarPoliticas(new Politicas(politicas));
        }
        if (horarioEntrada == null && horarioSalida == null) {
            return;
        }
        if (horarioEntrada == null || horarioSalida == null) {
            throw new ReglaNegocioException(
                    "DATO_INVALIDO", "Los horarios de entrada y salida son obligatorios juntos");
        }
        alojamiento.guardarHorarios(new HorarioEstadia(horarioEntrada), new HorarioEstadia(horarioSalida));
    }

    private void exigirOfertaCompleta(GuardarOfertaUnicaCommand comando) {
        if (comando.capacidadHuespedes() == null
                || comando.cantidadDormitorios() == null
                || comando.cantidadCamas() == null
                || comando.cantidadBanos() == null
                || comando.precioNoche() == null) {
            throw new ReglaNegocioException("DATO_INVALIDO", "La oferta está incompleta");
        }
    }

    private TipoEspacioId tipoEspacioAlojamientoCompleto() {
        return catalogos.listarTiposEspacio().stream()
                .filter(tipo -> tipo.nombre().equalsIgnoreCase("Alojamiento completo"))
                .map(TipoEspacio::id)
                .findFirst()
                .orElseThrow(() -> new ReglaNegocioException(
                        "CATALOGO_INVALIDO", "No existe el tipo de espacio Alojamiento completo"));
    }

    private AlojamientoRegistradoResponse modificar(
            UUID anfitrionId, UUID alojamientoId, Consumer<Alojamiento> cambio) {
        AnfitrionId anfitrion = propios.exigir(anfitrionId);
        Alojamiento guardado = unitOfWork.ejecutar(() -> {
            Alojamiento alojamiento = propios.propio(anfitrion, alojamientoId);
            cambio.accept(alojamiento);
            return alojamientos.guardar(alojamiento);
        });
        return presentador.detalle(guardado);
    }

    private void exigirTipo(TipoPropiedadId tipo) {
        if (!catalogos.existeTipoPropiedad(tipo)) {
            throw new ReglaNegocioException("CATALOGO_INVALIDO", "El tipo de propiedad no existe");
        }
    }

    private TipoPropiedadId tipo(Integer id) {
        if (id == null) {
            throw new ReglaNegocioException("DATO_INVALIDO", "El tipo de propiedad es obligatorio");
        }
        return new TipoPropiedadId(id);
    }

    private Set<ServicioId> exigirServicios(List<Integer> ids) {
        Set<ServicioId> servicios = servicios(ids);
        if (!catalogos.existenServicios(servicios)) {
            throw new ReglaNegocioException("CATALOGO_INVALIDO", "Hay un servicio que no existe");
        }
        return servicios;
    }

    private Set<ServicioId> servicios(List<Integer> ids) {
        if (ids == null) {
            return Set.of();
        }
        Set<ServicioId> servicios = new HashSet<>();
        for (Integer id : ids) {
            if (id == null) {
                throw new ReglaNegocioException("DATO_INVALIDO", "El servicio es obligatorio");
            }
            servicios.add(new ServicioId(id));
        }
        return Set.copyOf(servicios);
    }

    private Set<String> identificadores(List<Fotografia> fotografias) {
        return fotografias.stream().map(Fotografia::identificadorAlmacen).collect(Collectors.toSet());
    }

    private List<DatosFotografia> fotosAlojamiento(List<GuardarFotografiasCommand.FotografiaCommand> fotos) {
        if (fotos == null) {
            return List.of();
        }
        return fotos.stream()
                .map(foto -> foto == null
                        ? fotografia(null, null, null, null)
                        : fotografia(foto.id(), foto.identificadorAlmacen(), foto.url(), foto.orden()))
                .toList();
    }

    private List<DatosFotografia> fotosUnidad(List<AgregarUnidadCommand.FotografiaCommand> fotos) {
        if (fotos == null) {
            return List.of();
        }
        return fotos.stream()
                .map(foto -> foto == null
                        ? fotografia(null, null, null, null)
                        : fotografia(foto.id(), foto.identificadorAlmacen(), foto.url(), foto.orden()))
                .toList();
    }

    private DatosFotografia fotografia(UUID id, String identificadorAlmacen, String url, Integer orden) {
        if (orden == null) {
            throw new ReglaNegocioException("DATO_INVALIDO", "El orden de la fotografía es obligatorio");
        }
        return new DatosFotografia(id, identificadorAlmacen, url, orden);
    }
}
