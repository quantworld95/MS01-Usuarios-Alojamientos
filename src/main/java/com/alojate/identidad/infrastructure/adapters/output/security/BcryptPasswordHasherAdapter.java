package com.alojate.identidad.infrastructure.adapters.output.security;

import com.alojate.identidad.application.ports.output.PasswordHasherPort;
import com.alojate.identidad.domain.usuario.valueobject.Contrasena;
import com.alojate.identidad.domain.usuario.valueobject.PasswordHash;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BcryptPasswordHasherAdapter implements PasswordHasherPort {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public PasswordHash cifrar(Contrasena contrasena) {
        return new PasswordHash(encoder.encode(contrasena.valor()));
    }

    @Override
    public boolean coincide(Contrasena contrasena, PasswordHash hash) {
        return encoder.matches(contrasena.valor(), hash.valor());
    }
}
