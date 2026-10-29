package com.alojate.identidad.infrastructure.config;

import com.alojate.identidad.application.ports.output.Reloj;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class RelojSistema implements Reloj {

    @Override
    public Instant ahora() {
        return Instant.now();
    }
}
