package com.alojate.identidad.infrastructure.adapters.input.rest;

import com.alojate.config.Permisos;
import com.alojate.config.SesionActual;
import com.alojate.identidad.application.dto.ModuloResponse;
import com.alojate.identidad.application.ports.input.GestionarRoles;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/modulos")
@Tag(
        name = "Módulos y permisos",
        description = """
                Catálogo de módulos con sus permisos (para armar roles).
                Requiere JWT + ADMINISTRAR_ROLES.
                """)
public class ModuloController {

    private final GestionarRoles roles;

    public ModuloController(GestionarRoles roles) {
        this.roles = roles;
    }

    @GetMapping
    @Operation(summary = "Listar módulos con permisos")
    public List<ModuloResponse> listar() {
        SesionActual.exigirPermiso(Permisos.ADMINISTRAR_ROLES);
        return roles.listarModulos();
    }
}
