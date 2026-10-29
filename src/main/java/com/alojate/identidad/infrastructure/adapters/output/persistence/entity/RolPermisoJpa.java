package com.alojate.identidad.infrastructure.adapters.output.persistence.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "rol_permiso")
public class RolPermisoJpa {

    @EmbeddedId
    private RolPermisoJpaId id = new RolPermisoJpaId();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("idRol")
    @JoinColumn(name = "id_rol")
    private RolJpa rol;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("idPermiso")
    @JoinColumn(name = "id_permiso")
    private PermisoJpa permiso;

    public RolPermisoJpaId getId() {
        return id;
    }

    public RolJpa getRol() {
        return rol;
    }

    public void setRol(RolJpa rol) {
        this.rol = rol;
    }

    public PermisoJpa getPermiso() {
        return permiso;
    }

    public void setPermiso(PermisoJpa permiso) {
        this.permiso = permiso;
    }
}
