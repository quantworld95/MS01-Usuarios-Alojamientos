package com.alojate.identidad.infrastructure.adapters.input.rest;

import com.alojate.config.Permisos;
import com.alojate.config.SesionActual;
import com.alojate.identidad.application.dto.CambiarEstadoUsuarioPublicoCommand;
import com.alojate.identidad.application.dto.CrearRestriccionCommand;
import com.alojate.identidad.application.dto.ModificarTrabajadorCommand;
import com.alojate.identidad.application.dto.RegistrarTrabajadorCommand;
import com.alojate.identidad.application.dto.RegistrarUsuarioCommand;
import com.alojate.identidad.application.dto.ReenviarVerificacionCommand;
import com.alojate.identidad.application.dto.RestriccionResponse;
import com.alojate.identidad.application.dto.TrabajadorResponse;
import com.alojate.identidad.application.dto.UsuarioPublicoResponse;
import com.alojate.identidad.application.dto.VerificarCorreoCommand;
import com.alojate.identidad.application.ports.input.GestionarUsuarioCorporativo;
import com.alojate.identidad.application.ports.input.GestionarUsuarioPublico;
import com.alojate.identidad.application.ports.input.RegistrarUsuarioPublico;
import com.alojate.identidad.application.ports.input.ReenviarVerificacion;
import com.alojate.identidad.application.ports.input.VerificarCorreo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/usuarios")
@Tag(
        name = "Usuarios",
        description = """
                Cuentas públicas y corporativas.

                **Público (sin token):** registrar → verificar correo → luego POST /sesiones.
                **Admin (JWT + permiso):** corporativos, listado de públicos, estado y restricciones.
                """)
public class UsuarioController {

    private final RegistrarUsuarioPublico registrar;
    private final VerificarCorreo verificar;
    private final ReenviarVerificacion reenviar;
    private final GestionarUsuarioCorporativo trabajadores;
    private final GestionarUsuarioPublico publicos;

    public UsuarioController(
            RegistrarUsuarioPublico registrar,
            VerificarCorreo verificar,
            ReenviarVerificacion reenviar,
            GestionarUsuarioCorporativo trabajadores,
            GestionarUsuarioPublico publicos) {
        this.registrar = registrar;
        this.verificar = verificar;
        this.reenviar = reenviar;
        this.trabajadores = trabajadores;
        this.publicos = publicos;
    }

    @PostMapping
    @Operation(
            summary = "1. Registrar usuario público",
            description = "Sin autenticación. Deja la cuenta pendiente de verificación. Luego POST /usuarios/verificacion.")
    public ResponseEntity<?> registrar(@RequestBody RegistrarUsuarioRequest solicitud) {
        return ResponseEntity.status(HttpStatus.CREATED).body(registrar.registrar(new RegistrarUsuarioCommand(
                solicitud.nombre(),
                solicitud.apellido(),
                solicitud.email(),
                solicitud.telefono(),
                solicitud.contrasena())));
    }

    @PostMapping("/verificacion")
    @Operation(
            summary = "2. Verificar correo",
            description = "Body: email + codigo recibido. Activa la cuenta para poder iniciar sesión.")
    public ResponseEntity<?> verificar(@RequestBody VerificarCorreoRequest solicitud) {
        return ResponseEntity.ok(verificar.verificar(new VerificarCorreoCommand(
                solicitud.email(), solicitud.codigo())));
    }

    @PostMapping("/verificacion/reenvio")
    @Operation(summary = "Reenviar código de verificación", description = "Si el código expiró o no llegó el correo.")
    public ResponseEntity<?> reenviar(@RequestBody ReenviarVerificacionRequest solicitud) {
        return ResponseEntity.ok(reenviar.reenviar(new ReenviarVerificacionCommand(solicitud.email())));
    }

    @GetMapping("/corporativos")
    @Operation(summary = "Listar trabajadores", description = "Requiere permiso ADMINISTRAR_TRABAJADORES.")
    public List<TrabajadorResponse> listarTrabajadores() {
        SesionActual.exigirPermiso(Permisos.ADMINISTRAR_TRABAJADORES);
        return trabajadores.listar();
    }

    @GetMapping("/corporativos/{id}")
    @Operation(summary = "Obtener trabajador", description = "Requiere permiso ADMINISTRAR_TRABAJADORES.")
    public TrabajadorResponse obtenerTrabajador(@PathVariable UUID id) {
        SesionActual.exigirPermiso(Permisos.ADMINISTRAR_TRABAJADORES);
        return trabajadores.obtener(id);
    }

