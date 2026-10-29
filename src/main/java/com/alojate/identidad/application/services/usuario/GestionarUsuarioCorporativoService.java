package com.alojate.identidad.application.services.usuario;

import com.alojate.identidad.application.dto.ModificarTrabajadorCommand;
import com.alojate.identidad.application.dto.RegistrarTrabajadorCommand;
import com.alojate.identidad.application.dto.TrabajadorRegistradoResponse;
import com.alojate.identidad.application.dto.TrabajadorResponse;
import com.alojate.identidad.application.ports.input.GestionarUsuarioCorporativo;
import com.alojate.identidad.application.ports.output.AuditoriaPort;
import com.alojate.identidad.application.ports.output.PasswordHasherPort;
import com.alojate.identidad.application.ports.output.Reloj;
import com.alojate.identidad.application.ports.output.UnitOfWork;
import com.alojate.identidad.domain.exception.ReglaNegocioException;
import com.alojate.identidad.domain.rol.aggregate.Rol;
import com.alojate.identidad.domain.rol.repository.RolRepository;
import com.alojate.identidad.domain.rol.valueobject.RolId;
import com.alojate.identidad.domain.usuario.aggregate.Usuario;
import com.alojate.identidad.domain.usuario.event.TrabajadorIncorporado;
import com.alojate.identidad.domain.usuario.factory.UsuarioFactory;
import com.alojate.identidad.domain.usuario.repository.UsuarioRepository;
import com.alojate.identidad.domain.usuario.valueobject.Contrasena;
import com.alojate.identidad.domain.usuario.valueobject.Email;
import com.alojate.identidad.domain.usuario.valueobject.NombreCompleto;
import com.alojate.identidad.domain.usuario.valueobject.PasswordHash;
import com.alojate.identidad.domain.usuario.valueobject.Telefono;
import com.alojate.identidad.domain.usuario.valueobject.TipoCuenta;
import com.alojate.identidad.domain.usuario.valueobject.UsuarioId;

import java.util.List;
import java.util.UUID;

public final class GestionarUsuarioCorporativoService implements GestionarUsuarioCorporativo {

    private final UsuarioRepository usuarios;
    private final RolRepository roles;
    private final UsuarioFactory fabrica;
    private final PasswordHasherPort cifrador;
    private final Reloj reloj;
    private final UnitOfWork unitOfWork;
    private final AuditoriaPort auditoria;

    public GestionarUsuarioCorporativoService(
            UsuarioRepository usuarios,
            RolRepository roles,
            UsuarioFactory fabrica,
            PasswordHasherPort cifrador,
            Reloj reloj,
            UnitOfWork unitOfWork,
            AuditoriaPort auditoria) {
        this.usuarios = usuarios;
        this.roles = roles;
        this.fabrica = fabrica;
        this.cifrador = cifrador;
        this.reloj = reloj;
        this.unitOfWork = unitOfWork;
        this.auditoria = auditoria;
    }

    @Override
    public TrabajadorRegistradoResponse registrar(RegistrarTrabajadorCommand comando) {
        NombreCompleto nombre = new NombreCompleto(comando.nombre(), comando.apellido());
        Email email = new Email(comando.email());
        Telefono telefono = new Telefono(comando.telefono());
        PasswordHash passwordHash = cifrador.cifrar(new Contrasena(comando.contrasena()));
        RolId rolId = new RolId(comando.idRol());
        Usuario trabajador = unitOfWork.ejecutar(() -> {
            exigirCorreoLibre(email, null);
            Rol rol = buscarRol(rolId);
            Usuario usuario = fabrica.registrarCorporativo(
                    nombre, email, telefono, passwordHash, rol, reloj.ahora());
            usuarios.guardar(usuario);
            return usuario;
        });
        trabajador.eventos().stream()
                .filter(TrabajadorIncorporado.class::isInstance)
                .map(TrabajadorIncorporado.class::cast)
                .forEach(auditoria::registrar);
        return new TrabajadorRegistradoResponse(
                trabajador.id().valor(), trabajador.estado().name(), trabajador.rolId().valor());
    }

    @Override
    public List<TrabajadorResponse> listar() {
        return usuarios.listarCorporativos().stream().map(this::aTrabajador).toList();
    }

    @Override
    public TrabajadorResponse obtener(UUID id) {
        return aTrabajador(buscarCorporativo(new UsuarioId(id)));
    }

    @Override
    public TrabajadorResponse modificar(ModificarTrabajadorCommand comando) {
        if (comando.activo() == null) {
            throw new ReglaNegocioException("DATO_INVALIDO", "El estado de la cuenta es obligatorio");
        }
        NombreCompleto nombre = new NombreCompleto(comando.nombre(), comando.apellido());
        Email email = new Email(comando.email());
        Telefono telefono = new Telefono(comando.telefono());
        RolId rolId = new RolId(comando.idRol());
        UsuarioId id = new UsuarioId(comando.id());
        Usuario guardado = unitOfWork.ejecutar(() -> {
            Usuario usuario = buscarCorporativo(id);
            exigirCorreoLibre(email, id);
            usuario.modificarDatos(nombre, email, telefono);
            usuario.asignarRol(buscarRol(rolId));
            if (comando.activo()) {
                usuario.reactivar();
            } else {
                usuario.desactivar();
            }
            usuarios.guardar(usuario);
            return usuario;
        });
        return aTrabajador(guardado);
    }

    private Usuario buscarCorporativo(UsuarioId id) {
        Usuario usuario = usuarios.buscarPorId(id)
                .orElseThrow(() -> new ReglaNegocioException("CUENTA_NO_ENCONTRADA", "La cuenta no existe"));
        if (usuario.tipoCuenta() != TipoCuenta.CORPORATIVA) {
            throw new ReglaNegocioException("CUENTA_PUBLICA", "La cuenta no es corporativa");
        }
        return usuario;
    }

    private Rol buscarRol(RolId rolId) {
        return roles.buscarPorId(rolId)
                .orElseThrow(() -> new ReglaNegocioException("ROL_NO_ENCONTRADO", "El rol no existe"));
    }

    private void exigirCorreoLibre(Email email, UsuarioId excepto) {
        if (usuarios.correoOcupado(email, excepto)) {
            throw new ReglaNegocioException("CORREO_REGISTRADO", "El correo ya está registrado");
        }
    }

    private TrabajadorResponse aTrabajador(Usuario usuario) {
        return new TrabajadorResponse(
                usuario.id().valor(),
                usuario.nombre().nombre(),
                usuario.nombre().apellido(),
                usuario.email().valor(),
                usuario.telefono().valor(),
                usuario.estado().name(),
                usuario.rolId().valor());
    }
}
