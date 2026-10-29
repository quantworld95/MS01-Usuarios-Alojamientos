package com.alojate.identidad.infrastructure.adapters.output.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "usuario")
public class UsuarioJpa {

    @Id
    @Column(name = "id_usuario")
    private UUID idUsuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_rol")
    private RolJpa rol;

    @Column(name = "tipo_cuenta", nullable = false)
    private String tipoCuenta;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String apellido;

    @Column(nullable = false)
    private String email;

    private String telefono;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String estado;

    @Column(name = "fecha_registro", nullable = false)
    private Instant fechaRegistro;

    @Column(name = "codigo_verificacion")
    private String codigoVerificacion;

    @Column(name = "codigo_verificacion_vence")
    private Instant codigoVerificacionVence;

    @Column(name = "secreto_mfa")
    private String secretoMfa;

    @OneToMany(mappedBy = "usuario")
    private Set<RestriccionUsuarioJpa> restriccionesRecibidas = new HashSet<>();

    @OneToMany(mappedBy = "administrador")
    private Set<RestriccionUsuarioJpa> restriccionesAplicadas = new HashSet<>();

    public UUID getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(UUID idUsuario) {
        this.idUsuario = idUsuario;
    }

    public RolJpa getRol() {
        return rol;
    }

    public void setRol(RolJpa rol) {
        this.rol = rol;
    }

    public String getTipoCuenta() {
        return tipoCuenta;
    }

    public void setTipoCuenta(String tipoCuenta) {
        this.tipoCuenta = tipoCuenta;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Instant getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(Instant fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public String getCodigoVerificacion() {
        return codigoVerificacion;
    }

    public void setCodigoVerificacion(String codigoVerificacion) {
        this.codigoVerificacion = codigoVerificacion;
    }

    public Instant getCodigoVerificacionVence() {
        return codigoVerificacionVence;
    }

    public void setCodigoVerificacionVence(Instant codigoVerificacionVence) {
        this.codigoVerificacionVence = codigoVerificacionVence;
    }

    public String getSecretoMfa() {
        return secretoMfa;
    }

    public void setSecretoMfa(String secretoMfa) {
        this.secretoMfa = secretoMfa;
    }

    public Set<RestriccionUsuarioJpa> getRestriccionesRecibidas() {
        return restriccionesRecibidas;
    }

    public Set<RestriccionUsuarioJpa> getRestriccionesAplicadas() {
        return restriccionesAplicadas;
    }
}
