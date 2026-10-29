package com.alojate.alojamientos.infrastructure.adapters.output.persistence.jpa;

import com.alojate.alojamientos.infrastructure.adapters.output.persistence.entity.AlojamientoJpa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface AlojamientoJpaRepository extends JpaRepository<AlojamientoJpa, UUID> {

    List<AlojamientoJpa> findByIdAnfitrionOrderByFechaCreacionDesc(UUID idAnfitrion);

    List<AlojamientoJpa> findByEstadoPublicacionOrderByFechaCreacionDesc(String estadoPublicacion);

    List<AlojamientoJpa> findByEstadoPublicacionInOrderByFechaCreacionDesc(Collection<String> estados);

    List<AlojamientoJpa> findAllByOrderByFechaCreacionDesc();
}
