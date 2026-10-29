package com.alojate.alojamientos.application.services.registro;

import com.alojate.alojamientos.application.dto.CatalogosAlojamientoResponse;
import com.alojate.alojamientos.application.dto.CatalogosAlojamientoResponse.CatalogoItemResponse;
import com.alojate.alojamientos.application.dto.CatalogosAlojamientoResponse.ServicioCatalogoResponse;
import com.alojate.alojamientos.application.ports.input.ConsultarCatalogosAlojamiento;
import com.alojate.alojamientos.application.ports.output.AccesoAnfitrion;
import com.alojate.alojamientos.domain.catalogo.repository.CatalogoAlojamientoRepository;

import java.util.UUID;

public final class ConsultarCatalogosAlojamientoService implements ConsultarCatalogosAlojamiento {

    private final CatalogoAlojamientoRepository catalogos;
    private final AccesoAnfitrion acceso;

    public ConsultarCatalogosAlojamientoService(
            CatalogoAlojamientoRepository catalogos, AccesoAnfitrion acceso) {
        this.catalogos = catalogos;
        this.acceso = acceso;
    }

    @Override
    public CatalogosAlojamientoResponse listar(UUID anfitrionId) {
        acceso.exigirPuedeRegistrar(anfitrionId);
        return new CatalogosAlojamientoResponse(
                catalogos.listarTiposPropiedad().stream()
                        .map(tipo -> new CatalogoItemResponse(tipo.id().valor(), tipo.nombre()))
                        .toList(),
                catalogos.listarTiposEspacio().stream()
                        .map(tipo -> new CatalogoItemResponse(tipo.id().valor(), tipo.nombre()))
                        .toList(),
                catalogos.listarServicios().stream()
                        .map(servicio -> new ServicioCatalogoResponse(
                                servicio.id().valor(), servicio.nombre(), servicio.categoria()))
                        .toList());
    }
}
