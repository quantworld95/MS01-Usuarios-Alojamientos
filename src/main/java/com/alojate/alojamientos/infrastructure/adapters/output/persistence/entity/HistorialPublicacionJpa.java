package com.alojate.alojamientos.infrastructure.adapters.output.persistence.entity;

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
@Table(name = "historial_publicacion")
public class HistorialPublicacionJpa {

    @Id
    @Column(name = "id_historial")
    private UUID idHistorial;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_alojamiento")
    private AlojamientoJpa alojamiento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_unidad")
    private UnidadReservableJpa unidad;

    @Column(nullable = false, length = 30)
    private String estado;

    @Column(length = 500)
    private String motivo;

    @Column(name = "id_actor", nullable = false)
    private UUID idActor;

    @Column(nullable = false)
    private Instant fecha;

    public UUID getIdHistorial() {
        return idHistorial;
    }

    public void setIdHistorial(UUID idHistorial) {
        this.idHistorial = idHistorial;
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

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public UUID getIdActor() {
        return idActor;
    }

    public void setIdActor(UUID idActor) {
        this.idActor = idActor;
    }

    public Instant getFecha() {
        return fecha;
    }

    public void setFecha(Instant fecha) {
        this.fecha = fecha;
    }
}
