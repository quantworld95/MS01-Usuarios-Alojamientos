package com.alojate.identidad.application.services.usuario;

import com.alojate.identidad.application.dto.CambiarEstadoUsuarioPublicoCommand;
import com.alojate.identidad.application.dto.CrearRestriccionCommand;
import com.alojate.identidad.application.dto.RestriccionResponse;
import com.alojate.identidad.application.dto.UsuarioPublicoResponse;
import com.alojate.identidad.application.ports.input.GestionarUsuarioPublico;
import com.alojate.identidad.application.ports.output.Reloj;
import com.alojate.identidad.application.ports.output.UnitOfWork;
import com.alojate.identidad.domain.exception.ReglaNegocioException;
import com.alojate.identidad.domain.modulo.valueobject.PermisoId;
import com.alojate.identidad.domain.restriccion.aggregate.RestriccionUsuario;
import com.alojate.identidad.domain.restriccion.repository.RestriccionRepository;
import com.alojate.identidad.domain.rol.aggregate.Rol;
import com.alojate.identidad.domain.rol.repository.RolRepository;
import com.alojate.identidad.domain.usuario.aggregate.Usuario;
import com.alojate.identidad.domain.usuario.repository.UsuarioRepository;
import com.alojate.identidad.domain.usuario.valueobject.EstadoCuenta;
import com.alojate.identidad.domain.usuario.valueobject.TipoCuenta;
import com.alojate.identidad.domain.usuario.valueobject.UsuarioId;

import java.util.List;
import java.util.UUID;

public final class GestionarUsuarioPublicoService implements GestionarUsuarioPublico {

    private final UsuarioRepository usuarios;
    private final RolRepository roles;
    private final RestriccionRepository restricciones;
    private final Reloj reloj;
    private final UnitOfWork unitOfWork;

    public GestionarUsuarioPublicoService(
            UsuarioRepository usuarios,
            RolRepository roles,
            RestriccionRepository restricciones,
            Reloj reloj,
            UnitOfWork unitOfWork) {
        this.usuarios = usuarios;
        this.roles = roles;
        this.restricciones = restricciones;
        this.reloj = reloj;
        this.unitOfWork = unitOfWork;
    }

    @Override
    public List<UsuarioPublicoResponse> listar() {
        return usuarios.listarPublicos().stream().map(usuario -> aUsuario(usuario, List.of())).toList();
    }

    @Override
    public UsuarioPublicoResponse obtener(UUID id) {
        Usuario usuario = buscarPublico(new UsuarioId(id));
        return aUsuario(usuario, restricciones.listarPorUsuario(usuario.id()));
    }

    @Override
    public UsuarioPublicoResponse cambiarEstado(CambiarEstadoUsuarioPublicoCommand comando) {
        EstadoCuenta destino = estado(comando.estado());
        UsuarioId id = new UsuarioId(comando.id());
        Usuario guardado = unitOfWork.ejecutar(() -> {
            Usuario usuario = buscarPublico(id);
            switch (destino) {
                case ACTIVA -> usuario.activar();
                case SUSPENDIDA -> usuario.suspender();
                case DESACTIVADA -> usuario.desactivar();
                default -> throw new ReglaNegocioException(
                        "DATO_INVALIDO", "El estado debe ser ACTIVA, SUSPENDIDA o DESACTIVADA");
            }
            usuarios.guardar(usuario);
            return usuario;
        });
        return aUsuario(guardado, restricciones.listarPorUsuario(guardado.id()));
    }

    @Override
    public RestriccionResponse restringir(CrearRestriccionCommand comando) {
        UsuarioId usuarioId = new UsuarioId(comando.usuarioId());
        UsuarioId administradorId = new UsuarioId(comando.administradorId());
        PermisoId permisoId = new PermisoId(comando.idPermiso());
        RestriccionUsuario creada = unitOfWork.ejecutar(() -> {
            Usuario usuario = buscarPublico(usuarioId);
            exigirAdministrador(administradorId);
            exigirPermisoDelRolPublico(usuario, permisoId);
            if (restricciones.existeVigente(usuarioId, permisoId, reloj.ahora())) {
                throw new ReglaNegocioException(
                        "RESTRICCION_VIGENTE", "Esa función ya está restringida para esta persona");
            }
            RestriccionUsuario restriccion = RestriccionUsuario.crear(
                    usuarioId, permisoId, administradorId, comando.motivo(), reloj.ahora());
            return restricciones.guardar(restriccion);
        });
        return aRestriccion(creada);
    }

