package com.alojate.alojamientos.application.services.catalogo;

import com.alojate.alojamientos.application.dto.AlojamientoPublicadoResponse;
import com.alojate.alojamientos.application.dto.OfertaPublicadaResponse;
import com.alojate.alojamientos.application.dto.OfertaPublicadaResponse.UnidadPublicadaResponse;
import com.alojate.alojamientos.application.ports.input.ExplorarCatalogo;
import com.alojate.alojamientos.application.ports.output.AccesoHuesped;
import com.alojate.alojamientos.application.ports.output.UnitOfWork;
import com.alojate.alojamientos.domain.alojamiento.aggregate.Alojamiento;
import com.alojate.alojamientos.domain.alojamiento.repository.AlojamientoRepository;
import com.alojate.alojamientos.domain.alojamiento.valueobject.AlojamientoId;
import com.alojate.alojamientos.domain.exception.ReglaNegocioException;

import java.util.List;
import java.util.UUID;

public final class ExplorarCatalogoService implements ExplorarCatalogo {

    private final AlojamientoRepository alojamientos;
    private final AccesoHuesped acceso;
    private final PresentadorCatalogo presentador;
    private final UnitOfWork unitOfWork;

    public ExplorarCatalogoService(
            AlojamientoRepository alojamientos,
            AccesoHuesped acceso,
            PresentadorCatalogo presentador,
            UnitOfWork unitOfWork) {
        this.alojamientos = alojamientos;
        this.acceso = acceso;
        this.presentador = presentador;
        this.unitOfWork = unitOfWork;
    }

    @Override
    public List<AlojamientoPublicadoResponse> listar(UUID huespedId) {
        acceso.exigirPuedeBuscar(huespedId);
        List<Alojamiento> publicados = unitOfWork.ejecutar(alojamientos::listarPublicados);
        return presentador.resumir(publicados);
    }

    @Override
    public OfertaPublicadaResponse obtener(UUID huespedId, UUID alojamientoId) {
        acceso.exigirPuedeBuscar(huespedId);
        return presentador.detalle(publicado(alojamientoId));
    }

    @Override
    public UnidadPublicadaResponse obtenerUnidad(UUID huespedId, UUID alojamientoId, UUID unidadId) {
        acceso.exigirPuedeBuscar(huespedId);
        return presentador.detalleUnidad(publicado(alojamientoId), unidadId);
    }

    private Alojamiento publicado(UUID alojamientoId) {
        return unitOfWork.ejecutar(() -> alojamientos.buscarPorId(new AlojamientoId(alojamientoId))
                .filter(Alojamiento::visibleEnCatalogo)
                .orElseThrow(() -> new ReglaNegocioException(
                        "ALOJAMIENTO_NO_ENCONTRADO", "El alojamiento no existe")));
    }
}
