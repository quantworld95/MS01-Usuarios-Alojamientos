package com.alojate.identidad.infrastructure.adapters.output.persistence.jpa;

import com.alojate.identidad.infrastructure.adapters.output.persistence.entity.UsuarioJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UsuarioJpaRepository extends JpaRepository<UsuarioJpa, UUID> {

    @Query("""
            select u from UsuarioJpa u
            left join fetch u.rol
            where u.email = :email and u.tipoCuenta = :tipo
            """)
    Optional<UsuarioJpa> findByEmailAndTipoCuenta(@Param("email") String email, @Param("tipo") String tipo);

    @Query("""
            select u from UsuarioJpa u
            left join fetch u.rol
            where u.idUsuario = :id
            """)
    Optional<UsuarioJpa> buscarPorId(@Param("id") UUID id);

    @Query("""
            select u from UsuarioJpa u
            left join fetch u.rol
            where u.tipoCuenta = 'CORPORATIVA'
            order by u.nombre asc, u.apellido asc
            """)
    List<UsuarioJpa> listarCorporativos();

    @Query("""
            select u from UsuarioJpa u
            left join fetch u.rol
            where u.tipoCuenta = 'PUBLICA'
            order by u.nombre asc, u.apellido asc
            """)
    List<UsuarioJpa> listarPublicos();

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdUsuarioNot(String email, UUID idUsuario);
}
