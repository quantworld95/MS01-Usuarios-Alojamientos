package com.alojate.identidad.infrastructure.adapters.output.persistence.jpa;

import com.alojate.identidad.infrastructure.adapters.output.persistence.entity.ModuloJpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ModuloJpaRepository extends JpaRepository<ModuloJpa, Integer> {

    @Query("select distinct m from ModuloJpa m left join fetch m.permisos order by m.orden")
    List<ModuloJpa> listarConPermisos();
}
