package com.alojate.identidad.application.ports.output;

import com.alojate.identidad.domain.usuario.valueobject.Contrasena;
import com.alojate.identidad.domain.usuario.valueobject.PasswordHash;

public interface PasswordHasherPort {

    PasswordHash cifrar(Contrasena contrasena);

    boolean coincide(Contrasena contrasena, PasswordHash hash);
}
