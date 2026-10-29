package com.alojate.identidad.infrastructure.adapters.output.persistence.entity;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class RolPermisoJpaId implements Serializable {

    private Integer idRol;

    private Integer idPermiso;

    public Integer getIdRol() {
        return idRol;
    }

    public void setIdRol(Integer idRol) {
        this.idRol = idRol;
    }

    public Integer getIdPermiso() {
        return idPermiso;
    }

    public void setIdPermiso(Integer idPermiso) {
        this.idPermiso = idPermiso;
    }

    @Override
    public boolean equals(Object otro) {
        if (this == otro) {
            return true;
        }
        if (!(otro instanceof RolPermisoJpaId id)) {
            return false;
        }
        return Objects.equals(idRol, id.idRol) && Objects.equals(idPermiso, id.idPermiso);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idRol, idPermiso);
    }
}
