package com.alojate.alojamientos.infrastructure.adapters.output.persistence.entity;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class AlojamientoServicioJpaId implements Serializable {

    private UUID idAlojamiento;

    private Integer idServicio;

    public UUID getIdAlojamiento() {
        return idAlojamiento;
    }

    public void setIdAlojamiento(UUID idAlojamiento) {
        this.idAlojamiento = idAlojamiento;
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
        if (!(otro instanceof AlojamientoServicioJpaId id)) {
            return false;
        }
        return Objects.equals(idAlojamiento, id.idAlojamiento) && Objects.equals(idServicio, id.idServicio);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idAlojamiento, idServicio);
    }
}
