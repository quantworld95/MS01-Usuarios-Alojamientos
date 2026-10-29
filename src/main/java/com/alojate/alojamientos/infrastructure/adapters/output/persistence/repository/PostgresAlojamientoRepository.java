package com.alojate.alojamientos.infrastructure.adapters.output.persistence.repository;

import com.alojate.alojamientos.domain.alojamiento.aggregate.Alojamiento;
import com.alojate.alojamientos.domain.alojamiento.aggregate.Fotografia;
import com.alojate.alojamientos.domain.alojamiento.aggregate.HistorialPublicacion;
import com.alojate.alojamientos.domain.alojamiento.aggregate.UnidadReservable;
import com.alojate.alojamientos.domain.alojamiento.repository.AlojamientoRepository;
import com.alojate.alojamientos.domain.alojamiento.valueobject.AlojamientoId;
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
import com.alojate.alojamientos.infrastructure.adapters.output.persistence.entity.AlojamientoJpa;
import com.alojate.alojamientos.infrastructure.adapters.output.persistence.entity.AlojamientoServicioJpa;
import com.alojate.alojamientos.infrastructure.adapters.output.persistence.entity.FotografiaJpa;
import com.alojate.alojamientos.infrastructure.adapters.output.persistence.entity.HistorialPublicacionJpa;
import com.alojate.alojamientos.infrastructure.adapters.output.persistence.entity.UnidadReservableJpa;
import com.alojate.alojamientos.infrastructure.adapters.output.persistence.entity.UnidadServicioJpa;
import com.alojate.alojamientos.infrastructure.adapters.output.persistence.jpa.AlojamientoJpaRepository;
import com.alojate.alojamientos.infrastructure.adapters.output.persistence.jpa.ServicioJpaRepository;
import com.alojate.alojamientos.infrastructure.adapters.output.persistence.jpa.TipoEspacioJpaRepository;
import com.alojate.alojamientos.infrastructure.adapters.output.persistence.jpa.TipoPropiedadJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
public class PostgresAlojamientoRepository implements AlojamientoRepository {

    private final AlojamientoJpaRepository alojamientos;
    private final TipoPropiedadJpaRepository tiposPropiedad;
    private final TipoEspacioJpaRepository tiposEspacio;
    private final ServicioJpaRepository servicios;

    public PostgresAlojamientoRepository(
            AlojamientoJpaRepository alojamientos,
            TipoPropiedadJpaRepository tiposPropiedad,
            TipoEspacioJpaRepository tiposEspacio,
            ServicioJpaRepository servicios) {
        this.alojamientos = alojamientos;
        this.tiposPropiedad = tiposPropiedad;
        this.tiposEspacio = tiposEspacio;
        this.servicios = servicios;
    }

    @Override
    public Alojamiento guardar(Alojamiento alojamiento) {
        AlojamientoJpa jpa = alojamientos.findById(alojamiento.id().valor()).orElseGet(AlojamientoJpa::new);
        jpa.setIdAlojamiento(alojamiento.id().valor());
        jpa.setIdAnfitrion(alojamiento.anfitrionId().valor());
        jpa.setTipoPropiedad(tiposPropiedad.getReferenceById(alojamiento.tipoPropiedadId().valor()));
        jpa.setTitulo(alojamiento.titulo() == null ? null : alojamiento.titulo().valor());
        jpa.setDescripcion(alojamiento.descripcion() == null ? null : alojamiento.descripcion().valor());
        jpa.setPoliticas(alojamiento.politicas() == null ? null : alojamiento.politicas().valor());
        jpa.setHorarioEntrada(
                alojamiento.horarioEntrada() == null ? null : alojamiento.horarioEntrada().valor());
        jpa.setHorarioSalida(
                alojamiento.horarioSalida() == null ? null : alojamiento.horarioSalida().valor());
        if (alojamiento.ubicacion() == null) {
            jpa.setDireccion(null);
            jpa.setCiudad(null);
            jpa.setDepartamento(null);
            jpa.setLatitud(null);
            jpa.setLongitud(null);
        } else {
            jpa.setDireccion(alojamiento.ubicacion().direccion());
            jpa.setCiudad(alojamiento.ubicacion().ciudad());
            jpa.setDepartamento(alojamiento.ubicacion().departamento());
            jpa.setLatitud(alojamiento.ubicacion().latitud());
            jpa.setLongitud(alojamiento.ubicacion().longitud());
        }
        jpa.setEstadoPublicacion(alojamiento.estado().name());
        jpa.setFechaCreacion(alojamiento.fechaCreacion());
        sincronizarFotografiasAlojamiento(jpa, alojamiento);
        sincronizarServiciosAlojamiento(jpa, alojamiento);
        sincronizarUnidades(jpa, alojamiento);
        sincronizarHistorialAlojamiento(jpa, alojamiento);
        alojamientos.saveAndFlush(jpa);
        return alojamiento;
    }

