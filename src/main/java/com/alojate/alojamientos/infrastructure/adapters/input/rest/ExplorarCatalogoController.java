package com.alojate.alojamientos.infrastructure.adapters.input.rest;

import com.alojate.alojamientos.application.dto.AlojamientoPublicadoResponse;
import com.alojate.alojamientos.application.dto.OfertaPublicadaResponse;
import com.alojate.alojamientos.application.dto.OfertaPublicadaResponse.UnidadPublicadaResponse;
import com.alojate.alojamientos.application.ports.input.ExplorarCatalogo;
import com.alojate.config.SesionActual;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@Tag(
        name = "Catálogo público",
        description = """
                Solo ofertas y habitaciones en estado PUBLICADO (tras moderación).
                Requiere JWT (cualquier usuario autenticado).
                """)
public class ExplorarCatalogoController {

    private final ExplorarCatalogo explorar;

    public ExplorarCatalogoController(ExplorarCatalogo explorar) {
        this.explorar = explorar;
    }

    @GetMapping("/catalogo/alojamientos")
    @Operation(summary = "Listar alojamientos publicados")
    public List<AlojamientoPublicadoResponse> listar() {
        return explorar.listar(SesionActual.exigirUsuarioId());
    }

    @GetMapping("/catalogo/alojamientos/{id}")
    @Operation(summary = "Detalle de oferta publicada")
    public OfertaPublicadaResponse obtener(@PathVariable UUID id) {
        return explorar.obtener(SesionActual.exigirUsuarioId(), id);
    }

    @GetMapping("/catalogo/alojamientos/{id}/unidades/{unidadId}")
    @Operation(summary = "Detalle de habitación publicada")
    public UnidadPublicadaResponse obtenerUnidad(@PathVariable UUID id, @PathVariable UUID unidadId) {
        return explorar.obtenerUnidad(SesionActual.exigirUsuarioId(), id, unidadId);
    }
}
