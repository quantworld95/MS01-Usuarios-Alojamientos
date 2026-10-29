package com.alojate.identidad.domain.rol.repository;

import com.alojate.identidad.domain.rol.aggregate.Rol;
import com.alojate.identidad.domain.rol.valueobject.NombreRol;
import com.alojate.identidad.domain.rol.valueobject.RolId;

import java.util.List;
import java.util.Optional;

public interface RolRepository {

    Optional<Rol> buscarUsuarioPublico();

    List<Rol> listar();

    Optional<Rol> buscarPorId(RolId id);

    boolean existeOtroConNombre(NombreRol nombre, RolId excepto);

    Rol guardar(Rol rol);
}
