package com.alojate.identidad.application.services.usuario;

import com.alojate.identidad.application.dto.IniciarSesionCommand;
import com.alojate.identidad.application.ports.output.PasswordHasherPort;
import com.alojate.identidad.domain.exception.ReglaNegocioException;
import com.alojate.identidad.domain.modulo.aggregate.Modulo;
import com.alojate.identidad.domain.modulo.aggregate.Permiso;
import com.alojate.identidad.domain.modulo.repository.ModuloRepository;
import com.alojate.identidad.domain.modulo.valueobject.CodigoPermiso;
import com.alojate.identidad.domain.modulo.valueobject.ModuloId;
import com.alojate.identidad.domain.modulo.valueobject.PermisoId;
import com.alojate.identidad.domain.restriccion.aggregate.RestriccionUsuario;
import com.alojate.identidad.domain.rol.aggregate.Rol;
import com.alojate.identidad.domain.rol.valueobject.AmbitoRol;
import com.alojate.identidad.domain.rol.valueobject.DescripcionRol;
import com.alojate.identidad.domain.rol.valueobject.NombreRol;
import com.alojate.identidad.domain.rol.valueobject.RolId;
import com.alojate.identidad.domain.usuario.aggregate.Usuario;
import com.alojate.identidad.support.RestriccionesEnMemoria;
import com.alojate.identidad.support.RolesEnMemoria;
import com.alojate.identidad.support.UsuariosEnMemoria;
import com.alojate.identidad.domain.usuario.valueobject.Contrasena;
import com.alojate.identidad.domain.usuario.valueobject.Email;
import com.alojate.identidad.domain.usuario.valueobject.EstadoCuenta;
import com.alojate.identidad.domain.usuario.valueobject.NombreCompleto;
import com.alojate.identidad.domain.usuario.valueobject.PasswordHash;
import com.alojate.identidad.domain.usuario.valueobject.Telefono;
import com.alojate.identidad.domain.usuario.valueobject.TipoCuenta;
import com.alojate.identidad.domain.usuario.valueobject.UsuarioId;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IniciarSesionServiceTest {

    private static final Instant AHORA = Instant.parse("2026-10-20T15:00:00Z");

    private final UsuariosEnMemoria usuarios = new UsuariosEnMemoria();
    private final RestriccionesEnMemoria restricciones = new RestriccionesEnMemoria();
    private final RolesEnMemoria roles = RolesEnMemoria.con(
            Rol.reconstituir(
                    new RolId(1),
                    new NombreRol(NombreRol.USUARIO_PUBLICO),
                    new DescripcionRol(null),
                    AmbitoRol.PUBLICO,
                    true,
                    true,
                    Set.of(new PermisoId(8))),
            Rol.reconstituir(
                    new RolId(2),
                    new NombreRol("SUPERADMIN"),
                    new DescripcionRol("Administración"),
                    AmbitoRol.CORPORATIVO,
                    true,
                    true,
                    Set.of(new PermisoId(7))),
            Rol.reconstituir(
                    new RolId(3),
                    new NombreRol("SOPORTE"),
                    new DescripcionRol(null),
                    AmbitoRol.CORPORATIVO,
                    false,
                    false,
                    Set.of(new PermisoId(7))));
    private final IniciarSesionService servicio = new IniciarSesionService(
            usuarios,
            roles,
            new ModulosEnMemoria(),
            restricciones,
            new CifradorDePrueba(),
            (usuarioId, contexto) -> usuarioId.valor() + ":" + contexto,
            () -> AHORA);

    @Test
    void unaCuentaPublicaActivaObtieneLasFuncionesDelRol() {
        guardar("ana@ejemplo.com", EstadoCuenta.ACTIVA, TipoCuenta.PUBLICA, new RolId(1));

        var respuesta = servicio.iniciar(new IniciarSesionCommand("ana@ejemplo.com", "secreta123"));

        assertEquals("GUEST", respuesta.contexto());
        assertEquals("ACTIVA", respuesta.estado());
        assertEquals(List.of("BUSCAR_ALOJAMIENTOS"), respuesta.permisos());
        assertTrue(respuesta.token().endsWith(":GUEST"));
    }

    @Test
    void unaRestriccionVigenteQuitaSoloEsaFuncion() {
        Usuario usuario = guardar("ana@ejemplo.com", EstadoCuenta.ACTIVA, TipoCuenta.PUBLICA, new RolId(1));
        restricciones.guardar(RestriccionUsuario.crear(
                usuario.id(),
                new PermisoId(8),
                UsuarioId.generar(),
                "No puede buscar",
                AHORA));

        var respuesta = servicio.iniciar(new IniciarSesionCommand("ana@ejemplo.com", "secreta123"));

        assertEquals(List.of(), respuesta.permisos());
    }

    @Test
    void unaCuentaCorporativaActivaObtieneSesionAdminConPermisos() {
        guardar("luis@alojate.bo", EstadoCuenta.ACTIVA, TipoCuenta.CORPORATIVA, new RolId(2));

        var respuesta = servicio.iniciar(new IniciarSesionCommand("luis@alojate.bo", "secreta123"));

        assertEquals("ADMIN", respuesta.contexto());
        assertEquals(List.of("ADMINISTRAR_ROLES"), respuesta.permisos());
        assertTrue(respuesta.token().endsWith(":ADMIN"));
    }

    @Test
    void unaContrasenaIncorrectaNoEntra() {
        guardar("ana@ejemplo.com", EstadoCuenta.ACTIVA, TipoCuenta.PUBLICA, new RolId(1));

        ReglaNegocioException error = assertThrows(
                ReglaNegocioException.class,
                () -> servicio.iniciar(new IniciarSesionCommand("ana@ejemplo.com", "otraClave1")));

        assertEquals("CREDENCIALES_INVALIDAS", error.codigo());
    }

    @Test
    void unaCuentaPendienteNoEntra() {
        guardar("ana@ejemplo.com", EstadoCuenta.PENDIENTE_VERIFICACION, TipoCuenta.PUBLICA, null);

        ReglaNegocioException error = assertThrows(
                ReglaNegocioException.class,
                () -> servicio.iniciar(new IniciarSesionCommand("ana@ejemplo.com", "secreta123")));

        assertEquals("CUENTA_PENDIENTE", error.codigo());
    }

    @Test
    void unaCuentaSuspendidaNoEntra() {
        guardar("ana@ejemplo.com", EstadoCuenta.SUSPENDIDA, TipoCuenta.PUBLICA, new RolId(1));

        ReglaNegocioException error = assertThrows(
                ReglaNegocioException.class,
                () -> servicio.iniciar(new IniciarSesionCommand("ana@ejemplo.com", "secreta123")));

        assertEquals("CUENTA_SUSPENDIDA", error.codigo());
    }

    @Test
    void unaCuentaDesactivadaNoEntra() {
        guardar("luis@alojate.bo", EstadoCuenta.DESACTIVADA, TipoCuenta.CORPORATIVA, new RolId(2));

        ReglaNegocioException error = assertThrows(
                ReglaNegocioException.class,
                () -> servicio.iniciar(new IniciarSesionCommand("luis@alojate.bo", "secreta123")));

        assertEquals("CUENTA_DESACTIVADA", error.codigo());
    }

    @Test
    void unRolInactivoNoEntra() {
        guardar("luis@alojate.bo", EstadoCuenta.ACTIVA, TipoCuenta.CORPORATIVA, new RolId(3));

        ReglaNegocioException error = assertThrows(
                ReglaNegocioException.class,
                () -> servicio.iniciar(new IniciarSesionCommand("luis@alojate.bo", "secreta123")));

        assertEquals("ROL_INACTIVO", error.codigo());
    }

    private Usuario guardar(String email, EstadoCuenta estado, TipoCuenta tipo, RolId rolId) {
        Usuario usuario = Usuario.reconstituir(
                UsuarioId.generar(),
                new NombreCompleto("Ana", "Perez"),
                new Email(email),
                new Telefono("70000000"),
                new PasswordHash("hash:secreta123"),
                tipo,
                estado,
                rolId,
                AHORA,
                null);
        usuarios.guardar(usuario);
        return usuario;
    }

    private static final class ModulosEnMemoria implements ModuloRepository {
        @Override
        public List<Modulo> listar() {
            return List.of(
                    Modulo.reconstituir(
                            new ModuloId(1),
                            "IDENTIDAD",
                            "Gestión de identidad y acceso",
                            1,
                            List.of(Permiso.reconstituir(
                                    new PermisoId(7),
                                    new CodigoPermiso("ADMINISTRAR_ROLES"),
                                    "Administrar roles",
                                    null,
                                    true))),
                    Modulo.reconstituir(
                            new ModuloId(2),
                            "ALOJAMIENTOS",
                            "Publicación, consulta y moderación",
                            2,
                            List.of(Permiso.reconstituir(
                                    new PermisoId(8),
                                    new CodigoPermiso("BUSCAR_ALOJAMIENTOS"),
                                    "Buscar alojamientos",
                                    null,
                                    false))));
        }

        @Override
        public Set<PermisoId> buscarAsignables(Set<PermisoId> permisos) {
            return Set.of();
        }
    }

    private static final class CifradorDePrueba implements PasswordHasherPort {
        @Override
        public PasswordHash cifrar(Contrasena contrasena) {
            return new PasswordHash("hash:" + contrasena.valor());
        }

        @Override
        public boolean coincide(Contrasena contrasena, PasswordHash hash) {
            return hash.valor().equals("hash:" + contrasena.valor());
        }
    }
}
