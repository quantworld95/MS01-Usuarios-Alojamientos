package com.alojate.identidad.application.ports.output;

import com.alojate.identidad.domain.rol.event.PermisosDeRolActualizados;
import com.alojate.identidad.domain.usuario.event.TrabajadorIncorporado;

public interface AuditoriaPort {

    void registrar(PermisosDeRolActualizados evento);

    void registrar(TrabajadorIncorporado evento);
}
