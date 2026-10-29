package com.alojate.alojamientos.application.services.catalogo;

import com.alojate.alojamientos.application.dto.AlojamientoPublicadoResponse;
import com.alojate.alojamientos.application.dto.OfertaPublicadaResponse;
import com.alojate.alojamientos.application.dto.OfertaPublicadaResponse.FotografiaPublicaResponse;
import com.alojate.alojamientos.application.dto.OfertaPublicadaResponse.ServicioPublicadoResponse;
import com.alojate.alojamientos.application.dto.OfertaPublicadaResponse.UnidadPublicadaResponse;
import com.alojate.alojamientos.domain.alojamiento.aggregate.Alojamiento;
import com.alojate.alojamientos.domain.alojamiento.aggregate.Fotografia;
import com.alojate.alojamientos.domain.alojamiento.aggregate.UnidadReservable;
import com.alojate.alojamientos.domain.alojamiento.valueobject.EstadoPublicacion;
import com.alojate.alojamientos.domain.alojamiento.valueobject.Titulo;
import com.alojate.alojamientos.domain.alojamiento.valueobject.Ubicacion;
import com.alojate.alojamientos.domain.catalogo.aggregate.Servicio;
import com.alojate.alojamientos.domain.catalogo.aggregate.TipoEspacio;
import com.alojate.alojamientos.domain.catalogo.aggregate.TipoPropiedad;
import com.alojate.alojamientos.domain.catalogo.repository.CatalogoAlojamientoRepository;
import com.alojate.alojamientos.domain.catalogo.valueobject.ServicioId;
import com.alojate.alojamientos.domain.exception.ReglaNegocioException;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public final class PresentadorCatalogo {

    private final CatalogoAlojamientoRepository catalogos;

    public PresentadorCatalogo(CatalogoAlojamientoRepository catalogos) {
        this.catalogos = catalogos;
    }

    public List<AlojamientoPublicadoResponse> resumir(List<Alojamiento> alojamientos) {
        Map<Integer, String> tipos = nombresTipo();
        return alojamientos.stream().map(alojamiento -> resumen(alojamiento, tipos)).toList();
    }

    public OfertaPublicadaResponse detalle(Alojamiento alojamiento) {
        Map<Integer, String> tipos = nombresTipo();
        Map<Integer, String> espacios = nombresEspacio();
        Map<Integer, Servicio> servicios = catalogos.listarServicios().stream()
                .collect(Collectors.toMap(servicio -> servicio.id().valor(), servicio -> servicio));
        Ubicacion ubicacion = alojamiento.ubicacion();
        return new OfertaPublicadaResponse(
                alojamiento.id().valor(),
                alojamiento.tipoPropiedadId().valor(),
                tipos.get(alojamiento.tipoPropiedadId().valor()),
                valor(alojamiento.titulo()),
                alojamiento.descripcion() == null ? null : alojamiento.descripcion().valor(),
                alojamiento.politicas() == null ? null : alojamiento.politicas().valor(),
                alojamiento.horarioEntrada() == null ? null : alojamiento.horarioEntrada().valor(),
                alojamiento.horarioSalida() == null ? null : alojamiento.horarioSalida().valor(),
                ubicacion == null ? null : ubicacion.direccion(),
                ubicacion == null ? null : ubicacion.ciudad(),
                ubicacion == null ? null : ubicacion.departamento(),
                ubicacion == null ? null : ubicacion.latitud(),
                ubicacion == null ? null : ubicacion.longitud(),
                alojamiento.fotografias().stream()
                        .sorted(Comparator.comparingInt(Fotografia::orden))
                        .map(this::foto)
                        .toList(),
                mapearServicios(alojamiento.servicios(), servicios),
                alojamiento.unidades().stream()
                        .filter(unidad -> unidadVisibleEnCatalogo(alojamiento, unidad))
                        .map(unidad -> unidad(unidad, espacios, servicios))
                        .toList());
    }

    public UnidadPublicadaResponse detalleUnidad(Alojamiento alojamiento, UUID unidadId) {
        Map<Integer, String> espacios = nombresEspacio();
        Map<Integer, Servicio> servicios = catalogos.listarServicios().stream()
                .collect(Collectors.toMap(servicio -> servicio.id().valor(), servicio -> servicio));
        UnidadReservable unidad = alojamiento.unidades().stream()
                .filter(item -> item.id().equals(unidadId))
                .filter(item -> unidadVisibleEnCatalogo(alojamiento, item))
                .findFirst()
                .orElseThrow(() -> new ReglaNegocioException(
                        "UNIDAD_NO_ENCONTRADA", "La habitacion no existe"));
        return unidad(unidad, espacios, servicios);
    }

    /** Casa/depto: ciclo en alojamiento (oculta desactivadas). Hotel: solo PUBLICADO. */
    private static boolean unidadVisibleEnCatalogo(Alojamiento alojamiento, UnidadReservable unidad) {
        if (alojamiento.estado() == EstadoPublicacion.PUBLICADO) {
            return unidad.activa();
        }
        return unidad.visibleEnCatalogo();
    }

    private AlojamientoPublicadoResponse resumen(Alojamiento alojamiento, Map<Integer, String> tipos) {
        Ubicacion ubicacion = alojamiento.ubicacion();
        return new AlojamientoPublicadoResponse(
                alojamiento.id().valor(),
                alojamiento.tipoPropiedadId().valor(),
                tipos.get(alojamiento.tipoPropiedadId().valor()),
                valor(alojamiento.titulo()),
                ubicacion == null ? null : ubicacion.ciudad(),
                ubicacion == null ? null : ubicacion.departamento());
    }

    private FotografiaPublicaResponse foto(Fotografia fotografia) {
        return new FotografiaPublicaResponse(fotografia.id(), fotografia.url(), fotografia.orden());
    }

    private UnidadPublicadaResponse unidad(
            UnidadReservable unidad, Map<Integer, String> espacios, Map<Integer, Servicio> servicios) {
        return new UnidadPublicadaResponse(
                unidad.id(),
                unidad.tipoEspacioId().valor(),
                espacios.get(unidad.tipoEspacioId().valor()),
                unidad.nombre(),
                unidad.descripcion().valor(),
                unidad.capacidadHuespedes(),
                unidad.cantidadDormitorios(),
                unidad.cantidadCamas(),
                unidad.cantidadBanos(),
                unidad.inventarioTotal(),
                unidad.precioNoche().valor(),
                unidad.fotografias().stream()
                        .sorted(Comparator.comparingInt(Fotografia::orden))
                        .map(this::foto)
                        .toList(),
                mapearServicios(unidad.servicios(), servicios));
    }

    private List<ServicioPublicadoResponse> mapearServicios(
            Set<ServicioId> ids, Map<Integer, Servicio> servicios) {
        return ids.stream()
                .map(ServicioId::valor)
                .map(id -> servicio(servicios, id))
                .map(servicio -> new ServicioPublicadoResponse(
                        servicio.id().valor(), servicio.nombre(), servicio.categoria()))
                .toList();
    }

    private Servicio servicio(Map<Integer, Servicio> servicios, int id) {
        Servicio servicio = servicios.get(id);
        if (servicio == null) {
            throw new ReglaNegocioException("CATALOGO_INVALIDO", "Hay un servicio que no existe");
        }
        return servicio;
    }

    private Map<Integer, String> nombresTipo() {
        return catalogos.listarTiposPropiedad().stream()
                .collect(Collectors.toMap(tipo -> tipo.id().valor(), TipoPropiedad::nombre));
    }

    private Map<Integer, String> nombresEspacio() {
        return catalogos.listarTiposEspacio().stream()
                .collect(Collectors.toMap(tipo -> tipo.id().valor(), TipoEspacio::nombre));
    }

    private String valor(Titulo titulo) {
        return titulo == null ? null : titulo.valor();
    }
}
