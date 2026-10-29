package com.alojate.identidad.infrastructure.adapters.output.persistence.jpa;

import com.alojate.identidad.infrastructure.adapters.output.persistence.entity.RolJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RolJpaRepository extends JpaRepository<RolJpa, Integer> {

    @Query("select distinct r from RolJpa r left join fetch r.permisos order by r.idRol")
    List<RolJpa> listarConPermisos();

    @Query("select r from RolJpa r left join fetch r.permisos where r.idRol = :id")
    Optional<RolJpa> buscarConPermisos(@Param("id") Integer id);

    Optional<RolJpa> findByNombreAndAmbitoAndEsSistemaTrueAndActivoTrue(String nombre, String ambito);

    boolean existsByNombreIgnoreCase(String nombre);

    @Query("select count(r) from RolJpa r where lower(r.nombre) = lower(:nombre) and r.idRol <> :id")
    long contarOtroNombre(@Param("nombre") String nombre, @Param("id") Integer id);
}
