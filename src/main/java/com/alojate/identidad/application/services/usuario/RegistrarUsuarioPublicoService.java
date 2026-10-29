package com.alojate.identidad.application.services.usuario;

import com.alojate.identidad.application.dto.RegistrarUsuarioCommand;
import com.alojate.identidad.application.dto.UsuarioRegistradoResponse;
import com.alojate.identidad.application.ports.input.RegistrarUsuarioPublico;
import com.alojate.identidad.application.ports.output.EmailPort;
import com.alojate.identidad.application.ports.output.PasswordHasherPort;
import com.alojate.identidad.application.ports.output.Reloj;
import com.alojate.identidad.application.ports.output.UnitOfWork;
import com.alojate.identidad.domain.exception.ReglaNegocioException;
import com.alojate.identidad.domain.usuario.aggregate.Usuario;
import com.alojate.identidad.domain.usuario.factory.UsuarioFactory;
import com.alojate.identidad.domain.usuario.repository.UsuarioRepository;
import com.alojate.identidad.domain.usuario.valueobject.Contrasena;
import com.alojate.identidad.domain.usuario.valueobject.Email;
import com.alojate.identidad.domain.usuario.valueobject.NombreCompleto;
import com.alojate.identidad.domain.usuario.valueobject.PasswordHash;
import com.alojate.identidad.domain.usuario.valueobject.Telefono;

public final class RegistrarUsuarioPublicoService implements RegistrarUsuarioPublico {

    private final UsuarioRepository usuarios;
    private final UsuarioFactory fabrica;
    private final PasswordHasherPort cifrador;
    private final EmailPort correo;
    private final Reloj reloj;
    private final UnitOfWork unitOfWork;

    public RegistrarUsuarioPublicoService(
            UsuarioRepository usuarios,
            UsuarioFactory fabrica,
            PasswordHasherPort cifrador,
            EmailPort correo,
            Reloj reloj,
            UnitOfWork unitOfWork) {
        this.usuarios = usuarios;
        this.fabrica = fabrica;
        this.cifrador = cifrador;
        this.correo = correo;
        this.reloj = reloj;
        this.unitOfWork = unitOfWork;
    }

    @Override
    public UsuarioRegistradoResponse registrar(RegistrarUsuarioCommand comando) {
        NombreCompleto nombre = new NombreCompleto(comando.nombre(), comando.apellido());
        Email email = new Email(comando.email());
        Telefono telefono = new Telefono(comando.telefono());
        PasswordHash passwordHash = cifrador.cifrar(new Contrasena(comando.contrasena()));
        Usuario usuario = fabrica.registrarPublico(nombre, email, telefono, passwordHash, reloj.ahora());

        unitOfWork.ejecutar(() -> {
            if (usuarios.correoOcupado(email, null)) {
                throw new ReglaNegocioException("CORREO_REGISTRADO", "El correo ya está registrado");
            }
            usuarios.guardar(usuario);
        });

        enviarCodigo(usuario);
        return new UsuarioRegistradoResponse(usuario.id().valor(), usuario.estado().name());
    }

    private void enviarCodigo(Usuario usuario) {
        try {
            correo.enviarCodigoVerificacion(usuario.email(), usuario.codigoVerificacion());
        } catch (ReglaNegocioException excepcion) {
            throw excepcion;
        } catch (RuntimeException excepcion) {
            throw new ReglaNegocioException(
                    "CORREO_NO_ENVIADO",
                    "No se pudo enviar el código de verificación. Puedes solicitar otro.");
        }
    }
}
