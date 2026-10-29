package com.alojate.identidad.infrastructure.adapters.input.rest;

import com.alojate.identidad.application.dto.IniciarSesionCommand;
import com.alojate.identidad.application.dto.SesionResponse;
import com.alojate.identidad.application.ports.input.IniciarSesion;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sesiones")
@Tag(
        name = "Sesión",
        description = """
                Login. Devuelve JWT para Authorize en Swagger.

                Flujo: POST /usuarios → POST /usuarios/verificacion → POST /sesiones → Authorize.
                """)
public class SesionController {

    private final IniciarSesion iniciarSesion;

    public SesionController(IniciarSesion iniciarSesion) {
        this.iniciarSesion = iniciarSesion;
    }

    @PostMapping
    @Operation(
            summary = "Iniciar sesión",
            description = "Body: email + contrasena. Copia el token de la respuesta y úsalo en Authorize (sin escribir Bearer).")
    public SesionResponse iniciar(@RequestBody IniciarSesionRequest solicitud) {
        return iniciarSesion.iniciar(new IniciarSesionCommand(solicitud.email(), solicitud.contrasena()));
    }
}