    @PostMapping("/corporativos")
    @Operation(
            summary = "Crear trabajador",
            description = "Cuenta corporativa con rol administrativo. Requiere ADMINISTRAR_TRABAJADORES.")
    public ResponseEntity<?> registrarTrabajador(@RequestBody RegistrarTrabajadorRequest solicitud) {
        SesionActual.exigirPermiso(Permisos.ADMINISTRAR_TRABAJADORES);
        return ResponseEntity.status(HttpStatus.CREATED).body(trabajadores.registrar(
                new RegistrarTrabajadorCommand(
                        solicitud.nombre(),
                        solicitud.apellido(),
                        solicitud.email(),
                        solicitud.telefono(),
                        solicitud.contrasena(),
                        solicitud.idRol())));
    }

    @PutMapping("/corporativos/{id}")
    @Operation(summary = "Modificar trabajador", description = "Datos, rol y activo. Requiere ADMINISTRAR_TRABAJADORES.")
    public TrabajadorResponse modificarTrabajador(
            @PathVariable UUID id, @RequestBody ModificarTrabajadorRequest solicitud) {
        SesionActual.exigirPermiso(Permisos.ADMINISTRAR_TRABAJADORES);
        return trabajadores.modificar(new ModificarTrabajadorCommand(
                id,
                solicitud.nombre(),
                solicitud.apellido(),
                solicitud.email(),
                solicitud.telefono(),
                solicitud.idRol(),
                solicitud.activo()));
    }

    @GetMapping("/publicos")
    @Operation(summary = "Listar usuarios públicos", description = "Requiere ADMINISTRAR_USUARIOS_PUBLICOS.")
    public List<UsuarioPublicoResponse> listarPublicos() {
        SesionActual.exigirPermiso(Permisos.ADMINISTRAR_USUARIOS_PUBLICOS);
        return publicos.listar();
    }

    @GetMapping("/publicos/{id}")
    @Operation(summary = "Obtener usuario público", description = "Requiere ADMINISTRAR_USUARIOS_PUBLICOS.")
    public UsuarioPublicoResponse obtenerPublico(@PathVariable UUID id) {
        SesionActual.exigirPermiso(Permisos.ADMINISTRAR_USUARIOS_PUBLICOS);
        return publicos.obtener(id);
    }

    @PatchMapping("/publicos/{id}/estado")
    @Operation(
            summary = "Cambiar estado de cuenta pública",
            description = "Activa, suspende, etc. Requiere ADMINISTRAR_USUARIOS_PUBLICOS.")
    public UsuarioPublicoResponse cambiarEstadoPublico(
            @PathVariable UUID id, @RequestBody CambiarEstadoUsuarioPublicoRequest solicitud) {
        SesionActual.exigirPermiso(Permisos.ADMINISTRAR_USUARIOS_PUBLICOS);
        return publicos.cambiarEstado(new CambiarEstadoUsuarioPublicoCommand(id, solicitud.estado()));
    }

    @PostMapping("/publicos/{id}/restricciones")
    @Operation(
            summary = "Restringir un permiso al usuario",
            description = "Bloquea un permiso concreto (idPermiso + motivo). Requiere ADMINISTRAR_USUARIOS_PUBLICOS.")
    public ResponseEntity<RestriccionResponse> restringir(
            @PathVariable UUID id, @RequestBody CrearRestriccionRequest solicitud) {
        SesionActual.exigirPermiso(Permisos.ADMINISTRAR_USUARIOS_PUBLICOS);
        return ResponseEntity.status(HttpStatus.CREATED).body(publicos.restringir(new CrearRestriccionCommand(
                id, SesionActual.exigirUsuarioId(), solicitud.idPermiso(), solicitud.motivo())));
    }

    @PostMapping("/publicos/{id}/restricciones/{restriccionId}/levantamiento")
    @Operation(summary = "Levantar restricción", description = "Requiere ADMINISTRAR_USUARIOS_PUBLICOS.")
    public RestriccionResponse levantar(@PathVariable UUID id, @PathVariable UUID restriccionId) {
        SesionActual.exigirPermiso(Permisos.ADMINISTRAR_USUARIOS_PUBLICOS);
        return publicos.levantar(id, restriccionId);
    }
}
