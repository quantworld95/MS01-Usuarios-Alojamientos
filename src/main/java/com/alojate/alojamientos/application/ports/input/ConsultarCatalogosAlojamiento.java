package com.alojate.alojamientos.application.ports.input;

import com.alojate.alojamientos.application.dto.CatalogosAlojamientoResponse;

import java.util.UUID;

public interface ConsultarCatalogosAlojamiento {

    CatalogosAlojamientoResponse listar(UUID anfitrionId);
}
