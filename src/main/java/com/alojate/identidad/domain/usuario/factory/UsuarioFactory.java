package com.alojate.identidad.domain.usuario.factory;

import com.alojate.identidad.domain.rol.aggregate.Rol;
import com.alojate.identidad.domain.usuario.aggregate.Usuario;
import com.alojate.identidad.domain.usuario.valueobject.Email;
import com.alojate.identidad.domain.usuario.valueobject.NombreCompleto;
import com.alojate.identidad.domain.usuario.valueobject.PasswordHash;
import com.alojate.identidad.domain.usuario.valueobject.Telefono;

import java.time.Instant;

public final class UsuarioFactory {

    public Usuario registrarPublico(
            NombreCompleto nombre,
            Email email,
            Telefono telefono,
            PasswordHash passwordHash,
            Instant ahora) {
        return Usuario.registrarPublico(nombre, email, telefono, passwordHash, ahora);
    }

    public Usuario registrarCorporativo(
            NombreCompleto nombre,
            Email email,
            Telefono telefono,
            PasswordHash passwordHash,
            Rol rol,
            Instant ahora) {
        return Usuario.registrarCorporativo(nombre, email, telefono, passwordHash, rol, ahora);
    }
}
