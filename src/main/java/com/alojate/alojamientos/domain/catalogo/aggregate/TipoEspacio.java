package com.alojate.alojamientos.domain.catalogo.aggregate;

import com.alojate.alojamientos.domain.catalogo.valueobject.TipoEspacioId;

public final class TipoEspacio {

    private final TipoEspacioId id;
    private final String nombre;

    private TipoEspacio(TipoEspacioId id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public static TipoEspacio reconstituir(int id, String nombre) {
        return new TipoEspacio(new TipoEspacioId(id), nombre);
    }

    public TipoEspacioId id() {
        return id;
    }

    public String nombre() {
        return nombre;
    }
}
