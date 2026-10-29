package com.alojate.alojamientos.application.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record OfertaPublicadaResponse(
        UUID id,
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
        List<FotografiaPublicaResponse> fotografias,
        List<ServicioPublicadoResponse> servicios,
        List<UnidadPublicadaResponse> unidades) {

    public record FotografiaPublicaResponse(UUID id, String url, int orden) {
    }

    public record ServicioPublicadoResponse(int id, String nombre, String categoria) {
    }

    public record UnidadPublicadaResponse(
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
            List<FotografiaPublicaResponse> fotografias,
            List<ServicioPublicadoResponse> servicios) {
    }
}
