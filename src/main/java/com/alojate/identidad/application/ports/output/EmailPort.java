package com.alojate.identidad.application.ports.output;

import com.alojate.identidad.domain.usuario.valueobject.CodigoVerificacion;
import com.alojate.identidad.domain.usuario.valueobject.Email;

public interface EmailPort {

    void enviarCodigoVerificacion(Email email, CodigoVerificacion codigo);
}
