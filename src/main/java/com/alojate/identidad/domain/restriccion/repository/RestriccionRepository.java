package com.alojate.identidad.domain.restriccion.repository;

import com.alojate.identidad.domain.modulo.valueobject.PermisoId;
import com.alojate.identidad.domain.restriccion.aggregate.RestriccionUsuario;
import com.alojate.identidad.domain.usuario.valueobject.UsuarioId;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface RestriccionRepository {

    RestriccionUsuario guardar(RestriccionUsuario restriccion);

    Optional<RestriccionUsuario> buscarPorId(UUID id);

    List<RestriccionUsuario> listarPorUsuario(UsuarioId usuarioId);

    boolean existeVigente(UsuarioId usuarioId, PermisoId permisoId, Instant ahora);

    Set<PermisoId> permisosVigentes(UsuarioId usuarioId, Instant ahora);
}