    @Override
    public Optional<Alojamiento> buscarPorId(AlojamientoId id) {
        return alojamientos.findById(id.valor()).map(this::aDominio);
    }

    @Override
    public List<Alojamiento> listarPorAnfitrion(AnfitrionId anfitrionId) {
        return alojamientos.findByIdAnfitrionOrderByFechaCreacionDesc(anfitrionId.valor()).stream()
                .map(this::aDominio)
                .toList();
    }

    @Override
    public List<Alojamiento> listarPublicados() {
        return alojamientos.findByEstadoPublicacionInOrderByFechaCreacionDesc(List.of(
                        EstadoPublicacion.PUBLICADO.name(), EstadoPublicacion.ACTIVO.name()))
                .stream()
                .map(this::aDominio)
                .filter(Alojamiento::visibleEnCatalogo)
                .toList();
    }

    @Override
    public List<Alojamiento> listarTodos() {
        return alojamientos.findAllByOrderByFechaCreacionDesc().stream().map(this::aDominio).toList();
    }

    private void sincronizarFotografiasAlojamiento(AlojamientoJpa jpa, Alojamiento alojamiento) {
        Set<UUID> ids = alojamiento.fotografias().stream().map(Fotografia::id).collect(Collectors.toSet());
        jpa.getFotografias().removeIf(foto -> !ids.contains(foto.getIdFotografia()));
        Map<UUID, FotografiaJpa> actuales = jpa.getFotografias().stream()
                .collect(Collectors.toMap(FotografiaJpa::getIdFotografia, Function.identity()));
        for (Fotografia foto : alojamiento.fotografias()) {
            FotografiaJpa fila = actuales.get(foto.id());
            if (fila == null) {
                fila = new FotografiaJpa();
                fila.setIdFotografia(foto.id());
                fila.setAlojamiento(jpa);
                fila.setUnidad(null);
                jpa.getFotografias().add(fila);
            }
            fila.setIdentificadorAlmacen(foto.identificadorAlmacen());
            fila.setUrl(foto.url());
            fila.setOrden(foto.orden());
        }
    }

    private void sincronizarServiciosAlojamiento(AlojamientoJpa jpa, Alojamiento alojamiento) {
        Set<Integer> ids = alojamiento.servicios().stream().map(ServicioId::valor).collect(Collectors.toSet());
        jpa.getServicios().removeIf(vinculo -> !ids.contains(vinculo.getServicio().getIdServicio()));
        Set<Integer> actuales = new HashSet<>();
        for (AlojamientoServicioJpa vinculo : jpa.getServicios()) {
            actuales.add(vinculo.getServicio().getIdServicio());
        }
        for (ServicioId servicio : alojamiento.servicios()) {
            if (actuales.contains(servicio.valor())) {
                continue;
            }
            AlojamientoServicioJpa vinculo = new AlojamientoServicioJpa();
            vinculo.setAlojamiento(jpa);
            vinculo.setServicio(servicios.getReferenceById(servicio.valor()));
            jpa.getServicios().add(vinculo);
        }
    }

