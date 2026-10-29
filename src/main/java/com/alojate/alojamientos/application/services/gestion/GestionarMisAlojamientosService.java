package com.alojate.alojamientos.application.services.gestion;

import com.alojate.alojamientos.application.dto.AlojamientoRegistradoResponse;
import com.alojate.alojamientos.application.dto.AlojamientoRegistradoResponse.UnidadResponse;
import com.alojate.alojamientos.application.dto.AlojamientoResumenResponse;
import com.alojate.alojamientos.application.dto.EnviarARevisionCommand;
import com.alojate.alojamientos.application.dto.OperarAlojamientoCommand;
import com.alojate.alojamientos.application.dto.OperarUnidadCommand;
import com.alojate.alojamientos.application.ports.input.GestionarMisAlojamientos;
import com.alojate.alojamientos.application.ports.output.Reloj;
import com.alojate.alojamientos.application.ports.output.UnitOfWork;
import com.alojate.alojamientos.application.services.AlojamientosDelAnfitrion;
import com.alojate.alojamientos.application.services.registro.ClasificacionRegistro;
import com.alojate.alojamientos.application.services.registro.PresentadorAlojamiento;
import com.alojate.alojamientos.domain.alojamiento.aggregate.Alojamiento;
import com.alojate.alojamientos.domain.alojamiento.repository.AlojamientoRepository;
import com.alojate.alojamientos.domain.alojamiento.valueobject.AnfitrionId;
import com.alojate.alojamientos.domain.catalogo.repository.CatalogoAlojamientoRepository;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public final class GestionarMisAlojamientosService implements GestionarMisAlojamientos {

    private final AlojamientoRepository alojamientos;
    private final CatalogoAlojamientoRepository catalogos;
    private final AlojamientosDelAnfitrion propios;
    private final PresentadorAlojamiento presentador;
    private final Reloj reloj;
    private final UnitOfWork unitOfWork;

    public GestionarMisAlojamientosService(
            AlojamientoRepository alojamientos,
            CatalogoAlojamientoRepository catalogos,
            AlojamientosDelAnfitrion propios,
            PresentadorAlojamiento presentador,
            Reloj reloj,
            UnitOfWork unitOfWork) {
        this.alojamientos = alojamientos;
        this.catalogos = catalogos;
        this.propios = propios;
        this.presentador = presentador;
        this.reloj = reloj;
        this.unitOfWork = unitOfWork;
    }

    @Override
    public List<AlojamientoResumenResponse> listar(UUID anfitrionId) {
        AnfitrionId anfitrion = propios.exigir(anfitrionId);
        List<Alojamiento> lista = unitOfWork.ejecutar(() -> alojamientos.listarPorAnfitrion(anfitrion));
        return presentador.resumir(lista);
    }

    @Override
    public AlojamientoRegistradoResponse obtener(UUID anfitrionId, UUID alojamientoId) {
        AnfitrionId anfitrion = propios.exigir(anfitrionId);
        return presentador.detalle(unitOfWork.ejecutar(() -> propios.propio(anfitrion, alojamientoId)));
    }

    @Override
    public UnidadResponse obtenerUnidad(UUID anfitrionId, UUID alojamientoId, UUID unidadId) {
        AnfitrionId anfitrion = propios.exigir(anfitrionId);
        Alojamiento alojamiento = unitOfWork.ejecutar(() -> propios.propio(anfitrion, alojamientoId));
        return presentador.detalleUnidad(alojamiento, unidadId);
    }

    @Override
    public AlojamientoRegistradoResponse desactivar(OperarAlojamientoCommand comando) {
        return modificar(comando.anfitrionId(), comando.alojamientoId(), alojamiento -> {
            var politica = ClasificacionRegistro.politica(catalogos, alojamiento.tipoPropiedadId());
            alojamiento.desactivar(politica, reloj.ahora());
        });
    }

    @Override
    public AlojamientoRegistradoResponse activar(OperarAlojamientoCommand comando) {
        return modificar(comando.anfitrionId(), comando.alojamientoId(), alojamiento -> {
            var politica = ClasificacionRegistro.politica(catalogos, alojamiento.tipoPropiedadId());
            alojamiento.activar(politica, reloj.ahora());
        });
    }

    @Override
    public AlojamientoRegistradoResponse enviarARevision(EnviarARevisionCommand comando) {
        return modificar(comando.anfitrionId(), comando.alojamientoId(), alojamiento -> {
            var politica = ClasificacionRegistro.politica(catalogos, alojamiento.tipoPropiedadId());
            alojamiento.enviarARevision(politica, reloj.ahora());
        });
    }

    @Override
    public AlojamientoRegistradoResponse enviarUnidadARevision(OperarUnidadCommand comando) {
        return modificar(comando.anfitrionId(), comando.alojamientoId(), alojamiento ->
                alojamiento.enviarUnidadARevision(comando.unidadId(), reloj.ahora()));
    }

    @Override
    public AlojamientoRegistradoResponse desactivarUnidad(OperarUnidadCommand comando) {
        return modificar(comando.anfitrionId(), comando.alojamientoId(), alojamiento ->
                alojamiento.desactivarUnidad(comando.unidadId(), reloj.ahora()));
    }

    @Override
    public AlojamientoRegistradoResponse activarUnidad(OperarUnidadCommand comando) {
        return modificar(comando.anfitrionId(), comando.alojamientoId(), alojamiento ->
                alojamiento.activarUnidad(comando.unidadId(), reloj.ahora()));
    }

    private AlojamientoRegistradoResponse modificar(
            UUID anfitrionId, UUID alojamientoId, Consumer<Alojamiento> cambio) {
        AnfitrionId anfitrion = propios.exigir(anfitrionId);
        Alojamiento guardado = unitOfWork.ejecutar(() -> {
            Alojamiento alojamiento = propios.propio(anfitrion, alojamientoId);
            cambio.accept(alojamiento);
            return alojamientos.guardar(alojamiento);
        });
        return presentador.detalle(guardado);
    }
}
