package com.alojate.alojamientos.infrastructure.adapters.input.rest;

import com.alojate.alojamientos.application.dto.AlojamientoModeradoResponse;
import com.alojate.alojamientos.application.dto.AlojamientoModeradoResumenResponse;
import com.alojate.alojamientos.application.dto.DecidirPublicacionCommand;
import com.alojate.alojamientos.application.dto.DecidirUnidadCommand;
import com.alojate.alojamientos.application.ports.input.AdministrarAlojamientos;
import com.alojate.config.SesionActual;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@Tag(
        name = "Moderación de alojamientos",
        description = """
                Cierra el ciclo tras el registro. Requiere JWT corporativo con permiso de moderación.

                **Casa/Departamento** en EN_REVISION → aceptar (PUBLICADO) o rechazar (motivo).
                **Habitaciones** de hotel/hostal en EN_REVISION → aceptar/rechazar por unidad.
                Publicados: suspender / levantar-suspension (alojamiento o unidad).
                """)
public class AdministrarAlojamientosController {

    private final AdministrarAlojamientos administrar;

    public AdministrarAlojamientosController(AdministrarAlojamientos administrar) {
        this.administrar = administrar;
    }

    @GetMapping("/admin/alojamientos")
    @Operation(summary = "Listar alojamientos para moderación")
    public List<AlojamientoModeradoResumenResponse> listar() {
        return administrar.listar(SesionActual.exigirUsuarioId());
    }

    @GetMapping("/admin/alojamientos/{id}")
    @Operation(summary = "Detalle para moderación")
    public AlojamientoModeradoResponse obtener(@PathVariable UUID id) {
        return administrar.obtener(SesionActual.exigirUsuarioId(), id);
    }

    @PostMapping("/admin/alojamientos/{id}/aceptar")
    @Operation(
            summary = "Aceptar oferta (Casa/Depto)",
            description = "EN_REVISION → PUBLICADO. Visible en GET /catalogo/alojamientos.")
    public AlojamientoModeradoResponse aceptar(@PathVariable UUID id) {
        return administrar.aceptar(SesionActual.exigirUsuarioId(), id);
    }

    @PostMapping("/admin/alojamientos/{id}/rechazar")
    @Operation(
            summary = "Rechazar oferta (Casa/Depto)",
            description = "Body: { \"motivo\": \"...\" }. El anfitrión puede corregir y reenviar.")
    public AlojamientoModeradoResponse rechazar(@PathVariable UUID id, @RequestBody DecisionRequest solicitud) {
        return administrar.rechazar(
                new DecidirPublicacionCommand(SesionActual.exigirUsuarioId(), id, solicitud.motivo()));
    }

    @PostMapping("/admin/alojamientos/{id}/suspender")
    @Operation(summary = "Suspender alojamiento publicado", description = "Body: { \"motivo\": \"...\" }.")
    public AlojamientoModeradoResponse suspender(@PathVariable UUID id, @RequestBody DecisionRequest solicitud) {
        return administrar.suspender(
                new DecidirPublicacionCommand(SesionActual.exigirUsuarioId(), id, solicitud.motivo()));
    }

    @PostMapping("/admin/alojamientos/{id}/levantar-suspension")
    @Operation(summary = "Levantar suspensión del alojamiento")
    public AlojamientoModeradoResponse levantarSuspension(@PathVariable UUID id) {
        return administrar.levantarSuspension(SesionActual.exigirUsuarioId(), id);
    }

    @PostMapping("/admin/alojamientos/{id}/unidades/{unidadId}/aceptar")
    @Operation(
            summary = "Aceptar habitación (Hotel/Hostal)",
            description = "Unidad EN_REVISION → PUBLICADO.")
    public AlojamientoModeradoResponse aceptarUnidad(@PathVariable UUID id, @PathVariable UUID unidadId) {
        return administrar.aceptarUnidad(SesionActual.exigirUsuarioId(), id, unidadId);
    }

    @PostMapping("/admin/alojamientos/{id}/unidades/{unidadId}/rechazar")
    @Operation(summary = "Rechazar habitación", description = "Body: { \"motivo\": \"...\" }.")
    public AlojamientoModeradoResponse rechazarUnidad(
            @PathVariable UUID id, @PathVariable UUID unidadId, @RequestBody DecisionRequest solicitud) {
        return administrar.rechazarUnidad(
                new DecidirUnidadCommand(SesionActual.exigirUsuarioId(), id, unidadId, solicitud.motivo()));
    }

    @PostMapping("/admin/alojamientos/{id}/unidades/{unidadId}/suspender")
    @Operation(summary = "Suspender habitación publicada", description = "Body: { \"motivo\": \"...\" }.")
    public AlojamientoModeradoResponse suspenderUnidad(
            @PathVariable UUID id, @PathVariable UUID unidadId, @RequestBody DecisionRequest solicitud) {
        return administrar.suspenderUnidad(
                new DecidirUnidadCommand(SesionActual.exigirUsuarioId(), id, unidadId, solicitud.motivo()));
    }

    @PostMapping("/admin/alojamientos/{id}/unidades/{unidadId}/levantar-suspension")
    @Operation(summary = "Levantar suspensión de habitación")
    public AlojamientoModeradoResponse levantarSuspensionUnidad(
            @PathVariable UUID id, @PathVariable UUID unidadId) {
        return administrar.levantarSuspensionUnidad(SesionActual.exigirUsuarioId(), id, unidadId);
    }

    public record DecisionRequest(String motivo) {
    }
}
