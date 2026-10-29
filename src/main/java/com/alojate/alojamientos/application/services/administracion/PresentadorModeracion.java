package com.alojate.alojamientos.application.services.administracion;

import com.alojate.alojamientos.application.dto.AlojamientoModeradoResponse;
import com.alojate.alojamientos.application.dto.AlojamientoModeradoResumenResponse;
import com.alojate.alojamientos.application.dto.AlojamientoRegistradoResponse.FotografiaResponse;
import com.alojate.alojamientos.application.dto.AlojamientoRegistradoResponse.HistorialResponse;
import com.alojate.alojamientos.application.dto.AlojamientoRegistradoResponse.ServicioResponse;
import com.alojate.alojamientos.application.dto.AlojamientoRegistradoResponse.UnidadResponse;
import com.alojate.alojamientos.application.ports.output.NombreAnfitrion;
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

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public final class PresentadorModeracion {

    private final CatalogoAlojamientoRepository catalogos;
    private final NombreAnfitrion nombres;

    public PresentadorModeracion(CatalogoAlojamientoRepository catalogos, NombreAnfitrion nombres) {
        this.catalogos = catalogos;
        this.nombres = nombres;
    }

    public List<AlojamientoModeradoResumenResponse> resumir(List<Alojamiento> alojamientos) {
        Map<Integer, String> tipos = nombresTipo();
        Map<UUID, String> anfitriones = nombres.de(
                alojamientos.stream().map(alojamiento -> alojamiento.anfitrionId().valor()).toList());
        return alojamientos.stream()
                .map(alojamiento -> resumen(alojamiento, tipos, anfitriones))
                .toList();
    }

    public AlojamientoModeradoResponse detalle(Alojamiento alojamiento) {
        Map<Integer, String> tipos = nombresTipo();
        Map<Integer, String> espacios = nombresEspacio();
        Map<Integer, Servicio> servicios = catalogos.listarServicios().stream()
                .collect(Collectors.toMap(servicio -> servicio.id().valor(), servicio -> servicio));
        String nombre = nombres.de(List.of(alojamiento.anfitrionId().valor()))
                .get(alojamiento.anfitrionId().valor());
        Ubicacion ubicacion = alojamiento.ubicacion();
        return new AlojamientoModeradoResponse(
                alojamiento.id().valor(),
                alojamiento.anfitrionId().valor(),
                nombre,
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

    private AlojamientoModeradoResumenResponse resumen(
            Alojamiento alojamiento, Map<Integer, String> tipos, Map<UUID, String> anfitriones) {
        Ubicacion ubicacion = alojamiento.ubicacion();
        return new AlojamientoModeradoResumenResponse(
                alojamiento.id().valor(),
                alojamiento.anfitrionId().valor(),
                anfitriones.get(alojamiento.anfitrionId().valor()),
                alojamiento.tipoPropiedadId().valor(),
                tipos.get(alojamiento.tipoPropiedadId().valor()),
                valor(alojamiento.titulo()),
                ubicacion == null ? null : ubicacion.ciudad(),
                ubicacion == null ? null : ubicacion.departamento(),
                alojamiento.estado().name(),
                alojamiento.motivoVigente(),
                alojamiento.fechaCreacion());
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
