package com.alojate.alojamientos.application.services.administracion;

import com.alojate.alojamientos.application.dto.AlojamientoModeradoResponse;
import com.alojate.alojamientos.application.dto.AlojamientoModeradoResumenResponse;
import com.alojate.alojamientos.application.dto.DecidirPublicacionCommand;
import com.alojate.alojamientos.application.dto.DecidirUnidadCommand;
import com.alojate.alojamientos.application.ports.input.AdministrarAlojamientos;
import com.alojate.alojamientos.application.ports.output.AccesoAdministrador;
import com.alojate.alojamientos.application.ports.output.Reloj;
import com.alojate.alojamientos.application.ports.output.UnitOfWork;
import com.alojate.alojamientos.application.services.registro.ClasificacionRegistro;
import com.alojate.alojamientos.domain.alojamiento.aggregate.Alojamiento;
import com.alojate.alojamientos.domain.alojamiento.repository.AlojamientoRepository;
import com.alojate.alojamientos.domain.alojamiento.valueobject.AlojamientoId;
import com.alojate.alojamientos.domain.catalogo.repository.CatalogoAlojamientoRepository;
import com.alojate.alojamientos.domain.exception.ReglaNegocioException;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public final class AdministrarAlojamientosService implements AdministrarAlojamientos {

    private final AlojamientoRepository alojamientos;
    private final CatalogoAlojamientoRepository catalogos;
    private final AccesoAdministrador acceso;
    private final PresentadorModeracion presentador;
    private final Reloj reloj;
    private final UnitOfWork unitOfWork;

    public AdministrarAlojamientosService(
            AlojamientoRepository alojamientos,
            CatalogoAlojamientoRepository catalogos,
            AccesoAdministrador acceso,
            PresentadorModeracion presentador,
            Reloj reloj,
            UnitOfWork unitOfWork) {
        this.alojamientos = alojamientos;
        this.catalogos = catalogos;
        this.acceso = acceso;
        this.presentador = presentador;
        this.reloj = reloj;
        this.unitOfWork = unitOfWork;
    }

    @Override
    public List<AlojamientoModeradoResumenResponse> listar(UUID administradorId) {
        acceso.exigirPuedeModerar(administradorId);
        List<Alojamiento> todos = unitOfWork.ejecutar(alojamientos::listarTodos);
        return presentador.resumir(todos);
    }

    @Override
    public AlojamientoModeradoResponse obtener(UUID administradorId, UUID alojamientoId) {
        acceso.exigirPuedeModerar(administradorId);
        return presentador.detalle(unitOfWork.ejecutar(() -> buscar(alojamientoId)));
    }

    @Override
    public AlojamientoModeradoResponse aceptar(UUID administradorId, UUID alojamientoId) {
        return modificar(administradorId, alojamientoId, alojamiento ->
                alojamiento.aceptar(administradorId, reloj.ahora()));
    }

    @Override
    public AlojamientoModeradoResponse rechazar(DecidirPublicacionCommand comando) {
        return modificar(comando.administradorId(), comando.alojamientoId(), alojamiento ->
                alojamiento.rechazar(comando.administradorId(), comando.motivo(), reloj.ahora()));
    }

    @Override
    public AlojamientoModeradoResponse suspender(DecidirPublicacionCommand comando) {
        return modificar(comando.administradorId(), comando.alojamientoId(), alojamiento -> {
            var politica = ClasificacionRegistro.politica(catalogos, alojamiento.tipoPropiedadId());
            alojamiento.suspender(politica, comando.administradorId(), comando.motivo(), reloj.ahora());
        });
    }

    @Override
    public AlojamientoModeradoResponse levantarSuspension(UUID administradorId, UUID alojamientoId) {
        return modificar(administradorId, alojamientoId, alojamiento -> {
            var politica = ClasificacionRegistro.politica(catalogos, alojamiento.tipoPropiedadId());
            alojamiento.levantarSuspension(politica, administradorId, reloj.ahora());
        });
    }

    @Override
    public AlojamientoModeradoResponse aceptarUnidad(
            UUID administradorId, UUID alojamientoId, UUID unidadId) {
        return modificar(administradorId, alojamientoId, alojamiento ->
                alojamiento.aceptarUnidad(unidadId, administradorId, reloj.ahora()));
    }

    @Override
    public AlojamientoModeradoResponse rechazarUnidad(DecidirUnidadCommand comando) {
        return modificar(comando.administradorId(), comando.alojamientoId(), alojamiento ->
                alojamiento.rechazarUnidad(
                        comando.unidadId(), comando.administradorId(), comando.motivo(), reloj.ahora()));
    }

    @Override
    public AlojamientoModeradoResponse suspenderUnidad(DecidirUnidadCommand comando) {
        return modificar(comando.administradorId(), comando.alojamientoId(), alojamiento ->
                alojamiento.suspenderUnidad(
                        comando.unidadId(), comando.administradorId(), comando.motivo(), reloj.ahora()));
    }

    @Override
    public AlojamientoModeradoResponse levantarSuspensionUnidad(
            UUID administradorId, UUID alojamientoId, UUID unidadId) {
        return modificar(administradorId, alojamientoId, alojamiento ->
                alojamiento.levantarSuspensionUnidad(unidadId, administradorId, reloj.ahora()));
    }

    private AlojamientoModeradoResponse modificar(
            UUID administradorId, UUID alojamientoId, Consumer<Alojamiento> cambio) {
        acceso.exigirPuedeModerar(administradorId);
        Alojamiento guardado = unitOfWork.ejecutar(() -> {
            Alojamiento alojamiento = buscar(alojamientoId);
            cambio.accept(alojamiento);
            return alojamientos.guardar(alojamiento);
        });
        return presentador.detalle(guardado);
    }

    private Alojamiento buscar(UUID alojamientoId) {
        return alojamientos.buscarPorId(new AlojamientoId(alojamientoId))
                .orElseThrow(() -> new ReglaNegocioException(
                        "ALOJAMIENTO_NO_ENCONTRADO", "El alojamiento no existe"));
    }
}
