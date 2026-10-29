package com.alojate.identidad.domain.restriccion.aggregate;

import com.alojate.identidad.domain.exception.ReglaNegocioException;
import com.alojate.identidad.domain.modulo.valueobject.PermisoId;
import com.alojate.identidad.domain.usuario.valueobject.UsuarioId;

import java.time.Instant;
import java.util.UUID;

public final class RestriccionUsuario {

    private final UUID id;
    private final UsuarioId usuarioId;
    private final PermisoId permisoId;
    private final UsuarioId administradorId;
    private final String motivo;
    private final Instant fechaInicio;
    private Instant fechaFin;
    private boolean activa;

    private RestriccionUsuario(
            UUID id,
            UsuarioId usuarioId,
            PermisoId permisoId,
            UsuarioId administradorId,
            String motivo,
            Instant fechaInicio,
            Instant fechaFin,
            boolean activa) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.permisoId = permisoId;
        this.administradorId = administradorId;
        this.motivo = motivo;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.activa = activa;
    }

    public static RestriccionUsuario crear(
            UsuarioId usuarioId, PermisoId permisoId, UsuarioId administradorId, String motivo, Instant inicio) {
        return new RestriccionUsuario(
                UUID.randomUUID(),
                usuarioId,
                permisoId,
                administradorId,
                motivo(motivo),
                exigirInstantanea(inicio),
                null,
                true);
    }

    public static RestriccionUsuario reconstituir(
            UUID id,
            UsuarioId usuarioId,
            PermisoId permisoId,
            UsuarioId administradorId,
            String motivo,
            Instant fechaInicio,
            Instant fechaFin,
            boolean activa) {
        return new RestriccionUsuario(
                id, usuarioId, permisoId, administradorId, motivo, fechaInicio, fechaFin, activa);
    }

    public void levantar(Instant ahora) {
        if (!vigente(ahora)) {
            throw new ReglaNegocioException("RESTRICCION_NO_VIGENTE", "La restricción ya no está vigente");
        }
        this.activa = false;
        this.fechaFin = ahora;
    }

    public boolean vigente(Instant ahora) {
        if (ahora == null || !activa || fechaInicio.isAfter(ahora)) {
            return false;
        }
        return fechaFin == null || fechaFin.isAfter(ahora);
    }

    public UUID id() {
        return id;
    }

    public UsuarioId usuarioId() {
        return usuarioId;
    }

    public PermisoId permisoId() {
        return permisoId;
    }

    public UsuarioId administradorId() {
        return administradorId;
    }

    public String motivo() {
        return motivo;
    }

    public Instant fechaInicio() {
        return fechaInicio;
    }

    public Instant fechaFin() {
        return fechaFin;
    }

    public boolean activa() {
        return activa;
    }

    private static String motivo(String motivo) {
        if (motivo == null || motivo.isBlank() || motivo.trim().length() > 500) {
            throw new ReglaNegocioException("DATO_INVALIDO", "El motivo admite hasta 500 caracteres");
        }
        return motivo.trim();
    }

    private static Instant exigirInstantanea(Instant ahora) {
        if (ahora == null) {
            throw new ReglaNegocioException("DATO_INVALIDO", "La fecha de la operación es obligatoria");
        }
        return ahora;
    }
}
