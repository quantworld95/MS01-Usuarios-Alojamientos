package com.alojate.identidad.domain.usuario.event;

import com.alojate.identidad.domain.usuario.valueobject.Email;
import com.alojate.identidad.domain.usuario.valueobject.UsuarioId;

import java.time.Instant;

public record UsuarioRegistrado(UsuarioId usuarioId, Email email, Instant ocurridoEn) implements EventoDeUsuario {
}
