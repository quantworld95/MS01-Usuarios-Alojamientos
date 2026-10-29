package com.alojate.identidad.application.ports.input;

import com.alojate.identidad.application.dto.CorreoVerificadoResponse;
import com.alojate.identidad.application.dto.VerificarCorreoCommand;

public interface VerificarCorreo {

    CorreoVerificadoResponse verificar(VerificarCorreoCommand comando);
}
