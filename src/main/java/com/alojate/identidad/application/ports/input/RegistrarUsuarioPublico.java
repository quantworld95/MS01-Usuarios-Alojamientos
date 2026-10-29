package com.alojate.identidad.application.ports.input;

import com.alojate.identidad.application.dto.RegistrarUsuarioCommand;
import com.alojate.identidad.application.dto.UsuarioRegistradoResponse;

public interface RegistrarUsuarioPublico {

    UsuarioRegistradoResponse registrar(RegistrarUsuarioCommand comando);
}
