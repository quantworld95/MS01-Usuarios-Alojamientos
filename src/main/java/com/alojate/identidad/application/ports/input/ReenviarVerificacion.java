package com.alojate.identidad.application.ports.input;

import com.alojate.identidad.application.dto.ReenviarVerificacionCommand;
import com.alojate.identidad.application.dto.VerificacionReenviadaResponse;

public interface ReenviarVerificacion {

    VerificacionReenviadaResponse reenviar(ReenviarVerificacionCommand comando);
}
