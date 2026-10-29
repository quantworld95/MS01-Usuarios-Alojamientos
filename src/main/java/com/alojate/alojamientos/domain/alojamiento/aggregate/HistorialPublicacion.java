package com.alojate.alojamientos.domain.alojamiento.aggregate;

import com.alojate.alojamientos.domain.alojamiento.valueobject.EstadoPublicacion;
import com.alojate.alojamientos.domain.alojamiento.valueobject.EstadoUnidad;
import com.alojate.alojamientos.domain.exception.ReglaNegocioException;

import java.time.Instant;
import java.util.UUID;

public final class HistorialPublicacion {

    private final UUID id;
    private final String estado;
    private final String motivo;
    private final UUID actorId;
    private final Instant fecha;

    private HistorialPublicacion(
            UUID id, String estado, String motivo, UUID actorId, Instant fecha) {
        if (id == null || estado == null || estado.isBlank() || actorId == null || fecha == null) {
            throw new ReglaNegocioException("DATO_INVALIDO", "El historial está incompleto");
        }
        this.id = id;
        this.estado = estado;
        this.motivo = motivo;
        this.actorId = actorId;
        this.fecha = fecha;
    }

    public static HistorialPublicacion abrir(EstadoPublicacion estado, UUID actorId, Instant fecha) {
        return new HistorialPublicacion(UUID.randomUUID(), nombre(estado), null, actorId, fecha);
    }

    public static HistorialPublicacion abrir(EstadoUnidad estado, UUID actorId, Instant fecha) {
        return new HistorialPublicacion(UUID.randomUUID(), nombre(estado), null, actorId, fecha);
    }

    public static HistorialPublicacion decidir(EstadoPublicacion estado, String motivo, UUID actorId, Instant fecha) {
        return new HistorialPublicacion(UUID.randomUUID(), nombre(estado), motivo, actorId, fecha);
    }

    public static HistorialPublicacion decidir(EstadoUnidad estado, String motivo, UUID actorId, Instant fecha) {
        return new HistorialPublicacion(UUID.randomUUID(), nombre(estado), motivo, actorId, fecha);
    }

    public static HistorialPublicacion reconstituir(
            UUID id, String estado, String motivo, UUID actorId, Instant fecha) {
        return new HistorialPublicacion(id, estado, motivo, actorId, fecha);
    }

    public UUID id() {
        return id;
    }

    public String estado() {
        return estado;
    }

    public String motivo() {
        return motivo;
    }

    public UUID actorId() {
        return actorId;
    }

    public Instant fecha() {
        return fecha;
    }

    private static String nombre(EstadoPublicacion estado) {
        if (estado == null) {
            throw new ReglaNegocioException("DATO_INVALIDO", "El historial está incompleto");
        }
        return estado.name();
    }

    private static String nombre(EstadoUnidad estado) {
        if (estado == null) {
            throw new ReglaNegocioException("DATO_INVALIDO", "El historial está incompleto");
        }
        return estado.name();
    }
}
