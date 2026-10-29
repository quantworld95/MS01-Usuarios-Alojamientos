package com.alojate.identidad.domain.rol.service;

import com.alojate.identidad.domain.exception.ReglaNegocioException;
import com.alojate.identidad.domain.rol.aggregate.Rol;

public final class PoliticaProteccionRol {

    public void exigirModificable(Rol rol) {
        if (rol == null || rol.esSistema()) {
            throw new ReglaNegocioException("ROL_PROTEGIDO", "Un rol de sistema no se puede modificar");
        }
    }
}
