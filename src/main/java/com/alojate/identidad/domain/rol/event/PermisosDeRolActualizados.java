package com.alojate.identidad.domain.rol.event;

import com.alojate.identidad.domain.modulo.valueobject.PermisoId;
import com.alojate.identidad.domain.rol.valueobject.RolId;

import java.util.Set;

public record PermisosDeRolActualizados(RolId rolId, Set<PermisoId> permisos) implements EventoDeRol {

    public PermisosDeRolActualizados {
        permisos = Set.copyOf(permisos);
    }
}
