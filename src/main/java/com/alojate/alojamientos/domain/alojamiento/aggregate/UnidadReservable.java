package com.alojate.alojamientos.domain.alojamiento.aggregate;

import com.alojate.alojamientos.domain.alojamiento.DatosFotografia;
import com.alojate.alojamientos.domain.alojamiento.politica.PoliticaEstadosHabitacion;
import com.alojate.alojamientos.domain.alojamiento.valueobject.DescripcionUnidad;
import com.alojate.alojamientos.domain.alojamiento.valueobject.EstadoPublicacion;
import com.alojate.alojamientos.domain.alojamiento.valueobject.EstadoUnidad;
import com.alojate.alojamientos.domain.alojamiento.valueobject.PrecioNoche;
import com.alojate.alojamientos.domain.catalogo.valueobject.ServicioId;
import com.alojate.alojamientos.domain.catalogo.valueobject.TipoEspacioId;
import com.alojate.alojamientos.domain.exception.ReglaNegocioException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class UnidadReservable {

    private static final int MAX_FOTOGRAFIAS = 20;

    private final UUID id;
    private final TipoEspacioId tipoEspacioId;
    private final String nombre;
    private final DescripcionUnidad descripcion;
    private final int capacidadHuespedes;
    private final int cantidadDormitorios;
    private final int cantidadCamas;
    private final BigDecimal cantidadBanos;
    private final int inventarioTotal;
    private final PrecioNoche precioNoche;
    private final EstadoUnidad estado;
    private final List<Fotografia> fotografias;
    private final Set<ServicioId> servicios;
    private final List<HistorialPublicacion> historial;

    private UnidadReservable(
            UUID id,
            TipoEspacioId tipoEspacioId,
            String nombre,
            DescripcionUnidad descripcion,
            int capacidadHuespedes,
            int cantidadDormitorios,
            int cantidadCamas,
            BigDecimal cantidadBanos,
            int inventarioTotal,
            PrecioNoche precioNoche,
            EstadoUnidad estado,
            List<Fotografia> fotografias,
            Set<ServicioId> servicios,
            List<HistorialPublicacion> historial) {
        if (tipoEspacioId == null || descripcion == null || precioNoche == null || estado == null) {
            throw new ReglaNegocioException("DATO_INVALIDO", "La unidad está incompleta");
        }
        this.id = id;
        this.tipoEspacioId = tipoEspacioId;
        this.nombre = nombre(nombre);
        this.descripcion = descripcion;
        this.capacidadHuespedes = rango(capacidadHuespedes, 1, 100, "La capacidad");
        this.cantidadDormitorios = rango(cantidadDormitorios, 0, 50, "Los dormitorios");
        this.cantidadCamas = rango(cantidadCamas, 1, 100, "Las camas");
        this.cantidadBanos = banos(cantidadBanos);
        this.inventarioTotal = rango(inventarioTotal, 1, 500, "El inventario");
        this.precioNoche = precioNoche;
        this.estado = estado;
        this.fotografias = List.copyOf(fotografias == null ? List.of() : fotografias);
        this.servicios = Set.copyOf(servicios == null ? Set.of() : servicios);
        this.historial = List.copyOf(historial == null ? List.of() : historial);
    }

    public static UnidadReservable nueva(
            TipoEspacioId tipoEspacioId,
            String nombre,
            DescripcionUnidad descripcion,
            int capacidadHuespedes,
            int cantidadDormitorios,
            int cantidadCamas,
            BigDecimal cantidadBanos,
            int inventarioTotal,
            PrecioNoche precioNoche,
            EstadoUnidad estado,
            List<DatosFotografia> fotografias,
            Set<ServicioId> servicios,
            UUID actorId,
            Instant ahora) {
        EstadoUnidad inicial = estado == null ? EstadoUnidad.BORRADOR : estado;
        return new UnidadReservable(
                UUID.randomUUID(),
                tipoEspacioId,
                nombre,
                descripcion,
                capacidadHuespedes,
                cantidadDormitorios,
                cantidadCamas,
                cantidadBanos,
                inventarioTotal,
                precioNoche,
                inicial,
                mapearFotografias(List.of(), fotografias),
                servicios,
                List.of(HistorialPublicacion.abrir(inicial, actorId, ahora)));
    }

    public static UnidadReservable reconstituir(
            UUID id,
            TipoEspacioId tipoEspacioId,
            String nombre,
            DescripcionUnidad descripcion,
            int capacidadHuespedes,
            int cantidadDormitorios,
            int cantidadCamas,
            BigDecimal cantidadBanos,
            int inventarioTotal,
            PrecioNoche precioNoche,
            EstadoUnidad estado,
            List<Fotografia> fotografias,
            Set<ServicioId> servicios,
            List<HistorialPublicacion> historial) {
        if (id == null) {
            throw new ReglaNegocioException("DATO_INVALIDO", "La unidad es obligatoria");
        }
        return new UnidadReservable(
                id,
                tipoEspacioId,
                nombre,
                descripcion,
                capacidadHuespedes,
                cantidadDormitorios,
                cantidadCamas,
                cantidadBanos,
                inventarioTotal,
                precioNoche,
                estado,
                fotografias,
                servicios,
                historial);
    }

    public UnidadReservable actualizar(
            TipoEspacioId tipoEspacioId,
            String nombre,
            DescripcionUnidad descripcion,
            int capacidadHuespedes,
            int cantidadDormitorios,
            int cantidadCamas,
            BigDecimal cantidadBanos,
            int inventarioTotal,
            PrecioNoche precioNoche,
            EstadoUnidad estadoIgnorado,
            List<DatosFotografia> fotografias,
            Set<ServicioId> servicios,
            UUID actorId,
            Instant ahora) {
        if (!this.estado.admiteEdicionRegistro()) {
            throw new ReglaNegocioException(
                    "PUBLICACION_NO_PERMITIDA",
                    "Solo se edita una habitación en borrador o rechazada");
        }
        // El estado solo cambia por transiciones explícitas (enviar/aprobar/desactivar…).
        return new UnidadReservable(
                id,
                tipoEspacioId,
                nombre,
                descripcion,
                capacidadHuespedes,
                cantidadDormitorios,
                cantidadCamas,
                cantidadBanos,
                inventarioTotal,
                precioNoche,
                this.estado,
                mapearFotografias(this.fotografias, fotografias),
                servicios,
                this.historial);
    }

    public UnidadReservable enviarARevision(UUID actorId, Instant ahora) {
        return conEstado(PoliticaEstadosHabitacion.trasEnviarARevision(estado), null, actorId, ahora);
    }

    public UnidadReservable aceptar(UUID actorId, Instant ahora) {
        return conEstado(PoliticaEstadosHabitacion.trasAprobar(estado), null, actorId, ahora);
    }

    public UnidadReservable rechazar(String motivo, UUID actorId, Instant ahora) {
        return conEstado(PoliticaEstadosHabitacion.trasRechazar(estado), motivo, actorId, ahora);
    }

    public UnidadReservable desactivar(UUID actorId, Instant ahora) {
        return conEstado(PoliticaEstadosHabitacion.trasDesactivarPorAnfitrion(estado), null, actorId, ahora);
    }

    public UnidadReservable activar(EstadoPublicacion estadoEstablecimiento, UUID actorId, Instant ahora) {
        return conEstado(
                PoliticaEstadosHabitacion.trasReactivarPorAnfitrion(estado, estadoEstablecimiento),
                null,
                actorId,
                ahora);
    }

    public UnidadReservable suspender(String motivo, UUID actorId, Instant ahora) {
        return conEstado(PoliticaEstadosHabitacion.trasSuspender(estado), motivo, actorId, ahora);
    }

    public UnidadReservable levantarSuspension(
            EstadoPublicacion estadoEstablecimiento, UUID actorId, Instant ahora) {
        return conEstado(
                PoliticaEstadosHabitacion.trasLevantarSuspension(estado, estadoEstablecimiento),
                null,
                actorId,
                ahora);
    }

    public UnidadReservable aplicarCascadaEstablecimiento(
            EstadoPublicacion nuevoEstadoEstablecimiento, UUID actorId, Instant ahora) {
        EstadoUnidad siguiente =
                PoliticaEstadosHabitacion.cascadaDesdeEstablecimiento(estado, nuevoEstadoEstablecimiento);
        if (siguiente == estado) {
            return this;
        }
        return conEstado(siguiente, PoliticaEstadosHabitacion.MOTIVO_ESTABLECIMIENTO, actorId, ahora);
    }

    public UnidadReservable restaurarTrasEstablecimientoActivo(UUID actorId, Instant ahora) {
        EstadoUnidad siguiente =
                PoliticaEstadosHabitacion.restaurarTrasEstablecimientoActivo(estado, motivoVigente());
        if (siguiente == estado) {
            return this;
        }
        return conEstado(siguiente, null, actorId, ahora);
    }

    private UnidadReservable conEstado(EstadoUnidad nuevo, String motivo, UUID actorId, Instant ahora) {
        if (actorId == null || ahora == null) {
            throw new ReglaNegocioException("DATO_INVALIDO", "La transición de la unidad está incompleta");
        }
        List<HistorialPublicacion> nuevoHistorial = new ArrayList<>(historial);
        nuevoHistorial.add(HistorialPublicacion.decidir(nuevo, motivo, actorId, ahora));
        return new UnidadReservable(
                id,
                tipoEspacioId,
                nombre,
                descripcion,
                capacidadHuespedes,
                cantidadDormitorios,
                cantidadCamas,
                cantidadBanos,
                inventarioTotal,
                precioNoche,
                nuevo,
                fotografias,
                servicios,
                nuevoHistorial);
    }

    public String motivoVigente() {
        if (historial.isEmpty()) {
            return null;
        }
        return historial.getLast().motivo();
    }

    public UUID id() {
        return id;
    }

    public TipoEspacioId tipoEspacioId() {
        return tipoEspacioId;
    }

    public String nombre() {
        return nombre;
    }

    public DescripcionUnidad descripcion() {
        return descripcion;
    }

    public int capacidadHuespedes() {
        return capacidadHuespedes;
    }

    public int cantidadDormitorios() {
        return cantidadDormitorios;
    }

    public int cantidadCamas() {
        return cantidadCamas;
    }

    public BigDecimal cantidadBanos() {
        return cantidadBanos;
    }

    public int inventarioTotal() {
        return inventarioTotal;
    }

    public PrecioNoche precioNoche() {
        return precioNoche;
    }

    public EstadoUnidad estado() {
        return estado;
    }

    public List<Fotografia> fotografias() {
        return fotografias;
    }

    public Set<ServicioId> servicios() {
        return servicios;
    }

    public List<HistorialPublicacion> historial() {
        return historial;
    }

    /** Unidad usable como oferta en registro / con contenido listo. */
    public boolean activa() {
        return estado.cuentaComoOfertaLista();
    }

    public boolean visibleEnCatalogo() {
        return estado.esVisibleEnCatalogo();
    }

    private static List<Fotografia> mapearFotografias(
            List<Fotografia> actuales, List<DatosFotografia> fotos) {
        if (fotos == null) {
            throw new ReglaNegocioException("DATO_INVALIDO", "Las fotografías son obligatorias");
        }
        if (fotos.size() > MAX_FOTOGRAFIAS) {
            throw new ReglaNegocioException(
                    "DATO_INVALIDO", "La unidad admite hasta " + MAX_FOTOGRAFIAS + " fotografías");
        }
        Map<UUID, Fotografia> porId = actuales.stream()
                .collect(Collectors.toMap(Fotografia::id, Function.identity()));
        Set<Integer> ordenes = new HashSet<>();
        List<Fotografia> nuevas = new ArrayList<>();
        for (DatosFotografia datos : fotos) {
            if (datos == null || !ordenes.add(datos.orden())) {
                throw new ReglaNegocioException("DATO_INVALIDO", "El orden de las fotografías se repite");
            }
            if (datos.id() == null) {
                nuevas.add(Fotografia.nueva(datos.identificadorAlmacen(), datos.url(), datos.orden()));
            } else if (!porId.containsKey(datos.id())) {
                throw new ReglaNegocioException("DATO_INVALIDO", "La fotografía no pertenece a la unidad");
            } else {
                Fotografia actual = porId.get(datos.id());
                if (datos.identificadorAlmacen() == null
                        || !actual.identificadorAlmacen().equals(datos.identificadorAlmacen().trim())) {
                    throw new ReglaNegocioException(
                            "DATO_INVALIDO", "La fotografía no coincide con el archivo guardado");
                }
                nuevas.add(Fotografia.reconstituir(
                        datos.id(), actual.identificadorAlmacen(), datos.url(), datos.orden()));
            }
        }
        return List.copyOf(nuevas);
    }

    private static String nombre(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new ReglaNegocioException("DATO_INVALIDO", "El nombre de la unidad es obligatorio");
        }
        String limpio = valor.trim().replaceAll("\\s+", " ");
        if (limpio.length() > 120) {
            throw new ReglaNegocioException("DATO_INVALIDO", "El nombre de la unidad admite hasta 120 caracteres");
        }
        return limpio;
    }

    private static int rango(int valor, int minimo, int maximo, String nombre) {
        if (valor < minimo || valor > maximo) {
            throw new ReglaNegocioException(
                    "DATO_INVALIDO", nombre + " debe estar entre " + minimo + " y " + maximo);
        }
        return valor;
    }

    private static BigDecimal banos(BigDecimal valor) {
        if (valor == null || valor.scale() > 1 || valor.signum() < 0 || valor.compareTo(new BigDecimal("20")) > 0) {
            throw new ReglaNegocioException("DATO_INVALIDO", "La cantidad de baños no es válida");
        }
        return valor;
    }
}
