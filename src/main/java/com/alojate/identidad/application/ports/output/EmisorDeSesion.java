package com.alojate.identidad.application.ports.output;

import com.alojate.identidad.domain.usuario.valueobject.UsuarioId;

import java.util.List;

public interface EmisorDeSesion {

    String emitir(UsuarioId usuarioId, String contexto);

    default String emitir(UsuarioId usuarioId, String contexto, List<String> permisos) {
        return emitir(usuarioId, contexto);
    }
}
