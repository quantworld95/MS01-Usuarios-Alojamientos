package com.alojate.alojamientos.application.ports.input;

import com.alojate.alojamientos.application.dto.AlojamientoRegistradoResponse;
import com.alojate.alojamientos.application.dto.AlojamientoRegistradoResponse.UnidadResponse;
import com.alojate.alojamientos.application.dto.AlojamientoResumenResponse;
import com.alojate.alojamientos.application.dto.EnviarARevisionCommand;
import com.alojate.alojamientos.application.dto.OperarAlojamientoCommand;
import com.alojate.alojamientos.application.dto.OperarUnidadCommand;

import java.util.List;
import java.util.UUID;

public interface GestionarMisAlojamientos {

    List<AlojamientoResumenResponse> listar(UUID anfitrionId);

    AlojamientoRegistradoResponse obtener(UUID anfitrionId, UUID alojamientoId);

    UnidadResponse obtenerUnidad(UUID anfitrionId, UUID alojamientoId, UUID unidadId);

    AlojamientoRegistradoResponse desactivar(OperarAlojamientoCommand comando);

    AlojamientoRegistradoResponse activar(OperarAlojamientoCommand comando);

    /** Casa/depto → EN_REVISION (p. ej. reenvío tras rechazo). Hotel/hostal: no aplica. */
    AlojamientoRegistradoResponse enviarARevision(EnviarARevisionCommand comando);

    AlojamientoRegistradoResponse enviarUnidadARevision(OperarUnidadCommand comando);

    AlojamientoRegistradoResponse desactivarUnidad(OperarUnidadCommand comando);

    AlojamientoRegistradoResponse activarUnidad(OperarUnidadCommand comando);
}