    private void sincronizarUnidades(AlojamientoJpa jpa, Alojamiento alojamiento) {
        Set<UUID> ids = alojamiento.unidades().stream().map(UnidadReservable::id).collect(Collectors.toSet());
        jpa.getUnidades().removeIf(unidad -> !ids.contains(unidad.getIdUnidad()));
        Map<UUID, UnidadReservableJpa> actuales = jpa.getUnidades().stream()
                .collect(Collectors.toMap(UnidadReservableJpa::getIdUnidad, Function.identity()));
        for (UnidadReservable unidad : alojamiento.unidades()) {
            UnidadReservableJpa fila = actuales.get(unidad.id());
            if (fila == null) {
                fila = new UnidadReservableJpa();
                fila.setIdUnidad(unidad.id());
                fila.setAlojamiento(jpa);
                jpa.getUnidades().add(fila);
            }
            fila.setTipoEspacio(tiposEspacio.getReferenceById(unidad.tipoEspacioId().valor()));
            fila.setNombre(unidad.nombre());
            fila.setDescripcion(unidad.descripcion().valor());
            fila.setCapacidadHuespedes(unidad.capacidadHuespedes());
            fila.setCantidadDormitorios(unidad.cantidadDormitorios());
            fila.setCantidadCamas(unidad.cantidadCamas());
            fila.setCantidadBanos(unidad.cantidadBanos());
            fila.setInventarioTotal(unidad.inventarioTotal());
            fila.setPrecioNoche(unidad.precioNoche().valor());
            fila.setEstado(unidad.estado().name());
            sincronizarFotografiasUnidad(fila, unidad);
            sincronizarServiciosUnidad(fila, unidad);
            sincronizarHistorialUnidad(fila, unidad);
        }
    }

    private void sincronizarFotografiasUnidad(UnidadReservableJpa jpa, UnidadReservable unidad) {
        Set<UUID> ids = unidad.fotografias().stream().map(Fotografia::id).collect(Collectors.toSet());
        jpa.getFotografias().removeIf(foto -> !ids.contains(foto.getIdFotografia()));
        Map<UUID, FotografiaJpa> actuales = jpa.getFotografias().stream()
                .collect(Collectors.toMap(FotografiaJpa::getIdFotografia, Function.identity()));
        for (Fotografia foto : unidad.fotografias()) {
            FotografiaJpa fila = actuales.get(foto.id());
            if (fila == null) {
                fila = new FotografiaJpa();
                fila.setIdFotografia(foto.id());
                fila.setAlojamiento(null);
                fila.setUnidad(jpa);
                jpa.getFotografias().add(fila);
            }
            fila.setIdentificadorAlmacen(foto.identificadorAlmacen());
            fila.setUrl(foto.url());
            fila.setOrden(foto.orden());
        }
    }

    private void sincronizarServiciosUnidad(UnidadReservableJpa jpa, UnidadReservable unidad) {
        Set<Integer> ids = unidad.servicios().stream().map(ServicioId::valor).collect(Collectors.toSet());
        jpa.getServicios().removeIf(vinculo -> !ids.contains(vinculo.getServicio().getIdServicio()));
        Set<Integer> actuales = new HashSet<>();
        for (UnidadServicioJpa vinculo : jpa.getServicios()) {
            actuales.add(vinculo.getServicio().getIdServicio());
        }
        for (ServicioId servicio : unidad.servicios()) {
            if (actuales.contains(servicio.valor())) {
                continue;
            }
            UnidadServicioJpa vinculo = new UnidadServicioJpa();
            vinculo.setUnidad(jpa);
            vinculo.setServicio(servicios.getReferenceById(servicio.valor()));
            jpa.getServicios().add(vinculo);
        }
    }

    private void sincronizarHistorialAlojamiento(AlojamientoJpa jpa, Alojamiento alojamiento) {
        Set<UUID> actuales = jpa.getHistorial().stream()
                .map(HistorialPublicacionJpa::getIdHistorial)
                .collect(Collectors.toSet());
        for (HistorialPublicacion evento : alojamiento.historial()) {
            if (actuales.contains(evento.id())) {
                continue;
            }
            HistorialPublicacionJpa fila = new HistorialPublicacionJpa();
            fila.setIdHistorial(evento.id());
            fila.setAlojamiento(jpa);
            fila.setUnidad(null);
            fila.setEstado(evento.estado());
            fila.setMotivo(evento.motivo());
            fila.setIdActor(evento.actorId());
            fila.setFecha(evento.fecha());
            jpa.getHistorial().add(fila);
        }
    }

