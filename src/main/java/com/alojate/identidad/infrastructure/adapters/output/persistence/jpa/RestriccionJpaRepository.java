package com.alojate.identidad.infrastructure.adapters.output.persistence.jpa;

import com.alojate.identidad.infrastructure.adapters.output.persistence.entity.RestriccionUsuarioJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RestriccionJpaRepository extends JpaRepository<RestriccionUsuarioJpa, UUID> {

    @Query("""
            select r from RestriccionUsuarioJpa r
            join fetch r.usuario
            join fetch r.administrador
            join fetch r.permiso
            where r.idRestriccion = :id
            """)
    Optional<RestriccionUsuarioJpa> buscarPorId(@Param("id") UUID id);

    @Query("""
            select r from RestriccionUsuarioJpa r
            join fetch r.usuario
            join fetch r.administrador
            join fetch r.permiso
            where r.usuario.idUsuario = :usuario
            order by r.fechaInicio desc
            """)
    List<RestriccionUsuarioJpa> listarPorUsuario(@Param("usuario") UUID usuario);

    @Query("""
            select r.permiso.idPermiso from RestriccionUsuarioJpa r
            where r.usuario.idUsuario = :usuario
              and r.activa = true
              and r.fechaInicio <= :ahora
              and (r.fechaFin is null or r.fechaFin > :ahora)
            """)
    List<Integer> permisosVigentes(@Param("usuario") UUID usuario, @Param("ahora") Instant ahora);
}
