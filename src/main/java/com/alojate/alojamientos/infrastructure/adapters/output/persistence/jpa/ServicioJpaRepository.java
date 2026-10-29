package com.alojate.alojamientos.infrastructure.adapters.output.persistence.jpa;

import com.alojate.alojamientos.infrastructure.adapters.output.persistence.entity.ServicioJpa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServicioJpaRepository extends JpaRepository<ServicioJpa, Integer> {

    List<ServicioJpa> findAllByOrderByNombreAsc();
}
