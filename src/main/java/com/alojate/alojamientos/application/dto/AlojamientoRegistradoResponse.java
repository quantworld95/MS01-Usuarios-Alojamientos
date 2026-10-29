package com.alojate.alojamientos.application.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AlojamientoRegistradoResponse(
        UUID id,
        UUID anfitrionId,
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

    public record FotografiaResponse(UUID id, String identificadorAlmacen, String url, int orden) {
    }

    public record ServicioResponse(int id, String nombre, String categoria) {
    }

    public record UnidadResponse(
            UUID id,
            int tipoEspacioId,
            String tipoEspacio,
            String nombre,
            String descripcion,
            int capacidadHuespedes,
            int cantidadDormitorios,
            int cantidadCamas,
            BigDecimal cantidadBanos,
            int inventarioTotal,
            BigDecimal precioNoche,
            String estado,
            List<FotografiaResponse> fotografias,
            List<ServicioResponse> servicios,
            List<HistorialResponse> historial) {
    }

    public record HistorialResponse(String estado, String motivo, UUID actorId, Instant fecha) {
    }
}
