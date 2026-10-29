package com.alojate.identidad.infrastructure.adapters.output.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "permiso")
public class PermisoJpa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_permiso")
    private Integer idPermiso;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_modulo", nullable = false)
    private ModuloJpa modulo;

    @Column(nullable = false, length = 80)
    private String codigo;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    @Column(nullable = false)
    private boolean asignable;

    @OneToMany(mappedBy = "permiso")
    private Set<RolPermisoJpa> roles = new HashSet<>();

    @OneToMany(mappedBy = "permiso")
    private Set<RestriccionUsuarioJpa> restricciones = new HashSet<>();

    public Integer getIdPermiso() {
        return idPermiso;
    }

    public ModuloJpa getModulo() {
        return modulo;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public boolean isAsignable() {
        return asignable;
    }

    public Set<RolPermisoJpa> getRoles() {
        return roles;
    }

    public Set<RestriccionUsuarioJpa> getRestricciones() {
        return restricciones;
    }
}
