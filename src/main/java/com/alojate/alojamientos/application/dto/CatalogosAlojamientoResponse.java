package com.alojate.alojamientos.application.dto;

import java.util.List;

public record CatalogosAlojamientoResponse(
        List<CatalogoItemResponse> tiposPropiedad,
        List<CatalogoItemResponse> tiposEspacio,
        List<ServicioCatalogoResponse> servicios) {

    public record CatalogoItemResponse(int id, String nombre) {
    }

    public record ServicioCatalogoResponse(int id, String nombre, String categoria) {
    }
}
