package com.alojate.identidad.application.services.usuario;

import com.alojate.identidad.application.dto.CorreoVerificadoResponse;
import com.alojate.identidad.application.dto.VerificarCorreoCommand;
import com.alojate.identidad.application.ports.input.VerificarCorreo;
import com.alojate.identidad.application.ports.output.Reloj;
import com.alojate.identidad.application.ports.output.UnitOfWork;
import com.alojate.identidad.domain.exception.ReglaNegocioException;
import com.alojate.identidad.domain.rol.aggregate.Rol;
import com.alojate.identidad.domain.rol.repository.RolRepository;
import com.alojate.identidad.domain.usuario.aggregate.Usuario;
import com.alojate.identidad.domain.usuario.repository.UsuarioRepository;
import com.alojate.identidad.domain.usuario.valueobject.Email;

import java.time.Instant;

public final class VerificarCorreoService implements VerificarCorreo {

    private final UsuarioRepository usuarios;
    private final RolRepository roles;
    private final Reloj reloj;
    private final UnitOfWork unitOfWork;

    public VerificarCorreoService(
            UsuarioRepository usuarios,
            RolRepository roles,
            Reloj reloj,
            UnitOfWork unitOfWork) {
        this.usuarios = usuarios;
        this.roles = roles;
        this.reloj = reloj;
        this.unitOfWork = unitOfWork;
    }

    @Override
    public CorreoVerificadoResponse verificar(VerificarCorreoCommand comando) {
        Email email = new Email(comando.email());
        Usuario usuario = usuarios.buscarPorCorreoPublico(email)
                .orElseThrow(() -> new ReglaNegocioException(
                        "CUENTA_NO_ENCONTRADA", "No existe una cuenta pública con ese correo"));
        Rol rolPublico = roles.buscarUsuarioPublico()
                .orElseThrow(() -> new ReglaNegocioException(
                        "ROL_NO_ASIGNABLE", "El rol USUARIO_PUBLICO no está disponible"));
        Instant ahora = reloj.ahora();

        unitOfWork.ejecutar(() -> {
            usuario.verificarCorreo(comando.codigo(), ahora, rolPublico);
            usuarios.guardar(usuario);
        });

        return new CorreoVerificadoResponse(usuario.id().valor(), usuario.estado().name());
    }
}
