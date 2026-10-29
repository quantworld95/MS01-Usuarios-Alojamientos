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
@Table(name = "tipo_espacio")
public class TipoEspacioJpa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_espacio")
    private Integer idTipoEspacio;

    @Column(nullable = false, length = 80)
    private String nombre;

    @OneToMany(mappedBy = "tipoEspacio")
    private Set<UnidadReservableJpa> unidades = new HashSet<>();

    public Integer getIdTipoEspacio() {
        return idTipoEspacio;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Set<UnidadReservableJpa> getUnidades() {
        return unidades;
    }
}
