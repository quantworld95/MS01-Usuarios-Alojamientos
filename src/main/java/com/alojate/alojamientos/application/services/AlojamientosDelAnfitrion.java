package com.alojate.alojamientos.application.services;

import com.alojate.alojamientos.application.ports.output.AccesoAnfitrion;
import com.alojate.alojamientos.domain.alojamiento.aggregate.Alojamiento;
import com.alojate.alojamientos.domain.alojamiento.repository.AlojamientoRepository;
import com.alojate.alojamientos.domain.alojamiento.valueobject.AlojamientoId;
import com.alojate.alojamientos.domain.alojamiento.valueobject.AnfitrionId;
import com.alojate.alojamientos.domain.exception.ReglaNegocioException;

import java.util.UUID;

public final class AlojamientosDelAnfitrion {

    private final AlojamientoRepository alojamientos;
    private final AccesoAnfitrion acceso;

    public AlojamientosDelAnfitrion(AlojamientoRepository alojamientos, AccesoAnfitrion acceso) {
        this.alojamientos = alojamientos;
        this.acceso = acceso;
    }

    public AnfitrionId exigir(UUID anfitrionId) {
        AnfitrionId anfitrion = new AnfitrionId(anfitrionId);
        acceso.exigirPuedeRegistrar(anfitrion.valor());
        return anfitrion;
    }

    public Alojamiento propio(AnfitrionId anfitrion, UUID alojamientoId) {
        return alojamientos.buscarPorId(new AlojamientoId(alojamientoId))
                .filter(alojamiento -> alojamiento.anfitrionId().equals(anfitrion))
                .orElseThrow(() -> new ReglaNegocioException(
                        "ALOJAMIENTO_NO_ENCONTRADO", "El alojamiento no existe"));
    }
}
