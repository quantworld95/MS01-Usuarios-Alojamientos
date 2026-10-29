package com.alojate.alojamientos.infrastructure.adapters.output.persistence.jpa;

import com.alojate.alojamientos.infrastructure.adapters.output.persistence.entity.TipoPropiedadJpa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TipoPropiedadJpaRepository extends JpaRepository<TipoPropiedadJpa, Integer> {

    List<TipoPropiedadJpa> findAllByOrderByNombreAsc();
}
