package com.alojate.identidad.domain.usuario.aggregate;

import com.alojate.identidad.domain.exception.ReglaNegocioException;
import com.alojate.identidad.domain.rol.aggregate.Rol;
import com.alojate.identidad.domain.rol.service.PoliticaAsignacionRol;
import com.alojate.identidad.domain.rol.valueobject.RolId;
import com.alojate.identidad.domain.usuario.event.CorreoVerificado;
import com.alojate.identidad.domain.usuario.event.EventoDeUsuario;
import com.alojate.identidad.domain.usuario.event.TrabajadorIncorporado;
import com.alojate.identidad.domain.usuario.event.UsuarioRegistrado;
import com.alojate.identidad.domain.usuario.valueobject.CodigoVerificacion;
import com.alojate.identidad.domain.usuario.valueobject.Email;
import com.alojate.identidad.domain.usuario.valueobject.EstadoCuenta;
import com.alojate.identidad.domain.usuario.valueobject.NombreCompleto;
import com.alojate.identidad.domain.usuario.valueobject.PasswordHash;
import com.alojate.identidad.domain.usuario.valueobject.Telefono;
import com.alojate.identidad.domain.usuario.valueobject.TipoCuenta;
import com.alojate.identidad.domain.usuario.valueobject.UsuarioId;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public final class Usuario {

    private final UsuarioId id;
    private NombreCompleto nombre;
    private Email email;
    private Telefono telefono;
    private final PasswordHash passwordHash;
    private final TipoCuenta tipoCuenta;
    private final Instant fechaRegistro;
    private EstadoCuenta estado;
    private RolId rolId;
    private CodigoVerificacion codigoVerificacion;
    private String secretoMfa;
    private final List<EventoDeUsuario> eventos = new ArrayList<>();

    private Usuario(
            UsuarioId id,
            NombreCompleto nombre,
            Email email,
            Telefono telefono,
            PasswordHash passwordHash,
            TipoCuenta tipoCuenta,
            EstadoCuenta estado,
            RolId rolId,
            Instant fechaRegistro,
            CodigoVerificacion codigoVerificacion,
            String secretoMfa) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.telefono = telefono;
        this.passwordHash = passwordHash;
        this.tipoCuenta = tipoCuenta;
        this.estado = estado;
        this.rolId = rolId;
        this.fechaRegistro = fechaRegistro;
        this.codigoVerificacion = codigoVerificacion;
        this.secretoMfa = secretoMfa;
    }

    public static Usuario registrarPublico(
            NombreCompleto nombre,
            Email email,
            Telefono telefono,
            PasswordHash passwordHash,
            Instant ahora) {
        exigirInstantanea(ahora);
        Usuario usuario = new Usuario(
                UsuarioId.generar(),
                nombre,
                email,
                telefono,
                passwordHash,
                TipoCuenta.PUBLICA,
                EstadoCuenta.PENDIENTE_VERIFICACION,
                null,
                ahora,
                CodigoVerificacion.generar(ahora),
                null);
        usuario.eventos.add(new UsuarioRegistrado(usuario.id, usuario.email, ahora));
        return usuario;
    }

    public static Usuario registrarCorporativo(
            NombreCompleto nombre,
            Email email,
            Telefono telefono,
            PasswordHash passwordHash,
            Rol rol,
            Instant ahora) {
        exigirInstantanea(ahora);
        RolId rolId = new PoliticaAsignacionRol().asignarRolAdministrativo(rol);
        Usuario usuario = new Usuario(
                UsuarioId.generar(),
                nombre,
                email,
                telefono,
                passwordHash,
                TipoCuenta.CORPORATIVA,
                EstadoCuenta.ACTIVA,
                rolId,
                ahora,
                null,
                null);
        usuario.eventos.add(new TrabajadorIncorporado(usuario.id, usuario.email, rolId, ahora));
        return usuario;
    }

    public static Usuario reconstituir(
            UsuarioId id,
            NombreCompleto nombre,
            Email email,
            Telefono telefono,
            PasswordHash passwordHash,
            TipoCuenta tipoCuenta,
            EstadoCuenta estado,
            RolId rolId,
            Instant fechaRegistro,
            CodigoVerificacion codigoVerificacion) {
        return reconstituir(
                id, nombre, email, telefono, passwordHash, tipoCuenta, estado, rolId, fechaRegistro, codigoVerificacion, null);
    }

    public static Usuario reconstituir(
            UsuarioId id,
            NombreCompleto nombre,
            Email email,
            Telefono telefono,
            PasswordHash passwordHash,
            TipoCuenta tipoCuenta,
            EstadoCuenta estado,
            RolId rolId,
            Instant fechaRegistro,
            CodigoVerificacion codigoVerificacion,
            String secretoMfa) {
        return new Usuario(
                id,
                nombre,
                email,
                telefono,
                passwordHash,
                tipoCuenta,
                estado,
                rolId,
                fechaRegistro,
                codigoVerificacion,
                secretoMfa);
    }

    public void verificarCorreo(String codigo, Instant ahora, Rol rol) {
        exigirPendiente();
        exigirInstantanea(ahora);
        codigoVerificacion.exigirValido(codigo, ahora);
        rolId = new PoliticaAsignacionRol().asignarAlActivarCuentaPublica(rol);
        estado = EstadoCuenta.ACTIVA;
        codigoVerificacion = null;
        eventos.add(new CorreoVerificado(id, email, rolId, ahora));
    }

    public void reenviarVerificacion(Instant ahora) {
        exigirPendiente();
        exigirInstantanea(ahora);
        codigoVerificacion = CodigoVerificacion.generar(ahora);
    }

    public void modificarDatos(NombreCompleto nombre, Email email, Telefono telefono) {
        exigirCorporativo();
        this.nombre = nombre;
        this.email = email;
        this.telefono = telefono;
    }

    public void asignarRol(Rol rol) {
        exigirCorporativo();
        this.rolId = new PoliticaAsignacionRol().asignarRolAdministrativo(rol);
    }

    public void suspender() {
        exigirPublico();
        if (estado == EstadoCuenta.SUSPENDIDA) {
            return;
        }
        if (estado != EstadoCuenta.ACTIVA) {
            throw new ReglaNegocioException("CUENTA_NO_ACTIVA", "Solo se puede suspender una cuenta activa");
        }
        estado = EstadoCuenta.SUSPENDIDA;
    }

    public void activar() {
        exigirPublico();
        if (estado == EstadoCuenta.ACTIVA) {
            return;
        }
        if (estado != EstadoCuenta.SUSPENDIDA && estado != EstadoCuenta.DESACTIVADA) {
            throw new ReglaNegocioException(
                    "CUENTA_NO_ACTIVABLE", "Solo se puede activar una cuenta suspendida o desactivada");
        }
        estado = EstadoCuenta.ACTIVA;
    }

    public void desactivar() {
        if (estado == EstadoCuenta.DESACTIVADA) {
            return;
        }
        if (tipoCuenta == TipoCuenta.CORPORATIVA) {
            if (estado != EstadoCuenta.ACTIVA) {
                throw new ReglaNegocioException("CUENTA_NO_ACTIVA", "Solo se puede desactivar una cuenta activa");
            }
        } else if (estado != EstadoCuenta.ACTIVA && estado != EstadoCuenta.SUSPENDIDA) {
            throw new ReglaNegocioException(
                    "CUENTA_NO_ACTIVA", "Solo se puede desactivar una cuenta activa o suspendida");
        }
        estado = EstadoCuenta.DESACTIVADA;
    }

    public void reactivar() {
        exigirCorporativo();
        if (estado == EstadoCuenta.ACTIVA) {
            return;
        }
        if (estado != EstadoCuenta.DESACTIVADA) {
            throw new ReglaNegocioException(
                    "CUENTA_NO_DESACTIVADA", "Solo se puede reactivar una cuenta desactivada");
        }
        estado = EstadoCuenta.ACTIVA;
    }

    public void exigirSesionAdministrativa() {
        if (tipoCuenta != TipoCuenta.CORPORATIVA) {
            throw new ReglaNegocioException(
                    "CUENTA_PUBLICA", "Una cuenta pública no puede entrar al portal administrativo");
        }
        exigirPuedeIniciarSesion();
    }

    public void exigirSesionPublica() {
        if (tipoCuenta != TipoCuenta.PUBLICA) {
            throw new ReglaNegocioException(
                    "CUENTA_CORPORATIVA", "Una cuenta corporativa no puede iniciar sesión pública");
        }
        exigirPuedeIniciarSesion();
    }

    private void exigirPuedeIniciarSesion() {
        if (estado == EstadoCuenta.SUSPENDIDA) {
            throw new ReglaNegocioException("CUENTA_SUSPENDIDA", "La cuenta está suspendida");
        }
        if (estado == EstadoCuenta.DESACTIVADA) {
            throw new ReglaNegocioException("CUENTA_DESACTIVADA", "La cuenta está desactivada");
        }
        if (estado != EstadoCuenta.ACTIVA) {
            throw new ReglaNegocioException("CUENTA_PENDIENTE", "La cuenta todavía no está activa");
        }
    }

    public List<EventoDeUsuario> eventos() {
        return List.copyOf(eventos);
    }

    public UsuarioId id() {
        return id;
    }

    public NombreCompleto nombre() {
        return nombre;
    }

    public Email email() {
        return email;
    }

    public Telefono telefono() {
        return telefono;
    }

    public PasswordHash passwordHash() {
        return passwordHash;
    }

    public TipoCuenta tipoCuenta() {
        return tipoCuenta;
    }

    public EstadoCuenta estado() {
        return estado;
    }

    public RolId rolId() {
        return rolId;
    }

    public Instant fechaRegistro() {
        return fechaRegistro;
    }

    public CodigoVerificacion codigoVerificacion() {
        return codigoVerificacion;
    }

    public String secretoMfa() {
        return secretoMfa;
    }

    private void exigirPublico() {
        if (tipoCuenta != TipoCuenta.PUBLICA) {
            throw new ReglaNegocioException("CUENTA_CORPORATIVA", "La cuenta no es pública");
        }
    }

    private void exigirCorporativo() {
        if (tipoCuenta != TipoCuenta.CORPORATIVA) {
            throw new ReglaNegocioException("CUENTA_PUBLICA", "La cuenta no es corporativa");
        }
    }

    private void exigirPendiente() {
        if (estado != EstadoCuenta.PENDIENTE_VERIFICACION) {
            throw new ReglaNegocioException(
                    "CUENTA_NO_PENDIENTE", "La cuenta no está pendiente de verificación");
        }
    }

    private static void exigirInstantanea(Instant ahora) {
        if (ahora == null) {
            throw new ReglaNegocioException("DATO_INVALIDO", "La fecha de la operación es obligatoria");
        }
    }
}
