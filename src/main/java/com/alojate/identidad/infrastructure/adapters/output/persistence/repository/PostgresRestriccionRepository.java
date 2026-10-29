package com.alojate.identidad.infrastructure.adapters.output.persistence.repository;

import com.alojate.identidad.domain.modulo.valueobject.PermisoId;
import com.alojate.identidad.domain.restriccion.aggregate.RestriccionUsuario;
import com.alojate.identidad.domain.restriccion.repository.RestriccionRepository;
import com.alojate.identidad.domain.usuario.valueobject.UsuarioId;
import com.alojate.identidad.infrastructure.adapters.output.persistence.entity.RestriccionUsuarioJpa;
import com.alojate.identidad.infrastructure.adapters.output.persistence.jpa.PermisoJpaRepository;
import com.alojate.identidad.infrastructure.adapters.output.persistence.jpa.RestriccionJpaRepository;
import com.alojate.identidad.infrastructure.adapters.output.persistence.jpa.UsuarioJpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class PostgresRestriccionRepository implements RestriccionRepository {

    private final RestriccionJpaRepository restricciones;
    private final UsuarioJpaRepository usuarios;
    private final PermisoJpaRepository permisos;

    public PostgresRestriccionRepository(
            RestriccionJpaRepository restricciones,
            UsuarioJpaRepository usuarios,
            PermisoJpaRepository permisos) {
        this.restricciones = restricciones;
        this.usuarios = usuarios;
        this.permisos = permisos;
    }

    @Override
    public RestriccionUsuario guardar(RestriccionUsuario restriccion) {
        RestriccionUsuarioJpa jpa = restricciones.findById(restriccion.id()).orElseGet(RestriccionUsuarioJpa::new);
        jpa.setIdRestriccion(restriccion.id());
        jpa.setUsuario(usuarios.getReferenceById(restriccion.usuarioId().valor()));
        jpa.setAdministrador(usuarios.getReferenceById(restriccion.administradorId().valor()));
        jpa.setPermiso(permisos.getReferenceById(restriccion.permisoId().valor()));
        jpa.setMotivo(restriccion.motivo());
        jpa.setFechaInicio(restriccion.fechaInicio());
        jpa.setFechaFin(restriccion.fechaFin());
        jpa.setActiva(restriccion.activa());
        restricciones.saveAndFlush(jpa);
        return restriccion;
    }

    @Override
    public Optional<RestriccionUsuario> buscarPorId(UUID id) {
        return restricciones.buscarPorId(id).map(this::aDominio);
    }

    @Override
    public List<RestriccionUsuario> listarPorUsuario(UsuarioId usuarioId) {
        return restricciones.listarPorUsuario(usuarioId.valor()).stream().map(this::aDominio).toList();
    }

    @Override
    public boolean existeVigente(UsuarioId usuarioId, PermisoId permisoId, Instant ahora) {
        return permisosVigentes(usuarioId, ahora).contains(permisoId);
    }

    @Override
    public Set<PermisoId> permisosVigentes(UsuarioId usuarioId, Instant ahora) {
        return restricciones.permisosVigentes(usuarioId.valor(), ahora).stream()
                .map(PermisoId::new)
                .collect(Collectors.toSet());
    }

    private RestriccionUsuario aDominio(RestriccionUsuarioJpa jpa) {
        return RestriccionUsuario.reconstituir(
                jpa.getIdRestriccion(),
                new UsuarioId(jpa.getUsuario().getIdUsuario()),
                new PermisoId(jpa.getPermiso().getIdPermiso()),
                new UsuarioId(jpa.getAdministrador().getIdUsuario()),
                jpa.getMotivo(),
                jpa.getFechaInicio(),
                jpa.getFechaFin(),
                jpa.isActiva());
    }
}
