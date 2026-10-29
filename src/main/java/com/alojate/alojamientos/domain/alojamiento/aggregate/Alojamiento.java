package com.alojate.alojamientos.domain.alojamiento.aggregate;

import com.alojate.alojamientos.domain.alojamiento.DatosFotografia;
import com.alojate.alojamientos.domain.alojamiento.DatosUnidad;
import com.alojate.alojamientos.domain.alojamiento.politica.PoliticaCicloPublicacion;
import com.alojate.alojamientos.domain.alojamiento.politica.PoliticaEstadosEstablecimiento;
import com.alojate.alojamientos.domain.alojamiento.politica.PoliticaEstadosOferta;
import com.alojate.alojamientos.domain.alojamiento.valueobject.AlojamientoId;
import com.alojate.alojamientos.domain.alojamiento.valueobject.AnfitrionId;
import com.alojate.alojamientos.domain.alojamiento.valueobject.DescripcionAlojamiento;
import com.alojate.alojamientos.domain.alojamiento.valueobject.EstadoPublicacion;
import com.alojate.alojamientos.domain.alojamiento.valueobject.EstadoUnidad;
import com.alojate.alojamientos.domain.alojamiento.valueobject.HorarioEstadia;
import com.alojate.alojamientos.domain.alojamiento.valueobject.Politicas;
import com.alojate.alojamientos.domain.alojamiento.valueobject.Titulo;
import com.alojate.alojamientos.domain.alojamiento.valueobject.Ubicacion;
import com.alojate.alojamientos.domain.catalogo.valueobject.ServicioId;
import com.alojate.alojamientos.domain.catalogo.valueobject.TipoPropiedadId;
import com.alojate.alojamientos.domain.exception.ReglaNegocioException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class Alojamiento {

    private final AlojamientoId id;
    private final AnfitrionId anfitrionId;
    private TipoPropiedadId tipoPropiedadId;
    private Titulo titulo;
    private DescripcionAlojamiento descripcion;
    private Politicas politicas;
    private HorarioEstadia horarioEntrada;
    private HorarioEstadia horarioSalida;
    private Ubicacion ubicacion;
    private EstadoPublicacion estado;
    private final Instant fechaCreacion;
    private List<Fotografia> fotografias;
    private Set<ServicioId> servicios;
    private List<UnidadReservable> unidades;
    private final List<HistorialPublicacion> historial;

    private Alojamiento(
            AlojamientoId id,
            AnfitrionId anfitrionId,
            TipoPropiedadId tipoPropiedadId,
            Titulo titulo,
            DescripcionAlojamiento descripcion,
            Politicas politicas,
            HorarioEstadia horarioEntrada,
            HorarioEstadia horarioSalida,
            Ubicacion ubicacion,
            EstadoPublicacion estado,
            Instant fechaCreacion,
            List<Fotografia> fotografias,
            Set<ServicioId> servicios,
            List<UnidadReservable> unidades,
            List<HistorialPublicacion> historial) {
        this.id = id;
        this.anfitrionId = anfitrionId;
        this.tipoPropiedadId = tipoPropiedadId;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.politicas = politicas;
        this.horarioEntrada = horarioEntrada;
        this.horarioSalida = horarioSalida;
        this.ubicacion = ubicacion;
        this.estado = estado;
        this.fechaCreacion = fechaCreacion;
        this.fotografias = List.copyOf(fotografias);
        this.servicios = Set.copyOf(servicios);
        this.unidades = List.copyOf(unidades);
        this.historial = new ArrayList<>(historial);
    }

    public static Alojamiento registrar(AnfitrionId anfitrion, TipoPropiedadId tipo, Instant ahora) {
        if (anfitrion == null || tipo == null || ahora == null) {
            throw new ReglaNegocioException("DATO_INVALIDO", "El registro está incompleto");
        }
        Alojamiento alojamiento = new Alojamiento(
                AlojamientoId.nuevo(),
                anfitrion,
                tipo,
                null,
                null,
                null,
                null,
                null,
                null,
                EstadoPublicacion.BORRADOR,
                ahora,
                List.of(),
                Set.of(),
                List.of(),
                List.of());
        alojamiento.historial.add(HistorialPublicacion.abrir(EstadoPublicacion.BORRADOR, anfitrion.valor(), ahora));
        return alojamiento;
    }

    public static Alojamiento reconstituir(
            AlojamientoId id,
            AnfitrionId anfitrionId,
            TipoPropiedadId tipoPropiedadId,
            Titulo titulo,
            DescripcionAlojamiento descripcion,
            Politicas politicas,
            HorarioEstadia horarioEntrada,
            HorarioEstadia horarioSalida,
            Ubicacion ubicacion,
            EstadoPublicacion estado,
            Instant fechaCreacion,
            List<Fotografia> fotografias,
            Set<ServicioId> servicios,
            List<UnidadReservable> unidades,
            List<HistorialPublicacion> historial) {
        return new Alojamiento(
                id,
                anfitrionId,
                tipoPropiedadId,
                titulo,
                descripcion,
                politicas,
                horarioEntrada,
                horarioSalida,
                ubicacion,
                estado,
                fechaCreacion,
                fotografias,
                servicios,
                unidades,
                historial);
    }

    public void guardarTipo(TipoPropiedadId tipo) {
        exigirRegistroParcial();
        if (tipo == null) {
            throw new ReglaNegocioException("DATO_INVALIDO", "El tipo de propiedad es obligatorio");
        }
        this.tipoPropiedadId = tipo;
    }

    public void guardarUbicacion(Ubicacion ubicacion) {
        exigirRegistroParcial();
        if (ubicacion == null) {
            throw new ReglaNegocioException("DATO_INVALIDO", "La ubicación es obligatoria");
        }
        this.ubicacion = ubicacion;
    }

    public void guardarPresentacion(Titulo titulo, DescripcionAlojamiento descripcion) {
        exigirRegistroParcial();
        if (titulo == null || descripcion == null) {
            throw new ReglaNegocioException("DATO_INVALIDO", "El título y la descripción son obligatorios");
        }
        this.titulo = titulo;
        this.descripcion = descripcion;
    }

    public void guardarPoliticas(Politicas politicas) {
        exigirRegistroParcial();
        if (politicas == null) {
            throw new ReglaNegocioException("DATO_INVALIDO", "Las políticas son obligatorias");
        }
        this.politicas = politicas;
    }

    public void guardarHorarios(HorarioEstadia entrada, HorarioEstadia salida) {
        exigirRegistroParcial();
        if (entrada == null || salida == null) {
            throw new ReglaNegocioException("DATO_INVALIDO", "Los horarios de entrada y salida son obligatorios");
        }
        this.horarioEntrada = entrada;
        this.horarioSalida = salida;
    }

    public void guardarServicios(Set<ServicioId> servicios) {
        exigirRegistroParcial();
        this.servicios = servicios == null ? Set.of() : Set.copyOf(servicios);
    }

    public void guardarFotografias(List<DatosFotografia> fotos) {
        exigirRegistroParcial();
        if (fotos == null) {
            throw new ReglaNegocioException("DATO_INVALIDO", "Las fotografías son obligatorias");
        }
        if (fotos.size() > 20) {
            throw new ReglaNegocioException("DATO_INVALIDO", "El alojamiento admite hasta 20 fotografías");
        }
        Map<UUID, Fotografia> actuales = fotografias.stream()
                .collect(Collectors.toMap(Fotografia::id, Function.identity()));
        Set<Integer> ordenes = new HashSet<>();
        List<Fotografia> nuevas = new ArrayList<>();
        for (DatosFotografia datos : fotos) {
            if (datos == null || !ordenes.add(datos.orden())) {
                throw new ReglaNegocioException("DATO_INVALIDO", "El orden de las fotografías se repite");
            }
            if (datos.id() == null) {
                nuevas.add(Fotografia.nueva(datos.identificadorAlmacen(), datos.url(), datos.orden()));
            } else if (!actuales.containsKey(datos.id())) {
                throw new ReglaNegocioException("DATO_INVALIDO", "La fotografía no pertenece al alojamiento");
            } else {
                Fotografia actual = actuales.get(datos.id());
                if (datos.identificadorAlmacen() == null
                        || !actual.identificadorAlmacen().equals(datos.identificadorAlmacen().trim())) {
                    throw new ReglaNegocioException(
                            "DATO_INVALIDO", "La fotografía no coincide con el archivo guardado");
                }
                nuevas.add(Fotografia.reconstituir(
                        datos.id(), actual.identificadorAlmacen(), datos.url(), datos.orden()));
            }
        }
        this.fotografias = List.copyOf(nuevas);
    }

    public void agregarFotografia(String identificadorAlmacen, String url) {
        exigirRegistroParcial();
        if (fotografias.size() >= 20) {
            throw new ReglaNegocioException("DATO_INVALIDO", "El alojamiento admite hasta 20 fotografías");
        }
        int orden = fotografias.stream().mapToInt(Fotografia::orden).max().orElse(0) + 1;
        List<Fotografia> nuevas = new ArrayList<>(fotografias);
        nuevas.add(Fotografia.nueva(identificadorAlmacen, url, orden));
        this.fotografias = List.copyOf(nuevas);
    }

    /**
     * Agrega un tipo de habitación sin tocar las unidades ya existentes.
     */
    public void agregarUnidad(DatosUnidad datos, Instant ahora) {
        exigirRegistroParcial();
        if (datos == null || ahora == null) {
            throw new ReglaNegocioException("DATO_INVALIDO", "La habitación está incompleta");
        }
        if (datos.id() != null) {
            throw new ReglaNegocioException(
                    "DATO_INVALIDO", "Para crear una habitación no se envía id");
        }
        if (unidades.size() >= 30) {
            throw new ReglaNegocioException("DATO_INVALIDO", "El alojamiento admite hasta 30 unidades");
        }
        List<UnidadReservable> nuevas = new ArrayList<>(unidades);
        nuevas.add(crearUnidad(datos, anfitrionId.valor(), ahora));
        this.unidades = List.copyOf(nuevas);
    }

    public void guardarOfertaUnica(DatosUnidad datos, Instant ahora) {
        exigirRegistroParcial();
        if (datos == null || ahora == null) {
            throw new ReglaNegocioException("DATO_INVALIDO", "La oferta es obligatoria");
        }
        if (datos.inventarioTotal() != 1) {
            throw new ReglaNegocioException(
                    "DATO_INVALIDO", "Casa o departamento completo admite inventario 1");
        }
        UUID actorId = anfitrionId.valor();
        if (unidades.isEmpty()) {
            if (datos.id() != null) {
                throw new ReglaNegocioException("DATO_INVALIDO", "La unidad no pertenece al alojamiento");
            }
            this.unidades = List.of(crearUnidad(datos, actorId, ahora));
            return;
        }
        if (unidades.size() > 1) {
            throw new ReglaNegocioException(
                    "DATO_INVALIDO", "Casa o departamento completo solo admite una oferta");
        }
        UnidadReservable actual = unidades.getFirst();
        UUID id = datos.id() == null ? actual.id() : datos.id();
        if (!actual.id().equals(id)) {
            throw new ReglaNegocioException("DATO_INVALIDO", "La unidad no pertenece al alojamiento");
        }
        DatosUnidad conId = new DatosUnidad(
                id,
                datos.tipoEspacioId(),
                datos.nombre(),
                datos.descripcion(),
                datos.capacidadHuespedes(),
                datos.cantidadDormitorios(),
                datos.cantidadCamas(),
                datos.cantidadBanos(),
                1,
                datos.precioNoche(),
                datos.estado(),
                datos.fotografias(),
                datos.servicios());
        this.unidades = List.of(actualizarUnidad(actual, conId, actorId, ahora));
    }

    private UnidadReservable crearUnidad(DatosUnidad datos, UUID actorId, Instant ahora) {
        EstadoUnidad inicial = datos.estado() == null ? EstadoUnidad.BORRADOR : datos.estado();
        if (inicial != EstadoUnidad.BORRADOR) {
            // Nuevas unidades siempre arrancan en borrador; las transiciones son endpoints aparte.
            inicial = EstadoUnidad.BORRADOR;
        }
        return UnidadReservable.nueva(
                datos.tipoEspacioId(),
                datos.nombre(),
                datos.descripcion(),
                datos.capacidadHuespedes(),
                datos.cantidadDormitorios(),
                datos.cantidadCamas(),
                datos.cantidadBanos(),
                datos.inventarioTotal(),
                datos.precioNoche(),
                inicial,
                datos.fotografias(),
                datos.servicios(),
                actorId,
                ahora);
    }

    private static UnidadReservable actualizarUnidad(
            UnidadReservable actual, DatosUnidad datos, UUID actorId, Instant ahora) {
        return actual.actualizar(
                datos.tipoEspacioId(),
                datos.nombre(),
                datos.descripcion(),
                datos.capacidadHuespedes(),
                datos.cantidadDormitorios(),
                datos.cantidadCamas(),
                datos.cantidadBanos(),
                datos.inventarioTotal(),
                datos.precioNoche(),
                datos.estado(),
                datos.fotografias(),
                datos.servicios(),
                actorId,
                ahora);
    }

    public boolean establecimientoCompleto() {
        return titulo != null
                && descripcion != null
                && ubicacion != null
                && politicas != null
                && horarioEntrada != null
                && horarioSalida != null
                && !fotografias.isEmpty();
    }

    public boolean tieneUnidadActiva() {
        return unidades.stream().anyMatch(UnidadReservable::activa);
    }

    public boolean listoParaRevision() {
        return establecimientoCompleto() && tieneUnidadActiva();
    }

    public boolean listoParaRevision(PoliticaCicloPublicacion politica) {
        return politica.listoParaRevision(establecimientoCompleto(), tieneUnidadActiva());
    }

    public String motivoVigente() {
        if (historial.isEmpty()) {
            return null;
        }
        return historial.getLast().motivo();
    }

    /**
     * Cierra el wizard de registro según la strategy:
     * oferta única → EN_REVISION; establecimiento (hotel/hostal) → ACTIVO (luego se agregan habitaciones).
     */
    public void finalizarRegistro(PoliticaCicloPublicacion politica, Instant ahora) {
        if (politica == null || ahora == null) {
            throw new ReglaNegocioException("DATO_INVALIDO", "La finalización del registro está incompleta");
        }
        if (estado != EstadoPublicacion.BORRADOR && estado != EstadoPublicacion.RECHAZADO) {
            throw new ReglaNegocioException(
                    "PUBLICACION_NO_PERMITIDA",
                    "Solo un borrador o un rechazo puede finalizar el registro");
        }
        if (!politica.listoParaFinalizarRegistro(establecimientoCompleto(), tieneUnidadActiva())) {
            throw new ReglaNegocioException(
                    "PUBLICACION_NO_PERMITIDA",
                    politica.esEstablecimiento()
                            ? "Completa presentación, ubicación, políticas, horarios y fotos del establecimiento"
                            : "Completa presentación, ubicación, políticas, horarios, fotos y la oferta (unidad)");
        }
        transicionar(politica.estadoTrasFinalizarRegistro(), null, anfitrionId.valor(), ahora);
    }

    /**
     * Envía a moderación (casa/depto): EN_REVISION.
     * Hotel/hostal: la moderación es por habitación ({@link #enviarUnidadARevision}).
     */
    public void enviarARevision(PoliticaCicloPublicacion politica, Instant ahora) {
        if (politica == null || ahora == null) {
            throw new ReglaNegocioException("DATO_INVALIDO", "El envío a revisión está incompleto");
        }
        if (politica.esEstablecimiento()) {
            throw new ReglaNegocioException(
                    "PUBLICACION_NO_PERMITIDA",
                    "En hotel o hostal la revisión es por habitación");
        }
        if (!politica.listoParaRevision(establecimientoCompleto(), tieneUnidadActiva())) {
            throw new ReglaNegocioException(
                    "PUBLICACION_NO_PERMITIDA",
                    "Completa el establecimiento y la oferta (unidad)");
        }
        transicionar(PoliticaEstadosOferta.trasEnviarARevision(estado), null, anfitrionId.valor(), ahora);
    }

    /** Compatibilidad: asume oferta única (unidad requerida). */
    public void enviarARevision(Instant ahora) {
        enviarARevision(PoliticaCicloPublicacion.OfertaUnica.INSTANCE, ahora);
    }

    public void aceptar(UUID actorId, Instant ahora) {
        exigirModerador(actorId, ahora);
        if (!listoParaRevision()) {
            throw new ReglaNegocioException(
                    "PUBLICACION_NO_PERMITIDA", "El alojamiento no está listo para publicarse");
        }
        transicionar(PoliticaEstadosOferta.trasAprobar(estado), null, actorId, ahora);
    }

    public void rechazar(UUID actorId, String motivo, Instant ahora) {
        exigirModerador(actorId, ahora);
        transicionar(PoliticaEstadosOferta.trasRechazar(estado), motivo(motivo), actorId, ahora);
    }

    /** Anfitrión: casa PUBLICADO→DESACTIVADO; hotel ACTIVO→DESACTIVADO (+ cascada habitaciones). */
    public void desactivar(PoliticaCicloPublicacion politica, Instant ahora) {
        if (politica == null || ahora == null) {
            throw new ReglaNegocioException("DATO_INVALIDO", "La desactivación está incompleta");
        }
        if (politica.esEstablecimiento()) {
            EstadoPublicacion siguiente = PoliticaEstadosEstablecimiento.trasDesactivar(estado);
            transicionar(siguiente, null, anfitrionId.valor(), ahora);
            cascadaUnidadesDesdeEstablecimiento(siguiente, anfitrionId.valor(), ahora);
            return;
        }
        transicionar(PoliticaEstadosOferta.trasDesactivar(estado), null, anfitrionId.valor(), ahora);
    }

    /** Anfitrión: casa DESACTIVADO→PUBLICADO; hotel DESACTIVADO→ACTIVO (+ restaura cascada). */
    public void activar(PoliticaCicloPublicacion politica, Instant ahora) {
        if (politica == null || ahora == null) {
            throw new ReglaNegocioException("DATO_INVALIDO", "La activación está incompleta");
        }
        if (politica.esEstablecimiento()) {
            EstadoPublicacion siguiente = PoliticaEstadosEstablecimiento.trasActivar(estado);
            transicionar(siguiente, null, anfitrionId.valor(), ahora);
            restaurarUnidadesTrasEstablecimientoActivo(anfitrionId.valor(), ahora);
            return;
        }
        transicionar(PoliticaEstadosOferta.trasActivar(estado), null, anfitrionId.valor(), ahora);
    }

    public void suspender(PoliticaCicloPublicacion politica, UUID actorId, String motivo, Instant ahora) {
        exigirModerador(actorId, ahora);
        if (politica == null) {
            throw new ReglaNegocioException("DATO_INVALIDO", "La suspensión está incompleta");
        }
        String motivoLimpio = motivo(motivo);
        if (politica.esEstablecimiento()) {
            EstadoPublicacion siguiente = PoliticaEstadosEstablecimiento.trasSuspender(estado);
            transicionar(siguiente, motivoLimpio, actorId, ahora);
            cascadaUnidadesDesdeEstablecimiento(siguiente, actorId, ahora);
            return;
        }
        transicionar(PoliticaEstadosOferta.trasSuspender(estado), motivoLimpio, actorId, ahora);
    }

    /** Compat: suspensión de oferta única sin política explícita. */
    public void suspender(UUID actorId, String motivo, Instant ahora) {
        suspender(PoliticaCicloPublicacion.OfertaUnica.INSTANCE, actorId, motivo, ahora);
    }

    public void levantarSuspension(PoliticaCicloPublicacion politica, UUID actorId, Instant ahora) {
        exigirModerador(actorId, ahora);
        if (politica == null) {
            throw new ReglaNegocioException("DATO_INVALIDO", "El levantamiento está incompleto");
        }
        if (politica.esEstablecimiento()) {
            EstadoPublicacion siguiente = PoliticaEstadosEstablecimiento.trasLevantarSuspension(estado);
            transicionar(siguiente, null, actorId, ahora);
            restaurarUnidadesTrasEstablecimientoActivo(actorId, ahora);
            return;
        }
        transicionar(PoliticaEstadosOferta.trasLevantarSuspension(estado), null, actorId, ahora);
    }

    public void enviarUnidadARevision(UUID unidadId, Instant ahora) {
        exigirEstablecimientoOperableParaHabitacion(ahora);
        reemplazarUnidad(unidadId, unidad -> unidad.enviarARevision(anfitrionId.valor(), ahora));
    }

    public void aceptarUnidad(UUID unidadId, UUID actorId, Instant ahora) {
        exigirModerador(actorId, ahora);
        exigirEstablecimientoActivoParaPublicarHabitacion();
        reemplazarUnidad(unidadId, unidad -> unidad.aceptar(actorId, ahora));
    }

    public void rechazarUnidad(UUID unidadId, UUID actorId, String motivo, Instant ahora) {
        exigirModerador(actorId, ahora);
        String motivoLimpio = motivo(motivo);
        reemplazarUnidad(unidadId, unidad -> unidad.rechazar(motivoLimpio, actorId, ahora));
    }

    public void desactivarUnidad(UUID unidadId, Instant ahora) {
        if (ahora == null) {
            throw new ReglaNegocioException("DATO_INVALIDO", "La desactivación está incompleta");
        }
        reemplazarUnidad(unidadId, unidad -> unidad.desactivar(anfitrionId.valor(), ahora));
    }

    public void activarUnidad(UUID unidadId, Instant ahora) {
        if (ahora == null) {
            throw new ReglaNegocioException("DATO_INVALIDO", "La activación está incompleta");
        }
        reemplazarUnidad(unidadId, unidad -> unidad.activar(estado, anfitrionId.valor(), ahora));
    }

    public void suspenderUnidad(UUID unidadId, UUID actorId, String motivo, Instant ahora) {
        exigirModerador(actorId, ahora);
        String motivoLimpio = motivo(motivo);
        reemplazarUnidad(unidadId, unidad -> unidad.suspender(motivoLimpio, actorId, ahora));
    }

    public void levantarSuspensionUnidad(UUID unidadId, UUID actorId, Instant ahora) {
        exigirModerador(actorId, ahora);
        reemplazarUnidad(unidadId, unidad -> unidad.levantarSuspension(estado, actorId, ahora));
    }

    public boolean visibleEnCatalogo() {
        if (estado == EstadoPublicacion.PUBLICADO) {
            return true;
        }
        if (estado == EstadoPublicacion.ACTIVO) {
            return unidades.stream().anyMatch(UnidadReservable::visibleEnCatalogo);
        }
        return false;
    }

    private void cascadaUnidadesDesdeEstablecimiento(
            EstadoPublicacion nuevoEstado, UUID actorId, Instant ahora) {
        this.unidades = unidades.stream()
                .map(unidad -> unidad.aplicarCascadaEstablecimiento(nuevoEstado, actorId, ahora))
                .toList();
    }

    private void restaurarUnidadesTrasEstablecimientoActivo(UUID actorId, Instant ahora) {
        this.unidades = unidades.stream()
                .map(unidad -> unidad.restaurarTrasEstablecimientoActivo(actorId, ahora))
                .toList();
    }

    private void reemplazarUnidad(UUID unidadId, java.util.function.Function<UnidadReservable, UnidadReservable> cambio) {
        if (unidadId == null) {
            throw new ReglaNegocioException("DATO_INVALIDO", "La unidad es obligatoria");
        }
        List<UnidadReservable> nuevas = new ArrayList<>();
        boolean encontrada = false;
        for (UnidadReservable unidad : unidades) {
            if (unidad.id().equals(unidadId)) {
                nuevas.add(cambio.apply(unidad));
                encontrada = true;
            } else {
                nuevas.add(unidad);
            }
        }
        if (!encontrada) {
            throw new ReglaNegocioException("UNIDAD_NO_ENCONTRADA", "La habitación no existe");
        }
        this.unidades = List.copyOf(nuevas);
    }

    private void exigirEstablecimientoOperableParaHabitacion(Instant ahora) {
        if (ahora == null) {
            throw new ReglaNegocioException("DATO_INVALIDO", "El envío a revisión está incompleto");
        }
        if (estado != EstadoPublicacion.ACTIVO && estado != EstadoPublicacion.DESACTIVADO) {
            throw new ReglaNegocioException(
                    "PUBLICACION_NO_PERMITIDA",
                    "El establecimiento debe estar activado o desactivado para gestionar habitaciones");
        }
    }

    private void exigirEstablecimientoActivoParaPublicarHabitacion() {
        if (estado != EstadoPublicacion.ACTIVO) {
            throw new ReglaNegocioException(
                    "PUBLICACION_NO_PERMITIDA",
                    "El establecimiento debe estar activado para publicar la habitación");
        }
    }

    private void exigirModerador(UUID actorId, Instant ahora) {
        if (actorId == null || ahora == null) {
            throw new ReglaNegocioException("DATO_INVALIDO", "La decisión está incompleta");
        }
        if (actorId.equals(anfitrionId.valor())) {
            throw new ReglaNegocioException(
                    "PUBLICACION_NO_PERMITIDA", "El anfitrión no puede moderar su alojamiento");
        }
    }

    private void transicionar(EstadoPublicacion nuevo, String motivo, UUID actorId, Instant ahora) {
        this.estado = nuevo;
        historial.add(HistorialPublicacion.decidir(nuevo, motivo, actorId, ahora));
    }

    private static String motivo(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new ReglaNegocioException("DATO_INVALIDO", "El motivo es obligatorio");
        }
        String limpio = valor.trim();
        if (limpio.length() > 500) {
            throw new ReglaNegocioException("DATO_INVALIDO", "El motivo admite hasta 500 caracteres");
        }
        return limpio;
    }

    private void exigirRegistroParcial() {
        if (!estado.admiteRegistroParcial()) {
            throw new ReglaNegocioException(
                    "PUBLICACION_NO_PERMITIDA", "El alojamiento ya salió del registro");
        }
    }

    public AlojamientoId id() {
        return id;
    }

    public AnfitrionId anfitrionId() {
        return anfitrionId;
    }

    public TipoPropiedadId tipoPropiedadId() {
        return tipoPropiedadId;
    }

    public Titulo titulo() {
        return titulo;
    }

    public DescripcionAlojamiento descripcion() {
        return descripcion;
    }

    public Politicas politicas() {
        return politicas;
    }

    public HorarioEstadia horarioEntrada() {
        return horarioEntrada;
    }

    public HorarioEstadia horarioSalida() {
        return horarioSalida;
    }

    public Ubicacion ubicacion() {
        return ubicacion;
    }

    public EstadoPublicacion estado() {
        return estado;
    }

    public Instant fechaCreacion() {
        return fechaCreacion;
    }

    public List<Fotografia> fotografias() {
        return fotografias;
    }

    public Set<ServicioId> servicios() {
        return servicios;
    }

    public List<UnidadReservable> unidades() {
        return unidades;
    }

    public List<HistorialPublicacion> historial() {
        return List.copyOf(historial);
    }
}
