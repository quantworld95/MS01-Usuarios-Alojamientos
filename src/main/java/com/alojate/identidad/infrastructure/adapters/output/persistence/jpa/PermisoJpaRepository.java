package com.alojate.identidad.infrastructure.adapters.output.persistence.jpa;

import com.alojate.identidad.infrastructure.adapters.output.persistence.entity.PermisoJpa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface PermisoJpaRepository extends JpaRepository<PermisoJpa, Integer> {

    List<PermisoJpa> findByAsignableTrueAndIdPermisoIn(Collection<Integer> idPermisos);
}
