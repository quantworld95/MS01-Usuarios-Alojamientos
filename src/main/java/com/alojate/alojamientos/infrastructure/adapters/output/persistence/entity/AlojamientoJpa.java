package com.alojate.alojamientos.infrastructure.adapters.output.persistence.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "alojamiento")
public class AlojamientoJpa {

    @Id
    @Column(name = "id_alojamiento")
    private UUID idAlojamiento;

    @Column(name = "id_anfitrion", nullable = false)
    private UUID idAnfitrion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_tipo_propiedad", nullable = false)
    private TipoPropiedadJpa tipoPropiedad;

    @Column(length = 140)
    private String titulo;

    @Column(columnDefinition = "text")
    private String descripcion;

    @Column(columnDefinition = "text")
    private String politicas;

    @Column(length = 200)
    private String direccion;

    @Column(length = 80)
    private String ciudad;

    @Column(length = 40)
    private String departamento;

    @Column(name = "horario_entrada", length = 10)
    private String horarioEntrada;

    @Column(name = "horario_salida", length = 10)
    private String horarioSalida;

    @Column(precision = 9, scale = 6)
    private BigDecimal latitud;

    @Column(precision = 9, scale = 6)
    private BigDecimal longitud;

    @Column(name = "estado_publicacion", nullable = false, length = 30)
    private String estadoPublicacion;

    @Column(name = "fecha_creacion", nullable = false)
    private Instant fechaCreacion;

    @OneToMany(mappedBy = "alojamiento", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orden ASC")
    private List<FotografiaJpa> fotografias = new ArrayList<>();

    @OneToMany(mappedBy = "alojamiento", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<AlojamientoServicioJpa> servicios = new HashSet<>();

    @OneToMany(mappedBy = "alojamiento", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UnidadReservableJpa> unidades = new ArrayList<>();

    @OneToMany(mappedBy = "alojamiento", cascade = CascadeType.ALL)
    @OrderBy("fecha ASC")
    private List<HistorialPublicacionJpa> historial = new ArrayList<>();

    public UUID getIdAlojamiento() {
        return idAlojamiento;
    }

    public void setIdAlojamiento(UUID idAlojamiento) {
        this.idAlojamiento = idAlojamiento;
    }

    public UUID getIdAnfitrion() {
        return idAnfitrion;
    }

    public void setIdAnfitrion(UUID idAnfitrion) {
        this.idAnfitrion = idAnfitrion;
    }

    public TipoPropiedadJpa getTipoPropiedad() {
        return tipoPropiedad;
    }

    public void setTipoPropiedad(TipoPropiedadJpa tipoPropiedad) {
        this.tipoPropiedad = tipoPropiedad;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getPoliticas() {
        return politicas;
    }

    public void setPoliticas(String politicas) {
        this.politicas = politicas;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getDepartamento() {
        return departamento;
    }

    public void setDepartamento(String departamento) {
        this.departamento = departamento;
    }

    public String getHorarioEntrada() {
        return horarioEntrada;
    }

    public void setHorarioEntrada(String horarioEntrada) {
        this.horarioEntrada = horarioEntrada;
    }

    public String getHorarioSalida() {
        return horarioSalida;
    }

    public void setHorarioSalida(String horarioSalida) {
        this.horarioSalida = horarioSalida;
    }

    public BigDecimal getLatitud() {
        return latitud;
    }

    public void setLatitud(BigDecimal latitud) {
        this.latitud = latitud;
    }

    public BigDecimal getLongitud() {
        return longitud;
    }

    public void setLongitud(BigDecimal longitud) {
        this.longitud = longitud;
    }

    public String getEstadoPublicacion() {
        return estadoPublicacion;
    }

    public void setEstadoPublicacion(String estadoPublicacion) {
        this.estadoPublicacion = estadoPublicacion;
    }

    public Instant getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Instant fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public List<FotografiaJpa> getFotografias() {
        return fotografias;
    }

    public Set<AlojamientoServicioJpa> getServicios() {
        return servicios;
    }

    public List<UnidadReservableJpa> getUnidades() {
        return unidades;
    }

    public List<HistorialPublicacionJpa> getHistorial() {
        return historial;
    }
}
