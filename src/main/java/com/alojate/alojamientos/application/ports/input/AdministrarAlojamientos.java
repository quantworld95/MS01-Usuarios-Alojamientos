package com.alojate.alojamientos.application.ports.input;

import com.alojate.alojamientos.application.dto.AlojamientoModeradoResponse;
import com.alojate.alojamientos.application.dto.AlojamientoModeradoResumenResponse;
import com.alojate.alojamientos.application.dto.DecidirPublicacionCommand;
import com.alojate.alojamientos.application.dto.DecidirUnidadCommand;

import java.util.List;
import java.util.UUID;

public interface AdministrarAlojamientos {

    List<AlojamientoModeradoResumenResponse> listar(UUID administradorId);

    AlojamientoModeradoResponse obtener(UUID administradorId, UUID alojamientoId);

    AlojamientoModeradoResponse aceptar(UUID administradorId, UUID alojamientoId);

    AlojamientoModeradoResponse rechazar(DecidirPublicacionCommand comando);

    AlojamientoModeradoResponse suspender(DecidirPublicacionCommand comando);

    AlojamientoModeradoResponse levantarSuspension(UUID administradorId, UUID alojamientoId);

    AlojamientoModeradoResponse aceptarUnidad(UUID administradorId, UUID alojamientoId, UUID unidadId);

    AlojamientoModeradoResponse rechazarUnidad(DecidirUnidadCommand comando);

    AlojamientoModeradoResponse suspenderUnidad(DecidirUnidadCommand comando);

    AlojamientoModeradoResponse levantarSuspensionUnidad(
            UUID administradorId, UUID alojamientoId, UUID unidadId);
}
