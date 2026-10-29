package com.alojate.identidad.domain.rol.aggregate;

import com.alojate.identidad.domain.exception.ReglaNegocioException;
import com.alojate.identidad.domain.modulo.valueobject.PermisoId;

public record RolPermiso(PermisoId permisoId) {

    public RolPermiso {
        if (permisoId == null) {
            throw new ReglaNegocioException("DATO_INVALIDO", "El permiso es obligatorio");
        }
    }
}
