package com.alojate.alojamientos.domain.catalogo.aggregate;

import com.alojate.alojamientos.domain.catalogo.valueobject.ServicioId;

public final class Servicio {

    private final ServicioId id;
    private final String nombre;
    private final String categoria;

    private Servicio(ServicioId id, String nombre, String categoria) {
        this.id = id;
        this.nombre = nombre;
        this.categoria = categoria;
    }

    public static Servicio reconstituir(int id, String nombre, String categoria) {
        return new Servicio(new ServicioId(id), nombre, categoria);
    }

    public ServicioId id() {
        return id;
    }

    public String nombre() {
        return nombre;
    }

    public String categoria() {
        return categoria;
    }
}
