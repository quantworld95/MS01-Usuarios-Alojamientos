package com.alojate.identidad.infrastructure.adapters.input.rest;

import com.alojate.config.Permisos;
import com.alojate.config.SesionActual;
import com.alojate.identidad.application.dto.CambiarActivoRolCommand;
import com.alojate.identidad.application.dto.CrearRolCommand;
import com.alojate.identidad.application.dto.ModificarRolCommand;
import com.alojate.identidad.application.dto.RolResponse;
import com.alojate.identidad.application.ports.input.GestionarRoles;
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

@RestController
@RequestMapping("/roles")
@Tag(
        name = "Roles",
        description = """
                Gestión de roles y permisos asignados. Requiere JWT + ADMINISTRAR_ROLES.
                Para ver permisos disponibles por módulo: GET /modulos.
                """)
public class RolController {

    private final GestionarRoles roles;

    public RolController(GestionarRoles roles) {
        this.roles = roles;
    }

    @GetMapping
    @Operation(summary = "Listar roles")
    public List<RolResponse> listar() {
        SesionActual.exigirPermiso(Permisos.ADMINISTRAR_ROLES);
        return roles.listarRoles();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener rol")
    public RolResponse obtener(@PathVariable int id) {
        SesionActual.exigirPermiso(Permisos.ADMINISTRAR_ROLES);
        return roles.obtener(id);
    }

    @PostMapping
    @Operation(summary = "Crear rol", description = "nombre, descripcion y lista de ids de permiso.")
    public ResponseEntity<?> crear(@RequestBody GuardarRolRequest solicitud) {
        SesionActual.exigirPermiso(Permisos.ADMINISTRAR_ROLES);
        return ResponseEntity.status(HttpStatus.CREATED).body(roles.crear(new CrearRolCommand(
                solicitud.nombre(), solicitud.descripcion(), solicitud.permisos())));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modificar rol y sus permisos")
    public RolResponse modificar(@PathVariable int id, @RequestBody GuardarRolRequest solicitud) {
        SesionActual.exigirPermiso(Permisos.ADMINISTRAR_ROLES);
        return roles.modificar(new ModificarRolCommand(
                id, solicitud.nombre(), solicitud.descripcion(), solicitud.permisos()));
    }

    @PatchMapping("/{id}/activo")
    @Operation(summary = "Activar / desactivar rol")
    public RolResponse cambiarActivo(@PathVariable int id, @RequestBody CambiarActivoRolRequest solicitud) {
        SesionActual.exigirPermiso(Permisos.ADMINISTRAR_ROLES);
        return roles.cambiarActivo(new CambiarActivoRolCommand(id, solicitud.activo()));
    }
}
