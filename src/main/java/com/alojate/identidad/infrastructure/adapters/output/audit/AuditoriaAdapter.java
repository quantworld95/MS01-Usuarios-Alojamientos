package com.alojate.identidad.infrastructure.adapters.output.audit;

import com.alojate.identidad.application.ports.output.AuditoriaPort;
import com.alojate.identidad.domain.modulo.valueobject.PermisoId;
import com.alojate.identidad.domain.rol.event.PermisosDeRolActualizados;
import com.alojate.identidad.domain.usuario.event.TrabajadorIncorporado;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class AuditoriaAdapter implements AuditoriaPort {

    private static final Logger REGISTRO = LoggerFactory.getLogger(AuditoriaAdapter.class);

    @Override
    public void registrar(PermisosDeRolActualizados evento) {
        String permisos = evento.permisos().stream()
                .map(PermisoId::valor)
                .sorted()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        REGISTRO.info("Auditoria de rol {}: permisos {}", evento.rolId().valor(), permisos);
    }

    @Override
    public void registrar(TrabajadorIncorporado evento) {
        REGISTRO.info(
                "Auditoria de trabajador {}: rol {}",
                evento.email().valor(),
                evento.rolId().valor());
    }
}
