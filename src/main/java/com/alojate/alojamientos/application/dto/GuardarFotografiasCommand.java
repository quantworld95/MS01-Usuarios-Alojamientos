package com.alojate.alojamientos.application.dto;

import java.util.List;
import java.util.UUID;

public record GuardarFotografiasCommand(
        UUID anfitrionId, UUID alojamientoId, List<FotografiaCommand> fotografias) {

    public record FotografiaCommand(UUID id, String identificadorAlmacen, String url, Integer orden) {
    }
}
