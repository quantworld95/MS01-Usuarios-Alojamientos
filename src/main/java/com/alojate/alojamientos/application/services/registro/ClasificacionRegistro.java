package com.alojate.alojamientos.application.services.registro;

import com.alojate.alojamientos.domain.alojamiento.politica.PoliticaCicloPublicacion;
import com.alojate.alojamientos.domain.catalogo.aggregate.TipoPropiedad;
import com.alojate.alojamientos.domain.catalogo.repository.CatalogoAlojamientoRepository;
import com.alojate.alojamientos.domain.catalogo.valueobject.TipoPropiedadId;
import com.alojate.alojamientos.domain.exception.ReglaNegocioException;

public final class ClasificacionRegistro {

    private ClasificacionRegistro() {
    }

    public static PoliticaCicloPublicacion politica(
            CatalogoAlojamientoRepository catalogos, TipoPropiedadId tipoId) {
        return PoliticaCicloPublicacion.desdeNombreTipo(nombreTipo(catalogos, tipoId));
    }

    public static boolean esOfertaUnica(CatalogoAlojamientoRepository catalogos, TipoPropiedadId tipoId) {
        return politica(catalogos, tipoId).esOfertaUnica();
    }

    public static boolean esEstablecimiento(CatalogoAlojamientoRepository catalogos, TipoPropiedadId tipoId) {
        return politica(catalogos, tipoId).esEstablecimiento();
    }

    private static String nombreTipo(CatalogoAlojamientoRepository catalogos, TipoPropiedadId tipoId) {
        return catalogos.listarTiposPropiedad().stream()
                .filter(tipo -> tipo.id().equals(tipoId))
                .map(TipoPropiedad::nombre)
                .findFirst()
                .orElseThrow(() -> new ReglaNegocioException("CATALOGO_INVALIDO", "El tipo de propiedad no existe"));
    }
}
