package com.alojate.alojamientos.application.ports.output;

public interface AlmacenDeImagenes {

    ImagenAlmacenada publicar(ContenidoImagen contenido);

    void retirar(String identificadorAlmacen);
}
