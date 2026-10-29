package com.alojate.identidad.application.services.usuario;

import com.alojate.identidad.application.dto.RegistrarUsuarioCommand;
import com.alojate.identidad.application.dto.ReenviarVerificacionCommand;
import com.alojate.identidad.application.dto.VerificarCorreoCommand;
import com.alojate.identidad.application.ports.output.EmailPort;
import com.alojate.identidad.application.ports.output.PasswordHasherPort;
import com.alojate.identidad.application.ports.output.Reloj;
import com.alojate.identidad.domain.exception.ReglaNegocioException;
import com.alojate.identidad.domain.rol.aggregate.Rol;
import com.alojate.identidad.domain.rol.valueobject.AmbitoRol;
import com.alojate.identidad.domain.rol.valueobject.NombreRol;
import com.alojate.identidad.domain.rol.valueobject.RolId;
import com.alojate.identidad.domain.usuario.aggregate.Usuario;
import com.alojate.identidad.support.RolesEnMemoria;
import com.alojate.identidad.support.UnidadDeTrabajoEnMemoria;
import com.alojate.identidad.support.UsuariosEnMemoria;
import com.alojate.identidad.domain.usuario.valueobject.CodigoVerificacion;
import com.alojate.identidad.domain.usuario.valueobject.Contrasena;
import com.alojate.identidad.domain.usuario.valueobject.Email;
import com.alojate.identidad.domain.usuario.valueobject.EstadoCuenta;
import com.alojate.identidad.domain.usuario.valueobject.NombreCompleto;
import com.alojate.identidad.domain.usuario.valueobject.PasswordHash;
import com.alojate.identidad.domain.usuario.valueobject.Telefono;
import com.alojate.identidad.domain.usuario.valueobject.TipoCuenta;
import com.alojate.identidad.domain.usuario.valueobject.UsuarioId;
import com.alojate.identidad.domain.usuario.factory.UsuarioFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegistroUsuarioPublicoServiceTest {

    private static final Instant AHORA = Instant.parse("2026-10-20T15:00:00Z");

    private final UsuariosEnMemoria usuarios = new UsuariosEnMemoria();
    private final RolesEnMemoria roles = RolesEnMemoria.con(Rol.reconstituir(
            new RolId(1), new NombreRol(NombreRol.USUARIO_PUBLICO), AmbitoRol.PUBLICO, true, true));
    private final CorreoEnMemoria correo = new CorreoEnMemoria();
    private final RelojFijo reloj = new RelojFijo(AHORA);

    private RegistrarUsuarioPublicoService registrar;
    private VerificarCorreoService verificar;
    private ReenviarVerificacionService reenviar;

    @BeforeEach
    void preparar() {
        UnidadDeTrabajoEnMemoria unitOfWork = new UnidadDeTrabajoEnMemoria();
        registrar = new RegistrarUsuarioPublicoService(
                usuarios, new UsuarioFactory(), new CifradorDePrueba(), correo, reloj, unitOfWork);
        verificar = new VerificarCorreoService(usuarios, roles, reloj, unitOfWork);
        reenviar = new ReenviarVerificacionService(usuarios, correo, reloj, unitOfWork);
    }

    @Test
    void registraUnaCuentaPendienteYEnviaElCodigo() {
        var respuesta = registrar.registrar(comandoValido());

        Usuario guardado = usuarios.buscarPorCorreoPublico(new Email("ana@ejemplo.com")).orElseThrow();
        assertEquals(EstadoCuenta.PENDIENTE_VERIFICACION.name(), respuesta.estado());
        assertEquals(EstadoCuenta.PENDIENTE_VERIFICACION, guardado.estado());
        assertEquals("hash:secreta123", guardado.passwordHash().valor());
        assertEquals(guardado.codigoVerificacion().codigo(), correo.ultimoCodigo);
    }

    @Test
    void rechazaUnCorreoPublicoYaRegistrado() {
        registrar.registrar(comandoValido());

        ReglaNegocioException error = assertThrows(
                ReglaNegocioException.class, () -> registrar.registrar(comandoValido()));

        assertEquals("CORREO_REGISTRADO", error.codigo());
        assertEquals(1, usuarios.cantidad());
    }

    @Test
    void rechazaUnCorreoYaUsadoPorUnaCuentaCorporativa() {
        usuarios.guardar(Usuario.reconstituir(
                UsuarioId.generar(),
                new NombreCompleto("Ana", "Perez"),
                new Email("ana@ejemplo.com"),
                new Telefono("70000000"),
                new PasswordHash("hash:secreta123"),
                TipoCuenta.CORPORATIVA,
                EstadoCuenta.ACTIVA,
                new RolId(2),
                AHORA,
                null));

        ReglaNegocioException error = assertThrows(
                ReglaNegocioException.class, () -> registrar.registrar(comandoValido()));

        assertEquals("CORREO_REGISTRADO", error.codigo());
        assertEquals(1, usuarios.cantidad());
    }

    @Test
    void verificaElCorreoYAsignaElRolPublico() {
        registrar.registrar(comandoValido());
        String codigo = correo.ultimoCodigo;

        var respuesta = verificar.verificar(new VerificarCorreoCommand("ana@ejemplo.com", codigo));

        Usuario guardado = usuarios.buscarPorCorreoPublico(new Email("ana@ejemplo.com")).orElseThrow();
        assertEquals(EstadoCuenta.ACTIVA.name(), respuesta.estado());
        assertEquals(new RolId(1), guardado.rolId());
    }

    @Test
    void unCodigoVencidoNoActivaLaCuenta() {
        registrar.registrar(comandoValido());
        reloj.ahora = AHORA.plus(CodigoVerificacion.VIGENCIA);

        ReglaNegocioException error = assertThrows(
                ReglaNegocioException.class,
                () -> verificar.verificar(new VerificarCorreoCommand("ana@ejemplo.com", correo.ultimoCodigo)));

        assertEquals("CODIGO_VENCIDO", error.codigo());
        assertEquals(
                EstadoCuenta.PENDIENTE_VERIFICACION,
                usuarios.buscarPorCorreoPublico(new Email("ana@ejemplo.com")).orElseThrow().estado());
    }

    @Test
    void reenviaOtroCodigoYMantieneLaCuentaPendiente() {
        registrar.registrar(comandoValido());
        reloj.ahora = AHORA.plusSeconds(60);

        var respuesta = reenviar.reenviar(new ReenviarVerificacionCommand("ana@ejemplo.com"));

        Usuario guardado = usuarios.buscarPorCorreoPublico(new Email("ana@ejemplo.com")).orElseThrow();
        assertEquals(EstadoCuenta.PENDIENTE_VERIFICACION.name(), respuesta.estado());
        assertEquals(guardado.codigoVerificacion().codigo(), correo.ultimoCodigo);
        assertTrue(guardado.codigoVerificacion().vence().isAfter(AHORA.plus(CodigoVerificacion.VIGENCIA)));
    }

    @Test
    void siElCorreoFallaLaCuentaQuedaPendienteParaReenviar() {
        correo.fallar = true;

        ReglaNegocioException error = assertThrows(
                ReglaNegocioException.class, () -> registrar.registrar(comandoValido()));

        assertEquals("CORREO_NO_ENVIADO", error.codigo());
        assertTrue(usuarios.buscarPorCorreoPublico(new Email("ana@ejemplo.com")).isPresent());
    }

    private RegistrarUsuarioCommand comandoValido() {
        return new RegistrarUsuarioCommand("Ana", "Perez", "ana@ejemplo.com", "70000000", "secreta123");
    }

    private static final class CorreoEnMemoria implements EmailPort {
        private String ultimoCodigo;
        private boolean fallar;

        @Override
        public void enviarCodigoVerificacion(Email email, CodigoVerificacion codigo) {
            if (fallar) {
                throw new IllegalStateException("smtp caido");
            }
            ultimoCodigo = codigo.codigo();
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

    private static final class RelojFijo implements Reloj {
        private Instant ahora;

        private RelojFijo(Instant ahora) {
            this.ahora = ahora;
        }

        @Override
        public Instant ahora() {
            return ahora;
        }
    }
}
