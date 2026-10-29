package com.alojate.identidad.infrastructure.adapters.output.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "restriccion_usuario")
public class RestriccionUsuarioJpa {

    @Id
    @Column(name = "id_restriccion")
    private UUID idRestriccion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private UsuarioJpa usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_administrador", nullable = false)
    private UsuarioJpa administrador;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_permiso", nullable = false)
    private PermisoJpa permiso;

    @Column(nullable = false, length = 500)
    private String motivo;

    @Column(name = "fecha_inicio", nullable = false)
    private Instant fechaInicio;

    @Column(name = "fecha_fin")
    private Instant fechaFin;

    @Column(nullable = false)
    private boolean activa;

    public UUID getIdRestriccion() {
        return idRestriccion;
    }

    public void setIdRestriccion(UUID idRestriccion) {
        this.idRestriccion = idRestriccion;
    }

    public UsuarioJpa getUsuario() {
        return usuario;
    }

    public void setUsuario(UsuarioJpa usuario) {
        this.usuario = usuario;
    }

    public UsuarioJpa getAdministrador() {
        return administrador;
    }

    public void setAdministrador(UsuarioJpa administrador) {
        this.administrador = administrador;
    }

    public PermisoJpa getPermiso() {
        return permiso;
    }

    public void setPermiso(PermisoJpa permiso) {
        this.permiso = permiso;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public Instant getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(Instant fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public Instant getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(Instant fechaFin) {
        this.fechaFin = fechaFin;
    }

    public boolean isActiva() {
        return activa;
    }

    public void setActiva(boolean activa) {
        this.activa = activa;
    }
}
