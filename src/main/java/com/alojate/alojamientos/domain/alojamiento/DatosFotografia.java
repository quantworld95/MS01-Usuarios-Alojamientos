package com.alojate.alojamientos.domain.alojamiento;

import java.util.UUID;

public record DatosFotografia(UUID id, String identificadorAlmacen, String url, int orden) {
}
