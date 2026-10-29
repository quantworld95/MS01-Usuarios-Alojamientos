package com.alojate.alojamientos.infrastructure.config;

import com.alojate.alojamientos.application.ports.output.Reloj;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class RelojDeAlojamientos implements Reloj {

    @Override
    public Instant ahora() {
        return Instant.now();
    }
}
