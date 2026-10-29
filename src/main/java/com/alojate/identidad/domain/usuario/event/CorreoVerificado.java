package com.alojate.identidad.domain.usuario.event;

import com.alojate.identidad.domain.rol.valueobject.RolId;
import com.alojate.identidad.domain.usuario.valueobject.Email;
import com.alojate.identidad.domain.usuario.valueobject.UsuarioId;

import java.time.Instant;

public record CorreoVerificado(UsuarioId usuarioId, Email email, RolId rolId, Instant ocurridoEn)
        implements EventoDeUsuario {
}
