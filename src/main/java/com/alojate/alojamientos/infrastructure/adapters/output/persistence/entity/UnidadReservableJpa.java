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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "unidad_reservable")
public class UnidadReservableJpa {

    @Id
    @Column(name = "id_unidad")
    private UUID idUnidad;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_alojamiento", nullable = false)
    private AlojamientoJpa alojamiento;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_tipo_espacio", nullable = false)
    private TipoEspacioJpa tipoEspacio;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(nullable = false, columnDefinition = "text")
    private String descripcion;

    @Column(name = "capacidad_huespedes", nullable = false)
    private int capacidadHuespedes;

    @Column(name = "cantidad_dormitorios", nullable = false)
    private int cantidadDormitorios;

    @Column(name = "cantidad_camas", nullable = false)
    private int cantidadCamas;

    @Column(name = "cantidad_banos", nullable = false, precision = 3, scale = 1)
    private BigDecimal cantidadBanos;

    @Column(name = "inventario_total", nullable = false)
    private int inventarioTotal;

    @Column(name = "precio_noche", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioNoche;

    @Column(nullable = false, length = 20)
    private String estado;

    @OneToMany(mappedBy = "unidad", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orden ASC")
    private List<FotografiaJpa> fotografias = new ArrayList<>();

    @OneToMany(mappedBy = "unidad", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UnidadServicioJpa> servicios = new HashSet<>();

    @OneToMany(mappedBy = "unidad", cascade = CascadeType.ALL)
    @OrderBy("fecha ASC")
    private List<HistorialPublicacionJpa> historial = new ArrayList<>();

    public UUID getIdUnidad() {
        return idUnidad;
    }

    public void setIdUnidad(UUID idUnidad) {
        this.idUnidad = idUnidad;
    }

    public AlojamientoJpa getAlojamiento() {
        return alojamiento;
    }

    public void setAlojamiento(AlojamientoJpa alojamiento) {
        this.alojamiento = alojamiento;
    }

    public TipoEspacioJpa getTipoEspacio() {
        return tipoEspacio;
    }

    public void setTipoEspacio(TipoEspacioJpa tipoEspacio) {
        this.tipoEspacio = tipoEspacio;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public int getCapacidadHuespedes() {
        return capacidadHuespedes;
    }

    public void setCapacidadHuespedes(int capacidadHuespedes) {
        this.capacidadHuespedes = capacidadHuespedes;
    }

    public int getCantidadDormitorios() {
        return cantidadDormitorios;
    }

    public void setCantidadDormitorios(int cantidadDormitorios) {
        this.cantidadDormitorios = cantidadDormitorios;
    }

    public int getCantidadCamas() {
        return cantidadCamas;
    }

    public void setCantidadCamas(int cantidadCamas) {
        this.cantidadCamas = cantidadCamas;
    }

    public BigDecimal getCantidadBanos() {
        return cantidadBanos;
    }

    public void setCantidadBanos(BigDecimal cantidadBanos) {
        this.cantidadBanos = cantidadBanos;
    }

    public int getInventarioTotal() {
        return inventarioTotal;
    }

    public void setInventarioTotal(int inventarioTotal) {
        this.inventarioTotal = inventarioTotal;
    }

    public BigDecimal getPrecioNoche() {
        return precioNoche;
    }

    public void setPrecioNoche(BigDecimal precioNoche) {
        this.precioNoche = precioNoche;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public List<FotografiaJpa> getFotografias() {
        return fotografias;
    }

    public Set<UnidadServicioJpa> getServicios() {
        return servicios;
    }

    public List<HistorialPublicacionJpa> getHistorial() {
        return historial;
    }
}
