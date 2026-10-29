package com.alojate.alojamientos.application.dto;

import com.alojate.alojamientos.application.dto.AlojamientoRegistradoResponse.FotografiaResponse;
import com.alojate.alojamientos.application.dto.AlojamientoRegistradoResponse.HistorialResponse;
import com.alojate.alojamientos.application.dto.AlojamientoRegistradoResponse.ServicioResponse;
import com.alojate.alojamientos.application.dto.AlojamientoRegistradoResponse.UnidadResponse;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AlojamientoModeradoResponse(
        UUID id,
        UUID anfitrionId,
        String nombreAnfitrion,
        int tipoPropiedadId,
        String tipoPropiedad,
        String titulo,
        String descripcion,
        String politicas,
        String horarioEntrada,
        String horarioSalida,
        String direccion,
        String ciudad,
        String departamento,
        BigDecimal latitud,
        BigDecimal longitud,
        String estado,
        String motivo,
        Instant fechaCreacion,
        boolean establecimientoCompleto,
        boolean listoParaRevision,
        List<FotografiaResponse> fotografias,
        List<ServicioResponse> servicios,
        List<UnidadResponse> unidades,
        List<HistorialResponse> historial) {
}
