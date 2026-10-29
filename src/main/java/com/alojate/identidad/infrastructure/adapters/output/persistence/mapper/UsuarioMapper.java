package com.alojate.identidad.infrastructure.adapters.output.persistence.mapper;

import com.alojate.identidad.domain.rol.aggregate.Rol;
import com.alojate.identidad.domain.rol.valueobject.AmbitoRol;
import com.alojate.identidad.domain.rol.valueobject.NombreRol;
import com.alojate.identidad.domain.rol.valueobject.RolId;
import com.alojate.identidad.domain.usuario.aggregate.Usuario;
import com.alojate.identidad.domain.usuario.valueobject.CodigoVerificacion;
import com.alojate.identidad.domain.usuario.valueobject.Email;
import com.alojate.identidad.domain.usuario.valueobject.EstadoCuenta;
import com.alojate.identidad.domain.usuario.valueobject.NombreCompleto;
import com.alojate.identidad.domain.usuario.valueobject.PasswordHash;
import com.alojate.identidad.domain.usuario.valueobject.Telefono;
import com.alojate.identidad.domain.usuario.valueobject.TipoCuenta;
import com.alojate.identidad.domain.usuario.valueobject.UsuarioId;
import com.alojate.identidad.infrastructure.adapters.output.persistence.entity.RolJpa;
import com.alojate.identidad.infrastructure.adapters.output.persistence.entity.UsuarioJpa;

public final class UsuarioMapper {

    public UsuarioJpa aJpa(Usuario usuario, RolJpa rol) {
        UsuarioJpa jpa = new UsuarioJpa();
        jpa.setIdUsuario(usuario.id().valor());
        jpa.setRol(rol);
        jpa.setTipoCuenta(usuario.tipoCuenta().name());
        jpa.setNombre(usuario.nombre().nombre());
        jpa.setApellido(usuario.nombre().apellido());
        jpa.setEmail(usuario.email().valor());
        jpa.setTelefono(usuario.telefono().valor());
        jpa.setPasswordHash(usuario.passwordHash().valor());
        jpa.setEstado(usuario.estado().name());
        jpa.setFechaRegistro(usuario.fechaRegistro());
        if (usuario.codigoVerificacion() == null) {
            jpa.setCodigoVerificacion(null);
            jpa.setCodigoVerificacionVence(null);
        } else {
            jpa.setCodigoVerificacion(usuario.codigoVerificacion().codigo());
            jpa.setCodigoVerificacionVence(usuario.codigoVerificacion().vence());
        }
        jpa.setSecretoMfa(usuario.secretoMfa());
        return jpa;
    }

    public Usuario aDominio(UsuarioJpa jpa) {
        return Usuario.reconstituir(
                new UsuarioId(jpa.getIdUsuario()),
                new NombreCompleto(jpa.getNombre(), jpa.getApellido()),
                new Email(jpa.getEmail()),
                new Telefono(jpa.getTelefono()),
                new PasswordHash(jpa.getPasswordHash()),
                TipoCuenta.valueOf(jpa.getTipoCuenta()),
                EstadoCuenta.valueOf(jpa.getEstado()),
                jpa.getRol() == null ? null : new RolId(jpa.getRol().getIdRol()),
                jpa.getFechaRegistro(),
                codigo(jpa),
                jpa.getSecretoMfa());
    }

    public Rol aDominio(RolJpa jpa) {
        return Rol.reconstituir(
                new RolId(jpa.getIdRol()),
                new NombreRol(jpa.getNombre()),
                AmbitoRol.valueOf(jpa.getAmbito()),
                jpa.isEsSistema(),
                jpa.isActivo());
    }

    private CodigoVerificacion codigo(UsuarioJpa jpa) {
        if (jpa.getCodigoVerificacion() == null || jpa.getCodigoVerificacionVence() == null) {
            return null;
        }
        return new CodigoVerificacion(jpa.getCodigoVerificacion(), jpa.getCodigoVerificacionVence());
    }
}
