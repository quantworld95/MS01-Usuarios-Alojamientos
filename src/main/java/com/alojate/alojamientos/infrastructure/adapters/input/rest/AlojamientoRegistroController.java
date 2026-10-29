package com.alojate.alojamientos.infrastructure.adapters.input.rest;

import com.alojate.alojamientos.application.dto.AlojamientoRegistradoResponse;
import com.alojate.alojamientos.application.dto.AlojamientoRegistradoResponse.UnidadResponse;
import com.alojate.alojamientos.application.dto.AlojamientoResumenResponse;
import com.alojate.alojamientos.application.dto.CatalogosAlojamientoResponse;
import com.alojate.alojamientos.application.dto.EnviarARevisionCommand;
import com.alojate.alojamientos.application.dto.FinalizarRegistroCommand;
import com.alojate.alojamientos.application.dto.GuardarFotografiasCommand;
import com.alojate.alojamientos.application.dto.ImagenPublicadaResponse;
import com.alojate.alojamientos.application.dto.GuardarOfertaUnicaCommand;
import com.alojate.alojamientos.application.dto.GuardarPoliticasCommand;
import com.alojate.alojamientos.application.dto.GuardarPresentacionCommand;
import com.alojate.alojamientos.application.dto.GuardarServiciosCommand;
import com.alojate.alojamientos.application.dto.GuardarUbicacionCommand;
import com.alojate.alojamientos.application.dto.AgregarUnidadCommand;
import com.alojate.alojamientos.application.dto.OperarAlojamientoCommand;
import com.alojate.alojamientos.application.dto.OperarUnidadCommand;
import com.alojate.alojamientos.application.dto.PublicarFotografiaCommand;
import com.alojate.alojamientos.application.dto.RegistrarAlojamientoCommand;
import com.alojate.alojamientos.application.ports.input.ConsultarCatalogosAlojamiento;
import com.alojate.alojamientos.application.ports.input.GestionarMisAlojamientos;
import com.alojate.alojamientos.application.ports.input.RegistrarAlojamiento;
import com.alojate.alojamientos.domain.exception.ReglaNegocioException;
import com.alojate.config.SesionActual;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@Tag(
        name = "Registro de alojamiento",
        description = """
                Wizard por pasos (requiere JWT). El registro no es un solo POST.

                **Casa / Departamento:** POST /alojamientos → presentacion → ubicacion → servicios \
                → oferta-unica → fotografias → finalizar-registro (queda EN_REVISION).

                **Hotel / Hostal:** POST /alojamientos → presentacion → ubicacion → servicios \
                → politicas → fotografias → finalizar-registro (queda ACTIVO) → POST /unidades por habitación.
                """)
public class AlojamientoRegistroController {

    private final RegistrarAlojamiento registrar;
    private final GestionarMisAlojamientos gestionar;
    private final ConsultarCatalogosAlojamiento consultarCatalogos;

    public AlojamientoRegistroController(
            RegistrarAlojamiento registrar,
            GestionarMisAlojamientos gestionar,
            ConsultarCatalogosAlojamiento consultarCatalogos) {
        this.registrar = registrar;
        this.gestionar = gestionar;
        this.consultarCatalogos = consultarCatalogos;
    }

    @GetMapping("/catalogos-alojamiento")
    @Operation(summary = "0. Catálogos", description = "Tipos de propiedad, tipos de espacio y servicios. Usar antes de crear el borrador.")
    public CatalogosAlojamientoResponse catalogos() {
        return consultarCatalogos.listar(SesionActual.exigirUsuarioId());
    }

    @PostMapping("/alojamientos")
    @Operation(
            summary = "1. Crear borrador",
            description = "Crea el alojamiento en BORRADOR con solo el tipo. Guarda el id de la respuesta para los siguientes pasos.")
    public ResponseEntity<AlojamientoRegistradoResponse> registrar(@RequestBody RegistroRequest solicitud) {
        return ResponseEntity.status(HttpStatus.CREATED).body(registrar.registrar(
                new RegistrarAlojamientoCommand(SesionActual.exigirUsuarioId(), solicitud.tipoPropiedadId())));
    }

