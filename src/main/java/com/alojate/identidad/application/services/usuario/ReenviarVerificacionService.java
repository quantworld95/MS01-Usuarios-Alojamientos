package com.alojate.identidad.application.services.usuario;

import com.alojate.identidad.application.dto.ReenviarVerificacionCommand;
import com.alojate.identidad.application.dto.VerificacionReenviadaResponse;
import com.alojate.identidad.application.ports.input.ReenviarVerificacion;
import com.alojate.identidad.application.ports.output.EmailPort;
import com.alojate.identidad.application.ports.output.Reloj;
import com.alojate.identidad.application.ports.output.UnitOfWork;
import com.alojate.identidad.domain.exception.ReglaNegocioException;
import com.alojate.identidad.domain.usuario.aggregate.Usuario;
import com.alojate.identidad.domain.usuario.repository.UsuarioRepository;
import com.alojate.identidad.domain.usuario.valueobject.Email;

public final class ReenviarVerificacionService implements ReenviarVerificacion {

    private final UsuarioRepository usuarios;
    private final EmailPort correo;
    private final Reloj reloj;
    private final UnitOfWork unitOfWork;

    public ReenviarVerificacionService(
            UsuarioRepository usuarios,
            EmailPort correo,
            Reloj reloj,
            UnitOfWork unitOfWork) {
        this.usuarios = usuarios;
        this.correo = correo;
        this.reloj = reloj;
        this.unitOfWork = unitOfWork;
    }

    @Override
    public VerificacionReenviadaResponse reenviar(ReenviarVerificacionCommand comando) {
        Email email = new Email(comando.email());
        Usuario usuario = usuarios.buscarPorCorreoPublico(email)
                .orElseThrow(() -> new ReglaNegocioException(
                        "CUENTA_NO_ENCONTRADA", "No existe una cuenta pública con ese correo"));

        unitOfWork.ejecutar(() -> {
            usuario.reenviarVerificacion(reloj.ahora());
            usuarios.guardar(usuario);
        });

        try {
            correo.enviarCodigoVerificacion(usuario.email(), usuario.codigoVerificacion());
        } catch (ReglaNegocioException excepcion) {
            throw excepcion;
        } catch (RuntimeException excepcion) {
            throw new ReglaNegocioException(
                    "CORREO_NO_ENVIADO",
                    "No se pudo enviar el código de verificación. Puedes solicitar otro.");
        }

        return new VerificacionReenviadaResponse(usuario.id().valor(), usuario.estado().name());
    }
}
