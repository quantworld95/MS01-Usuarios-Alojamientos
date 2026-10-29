package com.alojate.identidad.support;

import com.alojate.identidad.domain.modulo.valueobject.PermisoId;
import com.alojate.identidad.domain.restriccion.aggregate.RestriccionUsuario;
import com.alojate.identidad.domain.restriccion.repository.RestriccionRepository;
import com.alojate.identidad.domain.usuario.valueobject.UsuarioId;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public final class RestriccionesEnMemoria implements RestriccionRepository {

    private final Map<UUID, RestriccionUsuario> porId = new HashMap<>();

    @Override
    public RestriccionUsuario guardar(RestriccionUsuario restriccion) {
        porId.put(restriccion.id(), restriccion);
        return restriccion;
    }

    @Override
    public Optional<RestriccionUsuario> buscarPorId(UUID id) {
        return Optional.ofNullable(porId.get(id));
    }

    @Override
    public List<RestriccionUsuario> listarPorUsuario(UsuarioId usuarioId) {
        return porId.values().stream()
                .filter(restriccion -> restriccion.usuarioId().equals(usuarioId))
                .toList();
    }

    @Override
    public boolean existeVigente(UsuarioId usuarioId, PermisoId permisoId, Instant ahora) {
        return permisosVigentes(usuarioId, ahora).contains(permisoId);
    }

    @Override
    public Set<PermisoId> permisosVigentes(UsuarioId usuarioId, Instant ahora) {
        return porId.values().stream()
                .filter(restriccion -> restriccion.usuarioId().equals(usuarioId) && restriccion.vigente(ahora))
                .map(RestriccionUsuario::permisoId)
                .collect(Collectors.toSet());
    }
}