    @PutMapping("/alojamientos/{id}/ubicacion")
    @Operation(summary = "3. Guardar ubicación", description = "Dirección, ciudad, departamento y coordenadas.")
    public AlojamientoRegistradoResponse ubicacion(
            @PathVariable UUID id, @RequestBody UbicacionRequest solicitud) {
        return registrar.guardarUbicacion(new GuardarUbicacionCommand(
                SesionActual.exigirUsuarioId(),
                id,
                solicitud.direccion(),
                solicitud.ciudad(),
                solicitud.departamento(),
                solicitud.latitud(),
                solicitud.longitud()));
    }

    @PutMapping("/alojamientos/{id}/presentacion")
    @Operation(summary = "2. Guardar presentación", description = "Título y descripción. Políticas/horarios opcionales aquí (en hotel suelen ir en el endpoint de políticas).")
    public AlojamientoRegistradoResponse presentacion(
            @PathVariable UUID id, @RequestBody PresentacionRequest solicitud) {
        return registrar.guardarPresentacion(new GuardarPresentacionCommand(
                SesionActual.exigirUsuarioId(),
                id,
                solicitud.titulo(),
                solicitud.descripcion(),
                solicitud.politicas(),
                solicitud.horarioEntrada(),
                solicitud.horarioSalida()));
    }

    @PutMapping("/alojamientos/{id}/politicas")
    @Operation(
            summary = "5b. Guardar políticas (Hotel/Hostal)",
            description = "Obligatorio en establecimiento: políticas + horarioEntrada + horarioSalida.")
    public AlojamientoRegistradoResponse politicas(
            @PathVariable UUID id, @RequestBody PoliticasRequest solicitud) {
        return registrar.guardarPoliticas(new GuardarPoliticasCommand(
                SesionActual.exigirUsuarioId(),
                id,
                solicitud.politicas(),
                solicitud.horarioEntrada(),
                solicitud.horarioSalida()));
    }

    @PutMapping("/alojamientos/{id}/servicios")
    @Operation(summary = "4. Guardar servicios", description = "Lista de ids de servicio del catálogo.")
    public AlojamientoRegistradoResponse servicios(
            @PathVariable UUID id, @RequestBody ServiciosRequest solicitud) {
        return registrar.guardarServicios(new GuardarServiciosCommand(
                SesionActual.exigirUsuarioId(), id, solicitud.servicios()));
    }

    @PutMapping("/alojamientos/{id}/fotografias")
    @Operation(summary = "6. Reemplazar fotografías (JSON)", description = "Sustituye la lista de fotos ya publicadas (identificadorAlmacen + url + orden).")
    public AlojamientoRegistradoResponse fotografias(
            @PathVariable UUID id, @RequestBody FotografiasRequest solicitud) {
        List<GuardarFotografiasCommand.FotografiaCommand> fotos = solicitud.fotografias() == null
                ? List.of()
                : solicitud.fotografias().stream()
                        .map(foto -> new GuardarFotografiasCommand.FotografiaCommand(
                                foto.id(), foto.identificadorAlmacen(), foto.url(), foto.orden()))
                        .toList();
        return registrar.guardarFotografias(
                new GuardarFotografiasCommand(SesionActual.exigirUsuarioId(), id, fotos));
    }

