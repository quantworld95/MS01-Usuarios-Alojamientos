package com.alojate.identidad.domain.modulo.repository;

import com.alojate.identidad.domain.modulo.aggregate.Modulo;
import com.alojate.identidad.domain.modulo.valueobject.PermisoId;

import java.util.List;
import java.util.Set;

public interface ModuloRepository {

    List<Modulo> listar();

    Set<PermisoId> buscarAsignables(Set<PermisoId> permisos);
}
