package com.alojate.identidad.application.ports.input;

import com.alojate.identidad.application.dto.CambiarEstadoUsuarioPublicoCommand;
import com.alojate.identidad.application.dto.CrearRestriccionCommand;
import com.alojate.identidad.application.dto.RestriccionResponse;
import com.alojate.identidad.application.dto.UsuarioPublicoResponse;

import java.util.List;
import java.util.UUID;

public interface GestionarUsuarioPublico {

    List<UsuarioPublicoResponse> listar();

    UsuarioPublicoResponse obtener(UUID id);

    UsuarioPublicoResponse cambiarEstado(CambiarEstadoUsuarioPublicoCommand comando);

    RestriccionResponse restringir(CrearRestriccionCommand comando);

    RestriccionResponse levantar(UUID usuarioId, UUID restriccionId);
}
