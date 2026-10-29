package com.alojate.alojamientos.domain.catalogo.aggregate;

import com.alojate.alojamientos.domain.catalogo.valueobject.TipoPropiedadId;

public final class TipoPropiedad {

    private final TipoPropiedadId id;
    private final String nombre;

    private TipoPropiedad(TipoPropiedadId id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public static TipoPropiedad reconstituir(int id, String nombre) {
        return new TipoPropiedad(new TipoPropiedadId(id), nombre);
    }

    public TipoPropiedadId id() {
        return id;
    }

    public String nombre() {
        return nombre;
    }
}
