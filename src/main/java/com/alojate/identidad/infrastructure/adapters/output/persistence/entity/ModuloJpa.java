package com.alojate.identidad.infrastructure.adapters.output.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "modulo")
public class ModuloJpa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_modulo")
    private Integer idModulo;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    @Column(nullable = false)
    private int orden;

    @OneToMany(mappedBy = "modulo")
    @OrderBy("codigo ASC")
    private List<PermisoJpa> permisos = new ArrayList<>();

    public Integer getIdModulo() {
        return idModulo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public int getOrden() {
        return orden;
    }

    public List<PermisoJpa> getPermisos() {
        return permisos;
    }
}
