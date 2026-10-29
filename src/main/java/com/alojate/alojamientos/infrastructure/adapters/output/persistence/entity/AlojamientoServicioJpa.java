package com.alojate.alojamientos.infrastructure.adapters.output.persistence.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "alojamiento_servicio")
public class AlojamientoServicioJpa {

    @EmbeddedId
    private AlojamientoServicioJpaId id = new AlojamientoServicioJpaId();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("idAlojamiento")
    @JoinColumn(name = "id_alojamiento")
    private AlojamientoJpa alojamiento;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("idServicio")
    @JoinColumn(name = "id_servicio")
    private ServicioJpa servicio;

    public AlojamientoServicioJpaId getId() {
        return id;
    }

    public AlojamientoJpa getAlojamiento() {
        return alojamiento;
    }

    public void setAlojamiento(AlojamientoJpa alojamiento) {
        this.alojamiento = alojamiento;
    }

    public ServicioJpa getServicio() {
        return servicio;
    }

    public void setServicio(ServicioJpa servicio) {
        this.servicio = servicio;
    }
}
