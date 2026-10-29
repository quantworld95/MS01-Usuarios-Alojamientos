package com.alojate.alojamientos.infrastructure.adapters.output.persistence.repository;

import com.alojate.alojamientos.domain.catalogo.aggregate.Servicio;
import com.alojate.alojamientos.domain.catalogo.aggregate.TipoEspacio;
import com.alojate.alojamientos.domain.catalogo.aggregate.TipoPropiedad;
import com.alojate.alojamientos.domain.catalogo.repository.CatalogoAlojamientoRepository;
import com.alojate.alojamientos.domain.catalogo.valueobject.ServicioId;
import com.alojate.alojamientos.domain.catalogo.valueobject.TipoEspacioId;
import com.alojate.alojamientos.domain.catalogo.valueobject.TipoPropiedadId;
import com.alojate.alojamientos.infrastructure.adapters.output.persistence.jpa.ServicioJpaRepository;
import com.alojate.alojamientos.infrastructure.adapters.output.persistence.jpa.TipoEspacioJpaRepository;
import com.alojate.alojamientos.infrastructure.adapters.output.persistence.jpa.TipoPropiedadJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public class PostgresCatalogoAlojamientoRepository implements CatalogoAlojamientoRepository {

    private final TipoPropiedadJpaRepository tiposPropiedad;
    private final TipoEspacioJpaRepository tiposEspacio;
    private final ServicioJpaRepository servicios;

    public PostgresCatalogoAlojamientoRepository(
            TipoPropiedadJpaRepository tiposPropiedad,
            TipoEspacioJpaRepository tiposEspacio,
            ServicioJpaRepository servicios) {
        this.tiposPropiedad = tiposPropiedad;
        this.tiposEspacio = tiposEspacio;
        this.servicios = servicios;
    }

    @Override
    public List<TipoPropiedad> listarTiposPropiedad() {
        return tiposPropiedad.findAllByOrderByNombreAsc().stream()
                .map(tipo -> TipoPropiedad.reconstituir(tipo.getIdTipoPropiedad(), tipo.getNombre()))
                .toList();
    }

    @Override
    public List<TipoEspacio> listarTiposEspacio() {
        return tiposEspacio.findAllByOrderByNombreAsc().stream()
                .map(tipo -> TipoEspacio.reconstituir(tipo.getIdTipoEspacio(), tipo.getNombre()))
                .toList();
    }

    @Override
    public List<Servicio> listarServicios() {
        return servicios.findAllByOrderByNombreAsc().stream()
                .map(servicio -> Servicio.reconstituir(
                        servicio.getIdServicio(), servicio.getNombre(), servicio.getCategoria()))
                .toList();
    }

    @Override
    public boolean existeTipoPropiedad(TipoPropiedadId id) {
        return tiposPropiedad.existsById(id.valor());
    }

    @Override
    public boolean existeTipoEspacio(TipoEspacioId id) {
        return tiposEspacio.existsById(id.valor());
    }

    @Override
    public boolean existenServicios(Set<ServicioId> ids) {
        if (ids.isEmpty()) {
            return true;
        }
        List<Integer> valores = ids.stream().map(ServicioId::valor).distinct().toList();
        return servicios.findAllById(valores).size() == valores.size();
    }
}