    @PostMapping(value = "/alojamientos/{id}/fotografias/archivo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "6. Subir fotografía (multipart)", description = "Campo form: archivo. Repetir hasta tener al menos una foto antes de finalizar.")
    public AlojamientoRegistradoResponse publicarFotografia(
            @PathVariable UUID id, @RequestParam("archivo") MultipartFile archivo) {
        try {
            return registrar.publicarFotografia(new PublicarFotografiaCommand(
                    SesionActual.exigirUsuarioId(), id, archivo.getBytes(), archivo.getContentType()));
        } catch (IOException error) {
            throw new ReglaNegocioException("DATO_INVALIDO", "La imagen es obligatoria");
        }
    }

    @PostMapping(value = "/alojamientos/{id}/imagenes/archivo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Subir imagen temporal", description = "Publica en almacén y devuelve identificador/url sin asociarla aún al alojamiento.")
    public ImagenPublicadaResponse publicarImagen(
            @PathVariable UUID id, @RequestParam("archivo") MultipartFile archivo) {
        try {
            return registrar.publicarImagen(new PublicarFotografiaCommand(
                    SesionActual.exigirUsuarioId(), id, archivo.getBytes(), archivo.getContentType()));
        } catch (IOException error) {
            throw new ReglaNegocioException("DATO_INVALIDO", "La imagen es obligatoria");
        }
    }

    @PutMapping("/alojamientos/{id}/oferta-unica")
    @Operation(
            summary = "5a. Oferta única (Casa/Departamento)",
            description = "Capacidad, dormitorios, camas, baños, precio y políticas/horarios. Solo para tipos de oferta única.")
    public AlojamientoRegistradoResponse ofertaUnica(
            @PathVariable UUID id, @RequestBody OfertaUnicaRequest solicitud) {
        return registrar.guardarOfertaUnica(new GuardarOfertaUnicaCommand(
                SesionActual.exigirUsuarioId(),
                id,
                solicitud.capacidadHuespedes(),
                solicitud.cantidadDormitorios(),
                solicitud.cantidadCamas(),
                solicitud.cantidadBanos(),
                solicitud.precioNoche(),
                solicitud.politicas(),
                solicitud.horarioEntrada(),
                solicitud.horarioSalida()));
    }

    @PostMapping("/alojamientos/{id}/unidades")
    @Operation(
            summary = "8. Agregar habitación (Hotel/Hostal)",
            description = "Tras finalizar el establecimiento (ACTIVO). Crea la unidad y la envía a EN_REVISION.")
    public AlojamientoRegistradoResponse agregarUnidad(
            @PathVariable UUID id, @RequestBody AgregarUnidadRequest solicitud) {
        return registrar.agregarUnidad(new AgregarUnidadCommand(
                SesionActual.exigirUsuarioId(),
                id,
                solicitud.tipoEspacioId(),
                solicitud.nombre(),
                solicitud.descripcion(),
                solicitud.capacidadHuespedes(),
                solicitud.cantidadDormitorios(),
                solicitud.cantidadCamas(),
                solicitud.cantidadBanos(),
                solicitud.inventarioTotal(),
                solicitud.precioNoche(),
                solicitud.fotografias() == null
                        ? List.of()
                        : solicitud.fotografias().stream()
                                .map(foto -> new AgregarUnidadCommand.FotografiaCommand(
                                        foto.id(),
                                        foto.identificadorAlmacen(),
                                        foto.url(),
                                        foto.orden()))
                                .toList(),
                solicitud.servicios()));
    }

    @PostMapping("/alojamientos/{id}/finalizar-registro")
    @Operation(
            summary = "7. Finalizar registro",
            description = "Casa/Departamento → EN_REVISION. Hotel/Hostal → ACTIVO. Requiere datos mínimos completos y al menos una foto.")
    public AlojamientoRegistradoResponse finalizarRegistro(@PathVariable UUID id) {
        return registrar.finalizarRegistro(new FinalizarRegistroCommand(SesionActual.exigirUsuarioId(), id));
    }

    @PostMapping("/alojamientos/{id}/enviar-revision")
    @Operation(summary = "Reenviar a revisión", description = "Para ofertas ya finalizadas que vuelven a edición (p. ej. tras rechazo).")
    public AlojamientoRegistradoResponse enviarRevision(@PathVariable UUID id) {
        return gestionar.enviarARevision(new EnviarARevisionCommand(SesionActual.exigirUsuarioId(), id));
    }

    @PostMapping("/alojamientos/{id}/desactivar")
    @Operation(summary = "Desactivar alojamiento")
    public AlojamientoRegistradoResponse desactivar(@PathVariable UUID id) {
        return gestionar.desactivar(new OperarAlojamientoCommand(SesionActual.exigirUsuarioId(), id));
    }

    @PostMapping("/alojamientos/{id}/activar")
    @Operation(summary = "Activar / reactivar alojamiento")
    public AlojamientoRegistradoResponse activar(@PathVariable UUID id) {
        return gestionar.activar(new OperarAlojamientoCommand(SesionActual.exigirUsuarioId(), id));
    }

    @GetMapping("/alojamientos")
    @Operation(summary = "Listar mis alojamientos")
    public List<AlojamientoResumenResponse> listar() {
        return gestionar.listar(SesionActual.exigirUsuarioId());
    }

    @GetMapping("/alojamientos/{id}")
    @Operation(summary = "Obtener mi alojamiento")
    public AlojamientoRegistradoResponse obtener(@PathVariable UUID id) {
        return gestionar.obtener(SesionActual.exigirUsuarioId(), id);
    }

    @GetMapping("/alojamientos/{id}/unidades/{unidadId}")
    @Operation(summary = "Obtener unidad / habitación")
    public UnidadResponse obtenerUnidad(@PathVariable UUID id, @PathVariable UUID unidadId) {
        return gestionar.obtenerUnidad(SesionActual.exigirUsuarioId(), id, unidadId);
    }

    @PostMapping("/alojamientos/{id}/unidades/{unidadId}/enviar-revision")
    @Operation(summary = "Enviar unidad a revisión")
    public AlojamientoRegistradoResponse enviarUnidadARevision(
            @PathVariable UUID id, @PathVariable UUID unidadId) {
        return gestionar.enviarUnidadARevision(
                new OperarUnidadCommand(SesionActual.exigirUsuarioId(), id, unidadId));
    }

    @PostMapping("/alojamientos/{id}/unidades/{unidadId}/desactivar")
    @Operation(summary = "Desactivar unidad")
    public AlojamientoRegistradoResponse desactivarUnidad(
            @PathVariable UUID id, @PathVariable UUID unidadId) {
        return gestionar.desactivarUnidad(
                new OperarUnidadCommand(SesionActual.exigirUsuarioId(), id, unidadId));
    }

    @PostMapping("/alojamientos/{id}/unidades/{unidadId}/activar")
    @Operation(summary = "Activar unidad")
    public AlojamientoRegistradoResponse activarUnidad(
            @PathVariable UUID id, @PathVariable UUID unidadId) {
        return gestionar.activarUnidad(
                new OperarUnidadCommand(SesionActual.exigirUsuarioId(), id, unidadId));
    }

    public record RegistroRequest(Integer tipoPropiedadId) {
    }

    public record UbicacionRequest(
            String direccion,
            String ciudad,
            String departamento,
            BigDecimal latitud,
            BigDecimal longitud) {
    }

    public record PresentacionRequest(
            String titulo,
            String descripcion,
            String politicas,
            String horarioEntrada,
            String horarioSalida) {
    }

    public record PoliticasRequest(String politicas, String horarioEntrada, String horarioSalida) {
    }

    public record ServiciosRequest(List<Integer> servicios) {
    }

    public record FotografiasRequest(List<FotoRequest> fotografias) {
    }

    public record FotoRequest(UUID id, String identificadorAlmacen, String url, Integer orden) {
    }

    public record OfertaUnicaRequest(
            Integer capacidadHuespedes,
            Integer cantidadDormitorios,
            Integer cantidadCamas,
            BigDecimal cantidadBanos,
            BigDecimal precioNoche,
            String politicas,
            String horarioEntrada,
            String horarioSalida) {
    }

    public record AgregarUnidadRequest(
            Integer tipoEspacioId,
            String nombre,
            String descripcion,
            Integer capacidadHuespedes,
            Integer cantidadDormitorios,
            Integer cantidadCamas,
            BigDecimal cantidadBanos,
            Integer inventarioTotal,
            BigDecimal precioNoche,
            List<FotoRequest> fotografias,
            List<Integer> servicios) {
    }
}
