package com.alojate.identidad.domain.rol.service;

import com.alojate.identidad.domain.exception.ReglaNegocioException;
import com.alojate.identidad.domain.rol.aggregate.Rol;
import com.alojate.identidad.domain.rol.valueobject.AmbitoRol;
import com.alojate.identidad.domain.rol.valueobject.RolId;

public final class PoliticaAsignacionRol {

    public RolId asignarAlActivarCuentaPublica(Rol rol) {
        if (rol == null || !rol.esUsuarioPublicoAsignable()) {
            throw new ReglaNegocioException(
                    "ROL_NO_ASIGNABLE",
                    "La cuenta pública solo puede recibir el rol USUARIO_PUBLICO");
        }
        return rol.id();
    }

    public RolId asignarRolAdministrativo(Rol rol) {
        if (rol == null || rol.ambito() != AmbitoRol.CORPORATIVO || rol.nombre().esUsuarioPublico()) {
            throw new ReglaNegocioException(
                    "ROL_NO_ASIGNABLE", "El trabajador solo puede recibir un rol corporativo");
        }
        if (!rol.activo()) {
            throw new ReglaNegocioException("ROL_INACTIVO", "El rol está desactivado");
        }
        return rol.id();
    }
}