    @Override
    public RestriccionResponse levantar(UUID usuarioId, UUID restriccionId) {
        UsuarioId duenio = new UsuarioId(usuarioId);
        RestriccionUsuario levantada = unitOfWork.ejecutar(() -> {
            RestriccionUsuario restriccion = restricciones.buscarPorId(restriccionId)
                    .filter(actual -> actual.usuarioId().equals(duenio))
                    .orElseThrow(() -> new ReglaNegocioException(
                            "RESTRICCION_NO_ENCONTRADA", "La restricción no existe"));
            restriccion.levantar(reloj.ahora());
            return restricciones.guardar(restriccion);
        });
        return aRestriccion(levantada);
    }

    private void exigirAdministrador(UsuarioId administradorId) {
        Usuario administrador = usuarios.buscarPorId(administradorId)
                .orElseThrow(() -> new ReglaNegocioException("CUENTA_NO_ENCONTRADA", "La cuenta no existe"));
        if (administrador.tipoCuenta() != TipoCuenta.CORPORATIVA || administrador.estado() != EstadoCuenta.ACTIVA) {
            throw new ReglaNegocioException(
                    "CUENTA_NO_ACTIVA", "La restricción la aplica un trabajador con cuenta activa");
        }
    }

    private void exigirPermisoDelRolPublico(Usuario usuario, PermisoId permisoId) {
        if (usuario.rolId() == null) {
            throw new ReglaNegocioException("CUENTA_PENDIENTE", "La cuenta todavía no está activa");
        }
        Rol rol = roles.buscarPorId(usuario.rolId())
                .orElseThrow(() -> new ReglaNegocioException(
                        "ROL_NO_ASIGNABLE", "La cuenta pública no tiene el rol USUARIO_PUBLICO"));
        if (!rol.esUsuarioPublicoAsignable() || !rol.permisos().contains(permisoId)) {
            throw new ReglaNegocioException(
                    "PERMISO_NO_RESTRINGIBLE", "Solo se puede restringir una función del rol público");
        }
    }

    private Usuario buscarPublico(UsuarioId id) {
        Usuario usuario = usuarios.buscarPorId(id)
                .orElseThrow(() -> new ReglaNegocioException("CUENTA_NO_ENCONTRADA", "La cuenta no existe"));
        if (usuario.tipoCuenta() != TipoCuenta.PUBLICA) {
            throw new ReglaNegocioException("CUENTA_CORPORATIVA", "La cuenta no es pública");
        }
        return usuario;
    }

    private EstadoCuenta estado(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new ReglaNegocioException("DATO_INVALIDO", "El estado de la cuenta es obligatorio");
        }
        try {
            return EstadoCuenta.valueOf(valor.trim());
        } catch (IllegalArgumentException excepcion) {
            throw new ReglaNegocioException(
                    "DATO_INVALIDO", "El estado debe ser ACTIVA, SUSPENDIDA o DESACTIVADA");
        }
    }

    private UsuarioPublicoResponse aUsuario(Usuario usuario, List<RestriccionUsuario> restricciones) {
        return new UsuarioPublicoResponse(
                usuario.id().valor(),
                usuario.nombre().nombre(),
                usuario.nombre().apellido(),
                usuario.email().valor(),
                usuario.telefono().valor(),
                usuario.estado().name(),
                usuario.rolId() == null ? null : usuario.rolId().valor(),
                restricciones.stream().map(this::aRestriccion).toList());
    }

    private RestriccionResponse aRestriccion(RestriccionUsuario restriccion) {
        return new RestriccionResponse(
                restriccion.id(),
                restriccion.usuarioId().valor(),
                restriccion.permisoId().valor(),
                restriccion.administradorId().valor(),
                restriccion.motivo(),
                restriccion.fechaInicio(),
                restriccion.fechaFin(),
                restriccion.activa());
    }
}
