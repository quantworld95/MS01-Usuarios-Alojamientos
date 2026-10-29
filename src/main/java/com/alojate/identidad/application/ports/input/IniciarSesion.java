package com.alojate.identidad.application.ports.input;

import com.alojate.identidad.application.dto.IniciarSesionCommand;
import com.alojate.identidad.application.dto.SesionResponse;

public interface IniciarSesion {

    SesionResponse iniciar(IniciarSesionCommand comando);
}
