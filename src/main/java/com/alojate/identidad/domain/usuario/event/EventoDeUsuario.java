package com.alojate.identidad.domain.usuario.event;

public sealed interface EventoDeUsuario permits UsuarioRegistrado, CorreoVerificado, TrabajadorIncorporado {
}
