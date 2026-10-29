package com.alojate.alojamientos.infrastructure.adapters.output.persistence.entity;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class UnidadServicioJpaId implements Serializable {

    private UUID idUnidad;

    private Integer idServicio;

    public UUID getIdUnidad() {
        return idUnidad;
    }

    public void setIdUnidad(UUID idUnidad) {
        this.idUnidad = idUnidad;
    }

    public Integer getIdServicio() {
        return idServicio;
    }

    public void setIdServicio(Integer idServicio) {
        this.idServicio = idServicio;
    }

    @Override
    public boolean equals(Object otro) {
        if (this == otro) {
            return true;
        }
        if (!(otro instanceof UnidadServicioJpaId id)) {
            return false;
        }
        return Objects.equals(idUnidad, id.idUnidad) && Objects.equals(idServicio, id.idServicio);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idUnidad, idServicio);
    }
}
