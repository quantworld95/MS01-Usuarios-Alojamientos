package com.alojate.alojamientos.domain.alojamiento.repository;

import com.alojate.alojamientos.domain.alojamiento.aggregate.Alojamiento;
import com.alojate.alojamientos.domain.alojamiento.valueobject.AlojamientoId;
import com.alojate.alojamientos.domain.alojamiento.valueobject.AnfitrionId;

import java.util.List;
import java.util.Optional;

public interface AlojamientoRepository {

    Alojamiento guardar(Alojamiento alojamiento);

    Optional<Alojamiento> buscarPorId(AlojamientoId id);

    List<Alojamiento> listarPorAnfitrion(AnfitrionId anfitrionId);

    List<Alojamiento> listarPublicados();

    List<Alojamiento> listarTodos();
}
