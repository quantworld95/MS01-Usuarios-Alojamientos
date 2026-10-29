package com.alojate.alojamientos.domain.catalogo.repository;

import com.alojate.alojamientos.domain.catalogo.aggregate.Servicio;
import com.alojate.alojamientos.domain.catalogo.aggregate.TipoEspacio;
import com.alojate.alojamientos.domain.catalogo.aggregate.TipoPropiedad;
import com.alojate.alojamientos.domain.catalogo.valueobject.ServicioId;
import com.alojate.alojamientos.domain.catalogo.valueobject.TipoEspacioId;
import com.alojate.alojamientos.domain.catalogo.valueobject.TipoPropiedadId;

import java.util.List;
import java.util.Set;

public interface CatalogoAlojamientoRepository {

    List<TipoPropiedad> listarTiposPropiedad();

    List<TipoEspacio> listarTiposEspacio();

    List<Servicio> listarServicios();

    boolean existeTipoPropiedad(TipoPropiedadId id);

    boolean existeTipoEspacio(TipoEspacioId id);

    boolean existenServicios(Set<ServicioId> ids);
}
