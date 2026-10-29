package com.alojate.identidad.infrastructure.adapters.output.persistence.mapper;

import com.alojate.identidad.domain.modulo.valueobject.PermisoId;
import com.alojate.identidad.domain.rol.aggregate.Rol;
import com.alojate.identidad.domain.rol.valueobject.AmbitoRol;
import com.alojate.identidad.domain.rol.valueobject.DescripcionRol;
import com.alojate.identidad.domain.rol.valueobject.NombreRol;
import com.alojate.identidad.domain.rol.valueobject.RolId;
import com.alojate.identidad.infrastructure.adapters.output.persistence.entity.RolJpa;
import com.alojate.identidad.infrastructure.adapters.output.persistence.entity.RolPermisoJpa;

import java.util.Set;
import java.util.stream.Collectors;

public final class RolMapper {

    public Rol aDominio(RolJpa jpa) {
        Set<PermisoId> permisos = jpa.getPermisos().stream()
                .map(this::permisoId)
                .collect(Collectors.toUnmodifiableSet());
        return Rol.reconstituir(
                new RolId(jpa.getIdRol()),
                new NombreRol(jpa.getNombre()),
                new DescripcionRol(jpa.getDescripcion()),
                AmbitoRol.valueOf(jpa.getAmbito()),
                jpa.isEsSistema(),
                jpa.isActivo(),
                permisos);
    }

    public void copiar(Rol rol, RolJpa jpa) {
        jpa.setNombre(rol.nombre().valor());
        jpa.setDescripcion(rol.descripcion().valor());
        jpa.setAmbito(rol.ambito().name());
        jpa.setEsSistema(rol.esSistema());
        jpa.setActivo(rol.activo());
    }

    private PermisoId permisoId(RolPermisoJpa fila) {
        if (fila.getId() != null && fila.getId().getIdPermiso() != null) {
            return new PermisoId(fila.getId().getIdPermiso());
        }
        return new PermisoId(fila.getPermiso().getIdPermiso());
    }
}