    private void sincronizarHistorialUnidad(UnidadReservableJpa jpa, UnidadReservable unidad) {
        Set<UUID> actuales = jpa.getHistorial().stream()
                .map(HistorialPublicacionJpa::getIdHistorial)
                .collect(Collectors.toSet());
        for (HistorialPublicacion evento : unidad.historial()) {
            if (actuales.contains(evento.id())) {
                continue;
            }
            HistorialPublicacionJpa fila = new HistorialPublicacionJpa();
            fila.setIdHistorial(evento.id());
            fila.setAlojamiento(null);
            fila.setUnidad(jpa);
            fila.setEstado(evento.estado());
            fila.setMotivo(evento.motivo());
            fila.setIdActor(evento.actorId());
            fila.setFecha(evento.fecha());
            jpa.getHistorial().add(fila);
        }
    }

    private Alojamiento aDominio(AlojamientoJpa jpa) {
        Ubicacion ubicacion = jpa.getDireccion() == null
                ? null
                : new Ubicacion(
                        jpa.getDireccion(),
                        jpa.getCiudad(),
                        jpa.getDepartamento(),
                        jpa.getLatitud(),
                        jpa.getLongitud());
        return Alojamiento.reconstituir(
                new AlojamientoId(jpa.getIdAlojamiento()),
                new AnfitrionId(jpa.getIdAnfitrion()),
                new TipoPropiedadId(jpa.getTipoPropiedad().getIdTipoPropiedad()),
                jpa.getTitulo() == null ? null : new Titulo(jpa.getTitulo()),
                jpa.getDescripcion() == null ? null : new DescripcionAlojamiento(jpa.getDescripcion()),
                jpa.getPoliticas() == null ? null : new Politicas(jpa.getPoliticas()),
                jpa.getHorarioEntrada() == null ? null : new HorarioEstadia(jpa.getHorarioEntrada()),
                jpa.getHorarioSalida() == null ? null : new HorarioEstadia(jpa.getHorarioSalida()),
                ubicacion,
                EstadoPublicacion.desdeLegacy(jpa.getEstadoPublicacion()),
                jpa.getFechaCreacion(),
                jpa.getFotografias().stream().map(this::fotografia).toList(),
                jpa.getServicios().stream()
                        .map(vinculo -> new ServicioId(vinculo.getServicio().getIdServicio()))
                        .collect(Collectors.toSet()),
                jpa.getUnidades().stream().map(this::unidad).toList(),
                jpa.getHistorial().stream().map(this::historial).toList());
    }

    private UnidadReservable unidad(UnidadReservableJpa jpa) {
        return UnidadReservable.reconstituir(
                jpa.getIdUnidad(),
                new TipoEspacioId(jpa.getTipoEspacio().getIdTipoEspacio()),
                jpa.getNombre(),
                new DescripcionUnidad(jpa.getDescripcion()),
                jpa.getCapacidadHuespedes(),
                jpa.getCantidadDormitorios(),
                jpa.getCantidadCamas(),
                jpa.getCantidadBanos(),
                jpa.getInventarioTotal(),
                new PrecioNoche(jpa.getPrecioNoche()),
                EstadoUnidad.desde(jpa.getEstado()),
                jpa.getFotografias().stream().map(this::fotografia).toList(),
                jpa.getServicios().stream()
                        .map(vinculo -> new ServicioId(vinculo.getServicio().getIdServicio()))
                        .collect(Collectors.toSet()),
                jpa.getHistorial().stream().map(this::historial).toList());
    }

    private Fotografia fotografia(FotografiaJpa jpa) {
        return Fotografia.reconstituir(
                jpa.getIdFotografia(), jpa.getIdentificadorAlmacen(), jpa.getUrl(), jpa.getOrden());
    }

    private HistorialPublicacion historial(HistorialPublicacionJpa jpa) {
        return HistorialPublicacion.reconstituir(
                jpa.getIdHistorial(), jpa.getEstado(), jpa.getMotivo(), jpa.getIdActor(), jpa.getFecha());
    }
}
