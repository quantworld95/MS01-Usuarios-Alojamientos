package com.alojate.alojamientos.infrastructure.adapters.output.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tipo_propiedad")
public class TipoPropiedadJpa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_propiedad")
    private Integer idTipoPropiedad;

    @Column(nullable = false, length = 80)
    private String nombre;

    @OneToMany(mappedBy = "tipoPropiedad")
    private Set<AlojamientoJpa> alojamientos = new HashSet<>();

    public Integer getIdTipoPropiedad() {
        return idTipoPropiedad;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Set<AlojamientoJpa> getAlojamientos() {
        return alojamientos;
    }
}
