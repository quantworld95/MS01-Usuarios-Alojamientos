package com.alojate.alojamientos.domain.alojamiento;

import com.alojate.alojamientos.domain.alojamiento.valueobject.DescripcionUnidad;
import com.alojate.alojamientos.domain.alojamiento.valueobject.EstadoUnidad;
import com.alojate.alojamientos.domain.alojamiento.valueobject.PrecioNoche;
import com.alojate.alojamientos.domain.catalogo.valueobject.ServicioId;
import com.alojate.alojamientos.domain.catalogo.valueobject.TipoEspacioId;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public record DatosUnidad(
        UUID id,
        TipoEspacioId tipoEspacioId,
        String nombre,
        DescripcionUnidad descripcion,
        int capacidadHuespedes,
        int cantidadDormitorios,
        int cantidadCamas,
        BigDecimal cantidadBanos,
        int inventarioTotal,
        PrecioNoche precioNoche,
        EstadoUnidad estado,
        List<DatosFotografia> fotografias,
        Set<ServicioId> servicios) {

    public DatosUnidad {
        fotografias = fotografias == null ? List.of() : List.copyOf(fotografias);
        servicios = servicios == null ? Set.of() : Set.copyOf(servicios);
    }
}
