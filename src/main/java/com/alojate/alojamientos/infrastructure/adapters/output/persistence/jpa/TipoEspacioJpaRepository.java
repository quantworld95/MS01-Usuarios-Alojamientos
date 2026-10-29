package com.alojate.alojamientos.infrastructure.adapters.output.persistence.jpa;

import com.alojate.alojamientos.infrastructure.adapters.output.persistence.entity.TipoEspacioJpa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TipoEspacioJpaRepository extends JpaRepository<TipoEspacioJpa, Integer> {

    List<TipoEspacioJpa> findAllByOrderByNombreAsc();
}
