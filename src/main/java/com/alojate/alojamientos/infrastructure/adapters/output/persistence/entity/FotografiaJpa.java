package com.alojate.alojamientos.infrastructure.adapters.output.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "fotografia")
public class FotografiaJpa {

    @Id
    @Column(name = "id_fotografia")
    private UUID idFotografia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_alojamiento")
    private AlojamientoJpa alojamiento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_unidad")
    private UnidadReservableJpa unidad;

    @Column(name = "identificador_almacen", nullable = false, length = 255)
    private String identificadorAlmacen;

    @Column(nullable = false, length = 500)
    private String url;

    @Column(name = "orden", nullable = false)
    private int orden;

    public UUID getIdFotografia() {
        return idFotografia;
    }

    public void setIdFotografia(UUID idFotografia) {
        this.idFotografia = idFotografia;
    }

    public AlojamientoJpa getAlojamiento() {
        return alojamiento;
    }

    public void setAlojamiento(AlojamientoJpa alojamiento) {
        this.alojamiento = alojamiento;
    }

    public UnidadReservableJpa getUnidad() {
        return unidad;
    }

    public void setUnidad(UnidadReservableJpa unidad) {
        this.unidad = unidad;
    }

    public String getIdentificadorAlmacen() {
        return identificadorAlmacen;
    }

    public void setIdentificadorAlmacen(String identificadorAlmacen) {
        this.identificadorAlmacen = identificadorAlmacen;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getOrden() {
        return orden;
    }

    public void setOrden(int orden) {
        this.orden = orden;
    }
}
