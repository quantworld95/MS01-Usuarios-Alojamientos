package com.alojate.identidad.application.services.usuario;

import com.alojate.identidad.application.dto.ModificarTrabajadorCommand;
import com.alojate.identidad.application.dto.RegistrarTrabajadorCommand;
import com.alojate.identidad.application.ports.output.AuditoriaPort;
import com.alojate.identidad.application.ports.output.PasswordHasherPort;
import com.alojate.identidad.domain.exception.ReglaNegocioException;
import com.alojate.identidad.domain.rol.aggregate.Rol;
import com.alojate.identidad.domain.rol.valueobject.AmbitoRol;
import com.alojate.identidad.domain.rol.valueobject.NombreRol;
import com.alojate.identidad.domain.rol.valueobject.RolId;
import com.alojate.identidad.domain.usuario.aggregate.Usuario;
import com.alojate.identidad.domain.usuario.event.TrabajadorIncorporado;
import com.alojate.identidad.domain.usuario.factory.UsuarioFactory;
import com.alojate.identidad.support.RolesEnMemoria;
import com.alojate.identidad.support.UnidadDeTrabajoEnMemoria;
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
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegistrarTrabajadorServiceTest {

    private final UsuariosEnMemoria usuarios = new UsuariosEnMemoria();
    private final AuditoriaEnMemoria auditoria = new AuditoriaEnMemoria();
    private final GestionarUsuarioCorporativoService servicio = new GestionarUsuarioCorporativoService(
            usuarios,
            RolesEnMemoria.con(
                    Rol.reconstituir(new RolId(1), new NombreRol("USUARIO_PUBLICO"), AmbitoRol.PUBLICO, true, true),
                    Rol.reconstituir(new RolId(2), new NombreRol("SUPERADMIN"), AmbitoRol.CORPORATIVO, true, true),
                    Rol.reconstituir(new RolId(3), new NombreRol("SOPORTE"), AmbitoRol.CORPORATIVO, false, false),
                    Rol.reconstituir(new RolId(4), new NombreRol("RECEPCION"), AmbitoRol.CORPORATIVO, false, true)),
            new UsuarioFactory(),
            new PasswordHasherPort() {
                @Override
                public PasswordHash cifrar(Contrasena contrasena) {
                    return new PasswordHash("hash:" + contrasena.valor());
                }

                @Override
                public boolean coincide(Contrasena contrasena, PasswordHash hash) {
                    return hash.valor().equals("hash:" + contrasena.valor());
                }
            },
            () -> Instant.parse("2026-10-20T20:00:00Z"),
            new UnidadDeTrabajoEnMemoria(),
            auditoria);

    @Test
    void incorporaUnTrabajadorActivoYLoAudita() {
        var respuesta = servicio.registrar(comando("ana@alojate.bo", 2));

        assertEquals("ACTIVA", respuesta.estado());
        assertEquals(2, respuesta.rolId());
        assertEquals(1, auditoria.eventos.size());
        assertEquals(TipoCuenta.CORPORATIVA, usuarios.buscarPorCorreoCorporativo(new Email("ana@alojate.bo"))
                .orElseThrow().tipoCuenta());
    }

    @Test
    void rechazaUnCorreoYaUsadoPorUnaCuentaPublica() {
        usuarios.guardar(Usuario.reconstituir(
                UsuarioId.generar(),
                new NombreCompleto("Ana", "Perez"),
                new Email("ana@alojate.bo"),
                new Telefono("70000000"),
                new PasswordHash("hash:secreta123"),
                TipoCuenta.PUBLICA,
                EstadoCuenta.ACTIVA,
                new RolId(1),
                Instant.parse("2026-10-20T20:00:00Z"),
                null));

        ReglaNegocioException error = assertThrows(
                ReglaNegocioException.class, () -> servicio.registrar(comando("ana@alojate.bo", 2)));

        assertEquals("CORREO_REGISTRADO", error.codigo());
        assertTrue(auditoria.eventos.isEmpty());
    }

    @Test
    void rechazaElCorreoCorporativoDuplicado() {
        servicio.registrar(comando("ana@alojate.bo", 2));

        ReglaNegocioException error = assertThrows(
                ReglaNegocioException.class, () -> servicio.registrar(comando("Ana@Alojate.bo", 2)));

        assertEquals("CORREO_REGISTRADO", error.codigo());
        assertEquals(1, auditoria.eventos.size());
    }

    @Test
    void rechazaUnRolPublicoYUnoInactivo() {
        assertEquals("ROL_NO_ASIGNABLE", assertThrows(
                ReglaNegocioException.class, () -> servicio.registrar(comando("ana@alojate.bo", 1))).codigo());
        assertEquals("ROL_INACTIVO", assertThrows(
                ReglaNegocioException.class, () -> servicio.registrar(comando("ana@alojate.bo", 3))).codigo());
        assertTrue(auditoria.eventos.isEmpty());
    }

    @Test
    void editaDatosRolYDesactiva() {
        var alta = servicio.registrar(comando("ana@alojate.bo", 2));

        var editado = servicio.modificar(new ModificarTrabajadorCommand(
                alta.usuarioId(), "Luis", "Quispe", "luis@alojate.bo", "71111111", 4, false));

        assertEquals("Luis", editado.nombre());
        assertEquals("luis@alojate.bo", editado.email());
        assertEquals("71111111", editado.telefono());
        assertEquals(4, editado.rolId());
        assertEquals("DESACTIVADA", editado.estado());
        assertEquals(editado.id(), servicio.obtener(alta.usuarioId()).id());
        assertEquals(1, servicio.listar().size());
    }

    @Test
    void reactivaUnaCuentaDesactivada() {
        var alta = servicio.registrar(comando("ana@alojate.bo", 2));
        servicio.modificar(new ModificarTrabajadorCommand(
                alta.usuarioId(), "Ana", "Perez", "ana@alojate.bo", "70000000", 2, false));

        var activa = servicio.modificar(new ModificarTrabajadorCommand(
                alta.usuarioId(), "Ana", "Perez", "ana@alojate.bo", "70000000", 2, true));

        assertEquals("ACTIVA", activa.estado());
    }

    @Test
    void rechazaAsignarElRolPublicoAlEditar() {
        var alta = servicio.registrar(comando("ana@alojate.bo", 2));

        ReglaNegocioException error = assertThrows(ReglaNegocioException.class, () -> servicio.modificar(
                new ModificarTrabajadorCommand(
                        alta.usuarioId(), "Ana", "Perez", "ana@alojate.bo", "70000000", 1, true)));

        assertEquals("ROL_NO_ASIGNABLE", error.codigo());
        assertEquals(2, servicio.obtener(alta.usuarioId()).rolId());
    }

    private RegistrarTrabajadorCommand comando(String email, int rol) {
        return new RegistrarTrabajadorCommand("Ana", "Perez", email, "70000000", "secreta123", rol);
    }

    private static final class AuditoriaEnMemoria implements AuditoriaPort {
        private final List<TrabajadorIncorporado> eventos = new ArrayList<>();

        @Override
        public void registrar(com.alojate.identidad.domain.rol.event.PermisosDeRolActualizados evento) {
        }

        @Override
        public void registrar(TrabajadorIncorporado evento) {
            eventos.add(evento);
        }
    }
}
