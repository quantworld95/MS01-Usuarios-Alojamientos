package com.alojate.alojamientos.application.ports.input;

import com.alojate.alojamientos.application.dto.AlojamientoPublicadoResponse;
import com.alojate.alojamientos.application.dto.OfertaPublicadaResponse;
import com.alojate.alojamientos.application.dto.OfertaPublicadaResponse.UnidadPublicadaResponse;

import java.util.List;
import java.util.UUID;

public interface ExplorarCatalogo {

    List<AlojamientoPublicadoResponse> listar(UUID huespedId);

    OfertaPublicadaResponse obtener(UUID huespedId, UUID alojamientoId);

    UnidadPublicadaResponse obtenerUnidad(UUID huespedId, UUID alojamientoId, UUID unidadId);
}
