package com.alojate.identidad.application.services.usuario;

import com.alojate.identidad.application.dto.IniciarSesionCommand;
import com.alojate.identidad.application.dto.SesionResponse;
import com.alojate.identidad.application.ports.input.IniciarSesion;
import com.alojate.identidad.application.ports.output.EmisorDeSesion;
import com.alojate.identidad.application.ports.output.PasswordHasherPort;
import com.alojate.identidad.application.ports.output.Reloj;
import com.alojate.identidad.domain.exception.ReglaNegocioException;
import com.alojate.identidad.domain.modulo.aggregate.Modulo;
import com.alojate.identidad.domain.modulo.aggregate.Permiso;
import com.alojate.identidad.domain.modulo.repository.ModuloRepository;
import com.alojate.identidad.domain.modulo.valueobject.PermisoId;
import com.alojate.identidad.domain.restriccion.repository.RestriccionRepository;
import com.alojate.identidad.domain.rol.aggregate.Rol;
import com.alojate.identidad.domain.rol.repository.RolRepository;
import com.alojate.identidad.domain.rol.valueobject.AmbitoRol;
import com.alojate.identidad.domain.usuario.aggregate.Usuario;
import com.alojate.identidad.domain.usuario.repository.UsuarioRepository;
import com.alojate.identidad.domain.usuario.valueobject.Contrasena;
import com.alojate.identidad.domain.usuario.valueobject.Email;
import com.alojate.identidad.domain.usuario.valueobject.TipoCuenta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class IniciarSesionService implements IniciarSesion {

    public static final String CONTEXTO_GUEST = "GUEST";
    public static final String CONTEXTO_ADMIN = "ADMIN";

    private final UsuarioRepository usuarios;
    private final RolRepository roles;
    private final ModuloRepository modulos;
    private final RestriccionRepository restricciones;
    private final PasswordHasherPort cifrador;
    private final EmisorDeSesion sesiones;
    private final Reloj reloj;

    public IniciarSesionService(
            UsuarioRepository usuarios,
            RolRepository roles,
            ModuloRepository modulos,
            RestriccionRepository restricciones,
            PasswordHasherPort cifrador,
            EmisorDeSesion sesiones,
            Reloj reloj) {
        this.usuarios = usuarios;
        this.roles = roles;
        this.modulos = modulos;
        this.restricciones = restricciones;
        this.cifrador = cifrador;
        this.sesiones = sesiones;
        this.reloj = reloj;
    }

    @Override
    public SesionResponse iniciar(IniciarSesionCommand comando) {
        Email email = new Email(comando.email());
        Contrasena contrasena = new Contrasena(comando.contrasena());
        Usuario usuario = localizar(email);
        if (usuario.tipoCuenta() == TipoCuenta.PUBLICA) {
            return sesionPublica(usuario, contrasena);
        }
        return sesionCorporativa(usuario, contrasena);
    }

    private Usuario localizar(Email email) {
        Optional<Usuario> publico = usuarios.buscarPorCorreoPublico(email);
        Optional<Usuario> corporativo = usuarios.buscarPorCorreoCorporativo(email);
        if (publico.isPresent() && corporativo.isPresent()) {
            throw credencialesInvalidas();
        }
        return publico.or(() -> corporativo).orElseThrow(this::credencialesInvalidas);
    }

    private SesionResponse sesionPublica(Usuario usuario, Contrasena contrasena) {
        usuario.exigirSesionPublica();
        if (!cifrador.coincide(contrasena, usuario.passwordHash())) {
            throw credencialesInvalidas();
        }
        Rol rol = exigirRolPublico(usuario);
        Set<PermisoId> restringidos = restricciones.permisosVigentes(usuario.id(), reloj.ahora());
        List<String> permisos = codigos(rol, restringidos);
        String token = sesiones.emitir(usuario.id(), CONTEXTO_GUEST, permisos);
        return new SesionResponse(
                token, usuario.id().valor(), usuario.estado().name(), CONTEXTO_GUEST, permisos);
    }

    private SesionResponse sesionCorporativa(Usuario usuario, Contrasena contrasena) {
        usuario.exigirSesionAdministrativa();
        if (!cifrador.coincide(contrasena, usuario.passwordHash())) {
            throw credencialesInvalidas();
        }
        Rol rol = exigirRolAdministrativo(usuario);
        List<String> permisos = codigos(rol, Set.of());
        String token = sesiones.emitir(usuario.id(), CONTEXTO_ADMIN, permisos);
        return new SesionResponse(
                token, usuario.id().valor(), usuario.estado().name(), CONTEXTO_ADMIN, permisos);
    }

    private Rol exigirRolAdministrativo(Usuario usuario) {
        if (usuario.rolId() == null) {
            throw new ReglaNegocioException(
                    "ROL_NO_ASIGNABLE", "El trabajador no tiene un rol administrativo");
        }
        Rol rol = roles.buscarPorId(usuario.rolId())
                .orElseThrow(() -> new ReglaNegocioException(
                        "ROL_NO_ASIGNABLE", "El trabajador no tiene un rol administrativo"));
        if (rol.ambito() != AmbitoRol.CORPORATIVO) {
            throw new ReglaNegocioException(
                    "ROL_NO_ASIGNABLE", "El trabajador solo puede recibir un rol corporativo");
        }
        if (!rol.activo()) {
            throw new ReglaNegocioException("ROL_INACTIVO", "El rol está desactivado");
        }
        return rol;
    }

    private Rol exigirRolPublico(Usuario usuario) {
        if (usuario.rolId() == null) {
            throw new ReglaNegocioException(
                    "ROL_NO_ASIGNABLE", "La cuenta pública no tiene el rol USUARIO_PUBLICO");
        }
        Rol rol = roles.buscarPorId(usuario.rolId())
                .orElseThrow(() -> new ReglaNegocioException(
                        "ROL_NO_ASIGNABLE", "La cuenta pública no tiene el rol USUARIO_PUBLICO"));
        if (!rol.esUsuarioPublicoAsignable()) {
            throw new ReglaNegocioException(
                    "ROL_NO_ASIGNABLE", "La cuenta pública solo puede recibir el rol USUARIO_PUBLICO");
        }
        if (!rol.activo()) {
            throw new ReglaNegocioException("ROL_INACTIVO", "El rol está desactivado");
        }
        return rol;
    }

    private List<String> codigos(Rol rol, Set<PermisoId> restringidos) {
        Map<Integer, String> catalogo = new HashMap<>();
        for (Modulo modulo : modulos.listar()) {
            for (Permiso permiso : modulo.permisos()) {
                catalogo.put(permiso.id().valor(), permiso.codigo().valor());
            }
        }
        return rol.permisos().stream()
                .filter(permiso -> !restringidos.contains(permiso))
                .map(PermisoId::valor)
                .map(catalogo::get)
                .filter(codigo -> codigo != null)
                .sorted()
                .toList();
    }

    private ReglaNegocioException credencialesInvalidas() {
        return new ReglaNegocioException("CREDENCIALES_INVALIDAS", "El correo o la contraseña no son válidos");
    }
}
