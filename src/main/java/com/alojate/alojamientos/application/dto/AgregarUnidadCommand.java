package com.alojate.alojamientos.application.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/** Alta de un solo tipo de habitación (no reemplaza la lista). */
public record AgregarUnidadCommand(
        UUID anfitrionId,
        UUID alojamientoId,
        Integer tipoEspacioId,
        String nombre,
        String descripcion,
        Integer capacidadHuespedes,
        Integer cantidadDormitorios,
        Integer cantidadCamas,
        BigDecimal cantidadBanos,
        Integer inventarioTotal,
        BigDecimal precioNoche,
        List<FotografiaCommand> fotografias,
        List<Integer> servicios) {

    public record FotografiaCommand(UUID id, String identificadorAlmacen, String url, Integer orden) {
    }
}
