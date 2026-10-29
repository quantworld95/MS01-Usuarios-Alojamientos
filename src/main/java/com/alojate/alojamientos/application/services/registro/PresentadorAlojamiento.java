package com.alojate.alojamientos.application.services.registro;

import com.alojate.alojamientos.application.dto.AlojamientoRegistradoResponse;
import com.alojate.alojamientos.application.dto.AlojamientoRegistradoResponse.FotografiaResponse;
import com.alojate.alojamientos.application.dto.AlojamientoRegistradoResponse.HistorialResponse;
import com.alojate.alojamientos.application.dto.AlojamientoRegistradoResponse.ServicioResponse;
import com.alojate.alojamientos.application.dto.AlojamientoRegistradoResponse.UnidadResponse;
import com.alojate.alojamientos.application.dto.AlojamientoResumenResponse;
import com.alojate.alojamientos.domain.alojamiento.aggregate.Alojamiento;
import com.alojate.alojamientos.domain.alojamiento.aggregate.Fotografia;
import com.alojate.alojamientos.domain.alojamiento.aggregate.HistorialPublicacion;
import com.alojate.alojamientos.domain.alojamiento.aggregate.UnidadReservable;
import com.alojate.alojamientos.domain.alojamiento.valueobject.Titulo;
import com.alojate.alojamientos.domain.alojamiento.valueobject.Ubicacion;
import com.alojate.alojamientos.domain.catalogo.aggregate.Servicio;
import com.alojate.alojamientos.domain.catalogo.aggregate.TipoEspacio;
import com.alojate.alojamientos.domain.catalogo.aggregate.TipoPropiedad;
import com.alojate.alojamientos.domain.catalogo.repository.CatalogoAlojamientoRepository;
import com.alojate.alojamientos.domain.catalogo.valueobject.ServicioId;
import com.alojate.alojamientos.domain.exception.ReglaNegocioException;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public final class PresentadorAlojamiento {

    private final CatalogoAlojamientoRepository catalogos;

    public PresentadorAlojamiento(CatalogoAlojamientoRepository catalogos) {
        this.catalogos = catalogos;
    }

    public AlojamientoRegistradoResponse detalle(Alojamiento alojamiento) {
        Map<Integer, String> tipos = nombresTipo();
        Map<Integer, String> espacios = nombresEspacio();
        Map<Integer, Servicio> servicios = catalogos.listarServicios().stream()
                .collect(Collectors.toMap(servicio -> servicio.id().valor(), servicio -> servicio));
        Ubicacion ubicacion = alojamiento.ubicacion();
        return new AlojamientoRegistradoResponse(
                alojamiento.id().valor(),
                alojamiento.anfitrionId().valor(),
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
                alojamiento.estado().name(),
                alojamiento.motivoVigente(),
                alojamiento.fechaCreacion(),
                alojamiento.establecimientoCompleto(),
                alojamiento.listoParaRevision(),
                alojamiento.fotografias().stream().map(this::foto).toList(),
                mapearServicios(alojamiento.servicios(), servicios),
                alojamiento.unidades().stream()
                        .map(unidad -> unidad(unidad, espacios, servicios))
                        .toList(),
                alojamiento.historial().stream().map(this::historial).toList());
    }

    public UnidadResponse detalleUnidad(Alojamiento alojamiento, UUID unidadId) {
        Map<Integer, String> espacios = nombresEspacio();
        Map<Integer, Servicio> servicios = catalogos.listarServicios().stream()
                .collect(Collectors.toMap(servicio -> servicio.id().valor(), servicio -> servicio));
        UnidadReservable unidad = alojamiento.unidades().stream()
                .filter(item -> item.id().equals(unidadId))
                .findFirst()
                .orElseThrow(() -> new ReglaNegocioException(
                        "UNIDAD_NO_ENCONTRADA", "La habitacion no existe"));
        return unidad(unidad, espacios, servicios);
    }

    public List<AlojamientoResumenResponse> resumir(List<Alojamiento> alojamientos) {
        Map<Integer, String> tipos = nombresTipo();
        return alojamientos.stream().map(alojamiento -> resumen(alojamiento, tipos)).toList();
    }

    private AlojamientoResumenResponse resumen(Alojamiento alojamiento, Map<Integer, String> tipos) {
        Ubicacion ubicacion = alojamiento.ubicacion();
        return new AlojamientoResumenResponse(
                alojamiento.id().valor(),
                alojamiento.tipoPropiedadId().valor(),
                tipos.get(alojamiento.tipoPropiedadId().valor()),
                valor(alojamiento.titulo()),
                ubicacion == null ? null : ubicacion.ciudad(),
                ubicacion == null ? null : ubicacion.departamento(),
                alojamiento.estado().name(),
                alojamiento.motivoVigente(),
                alojamiento.fechaCreacion(),
                alojamiento.establecimientoCompleto(),
                alojamiento.listoParaRevision(),
                fotoPortada(alojamiento));
    }

    private String fotoPortada(Alojamiento alojamiento) {
        return alojamiento.fotografias().stream()
                .sorted(java.util.Comparator.comparingInt(Fotografia::orden))
                .map(Fotografia::url)
                .findFirst()
                .orElse(null);
    }

    private FotografiaResponse foto(Fotografia fotografia) {
        return new FotografiaResponse(
                fotografia.id(), fotografia.identificadorAlmacen(), fotografia.url(), fotografia.orden());
    }

    private UnidadResponse unidad(
            UnidadReservable unidad, Map<Integer, String> espacios, Map<Integer, Servicio> servicios) {
        return new UnidadResponse(
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
                unidad.estado().name(),
                unidad.fotografias().stream().map(this::foto).toList(),
                mapearServicios(unidad.servicios(), servicios),
                unidad.historial().stream().map(this::historial).toList());
    }

    private List<ServicioResponse> mapearServicios(Set<ServicioId> ids, Map<Integer, Servicio> servicios) {
        return ids.stream()
                .map(ServicioId::valor)
                .map(servicios::get)
                .map(servicio -> new ServicioResponse(
                        servicio.id().valor(), servicio.nombre(), servicio.categoria()))
                .toList();
    }

    private HistorialResponse historial(HistorialPublicacion evento) {
        return new HistorialResponse(evento.estado(), evento.motivo(), evento.actorId(), evento.fecha());
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
