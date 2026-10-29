package com.alojate.identidad.support;

import com.alojate.identidad.domain.rol.aggregate.Rol;
import com.alojate.identidad.domain.rol.repository.RolRepository;
import com.alojate.identidad.domain.rol.valueobject.NombreRol;
import com.alojate.identidad.domain.rol.valueobject.RolId;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class RolesEnMemoria implements RolRepository {

    private final Map<Integer, Rol> porId = new HashMap<>();
    private int secuencia;

    public static RolesEnMemoria con(Rol... iniciales) {
        RolesEnMemoria memoria = new RolesEnMemoria();
        for (Rol rol : iniciales) {
            memoria.agregar(rol);
        }
        return memoria;
    }

    public void agregar(Rol rol) {
        porId.put(rol.id().valor(), rol);
        secuencia = Math.max(secuencia, rol.id().valor());
    }

    @Override
    public Optional<Rol> buscarUsuarioPublico() {
        return porId.values().stream().filter(Rol::esUsuarioPublicoAsignable).findFirst();
    }

    @Override
    public List<Rol> listar() {
        return List.copyOf(porId.values());
    }

    @Override
    public Optional<Rol> buscarPorId(RolId id) {
        return Optional.ofNullable(porId.get(id.valor()));
    }

    @Override
    public boolean existeOtroConNombre(NombreRol nombre, RolId excepto) {
        return porId.values().stream()
                .filter(rol -> excepto == null || !rol.id().equals(excepto))
                .anyMatch(rol -> rol.nombre().valor().equalsIgnoreCase(nombre.valor()));
    }

    @Override
    public Rol guardar(Rol rol) {
        Rol guardado = rol.id() == null ? rol.identificado(++secuencia) : rol;
        porId.put(guardado.id().valor(), guardado);
        return guardado;
    }
}
