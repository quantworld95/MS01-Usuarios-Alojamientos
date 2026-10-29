package com.alojate.alojamientos.infrastructure.adapters.output.persistence.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "unidad_servicio")
public class UnidadServicioJpa {

    @EmbeddedId
    private UnidadServicioJpaId id = new UnidadServicioJpaId();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("idUnidad")
    @JoinColumn(name = "id_unidad")
    private UnidadReservableJpa unidad;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("idServicio")
    @JoinColumn(name = "id_servicio")
    private ServicioJpa servicio;

    public UnidadServicioJpaId getId() {
        return id;
    }

    public UnidadReservableJpa getUnidad() {
        return unidad;
    }

    public void setUnidad(UnidadReservableJpa unidad) {
        this.unidad = unidad;
    }

    public ServicioJpa getServicio() {
        return servicio;
    }

    public void setServicio(ServicioJpa servicio) {
        this.servicio = servicio;
    }
}
