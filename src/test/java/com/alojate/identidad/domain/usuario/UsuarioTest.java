package com.alojate.identidad.domain.usuario;

import com.alojate.identidad.domain.exception.ReglaNegocioException;
import com.alojate.identidad.domain.rol.aggregate.Rol;
import com.alojate.identidad.domain.rol.valueobject.AmbitoRol;
import com.alojate.identidad.domain.rol.valueobject.NombreRol;
import com.alojate.identidad.domain.rol.valueobject.RolId;
import com.alojate.identidad.domain.usuario.aggregate.Usuario;
import com.alojate.identidad.domain.usuario.event.CorreoVerificado;
import com.alojate.identidad.domain.usuario.event.UsuarioRegistrado;
import com.alojate.identidad.domain.usuario.factory.UsuarioFactory;
import com.alojate.identidad.domain.usuario.valueobject.CodigoVerificacion;
import com.alojate.identidad.domain.usuario.valueobject.Contrasena;
import com.alojate.identidad.domain.usuario.valueobject.Email;
import com.alojate.identidad.domain.usuario.valueobject.EstadoCuenta;
import com.alojate.identidad.domain.usuario.valueobject.NombreCompleto;
import com.alojate.identidad.domain.usuario.valueobject.PasswordHash;
import com.alojate.identidad.domain.usuario.valueobject.Telefono;
import com.alojate.identidad.domain.usuario.valueobject.TipoCuenta;
import com.alojate.identidad.domain.usuario.valueobject.UsuarioId;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UsuarioTest {

    private static final Instant AHORA = Instant.parse("2026-10-20T15:00:00Z");

    private final UsuarioFactory fabrica = new UsuarioFactory();

    @Test
    void elRegistroNacePendienteSinRolYConCodigoVigente() {
        Usuario usuario = registrar();

        assertEquals(EstadoCuenta.PENDIENTE_VERIFICACION, usuario.estado());
        assertEquals(TipoCuenta.PUBLICA, usuario.tipoCuenta());
        assertNull(usuario.rolId());
        assertEquals(AHORA.plus(CodigoVerificacion.VIGENCIA), usuario.codigoVerificacion().vence());
        assertEquals(Duration.ofHours(24), CodigoVerificacion.VIGENCIA);
        assertInstanceOf(UsuarioRegistrado.class, usuario.eventos().getFirst());
    }

    @Test
    void verificarCorreoActivaLaCuentaYAsignaUsuarioPublico() {
        Usuario usuario = registrar();
        String codigo = usuario.codigoVerificacion().codigo();

        usuario.verificarCorreo(codigo, AHORA.plus(Duration.ofHours(1)), rolPublico());

        assertEquals(EstadoCuenta.ACTIVA, usuario.estado());
        assertEquals(new RolId(1), usuario.rolId());
        assertNull(usuario.codigoVerificacion());
        assertInstanceOf(CorreoVerificado.class, usuario.eventos().get(1));
    }

    @Test
    void unCodigoIncorrectoMantieneLaCuentaPendiente() {
        Usuario usuario = registrar();

        ReglaNegocioException error = assertThrows(
                ReglaNegocioException.class,
                () -> usuario.verificarCorreo("000000", AHORA.plus(Duration.ofMinutes(1)), rolPublico()));

        assertEquals("CODIGO_INCORRECTO", error.codigo());
        assertEquals(EstadoCuenta.PENDIENTE_VERIFICACION, usuario.estado());
        assertNull(usuario.rolId());
    }

    @Test
    void unCodigoVencidoMantieneLaCuentaPendiente() {
        Usuario usuario = registrar();
        String codigo = usuario.codigoVerificacion().codigo();

        ReglaNegocioException error = assertThrows(
                ReglaNegocioException.class,
                () -> usuario.verificarCorreo(codigo, AHORA.plus(Duration.ofHours(24)), rolPublico()));

        assertEquals("CODIGO_VENCIDO", error.codigo());
        assertEquals(EstadoCuenta.PENDIENTE_VERIFICACION, usuario.estado());
    }

    @Test
    void reenviarGeneraOtroCodigoYMantieneLaCuentaPendiente() {
        Usuario usuario = registrar();

        usuario.reenviarVerificacion(AHORA.plus(Duration.ofHours(2)));

        assertEquals(EstadoCuenta.PENDIENTE_VERIFICACION, usuario.estado());
        assertEquals(AHORA.plus(Duration.ofHours(26)), usuario.codigoVerificacion().vence());
    }

    @Test
    void noReenviaCuandoLaCuentaYaEstaActiva() {
        Usuario usuario = registrar();
        usuario.verificarCorreo(usuario.codigoVerificacion().codigo(), AHORA.plusSeconds(1), rolPublico());

        ReglaNegocioException error = assertThrows(
                ReglaNegocioException.class,
                () -> usuario.reenviarVerificacion(AHORA.plusSeconds(2)));

        assertEquals("CUENTA_NO_PENDIENTE", error.codigo());
    }

    @Test
    void rechazaUnRolDistintoAlActivar() {
        Usuario usuario = registrar();
        Rol otroRol = Rol.reconstituir(
                new RolId(2), new NombreRol("EDITOR"), AmbitoRol.CORPORATIVO, false, true);

        ReglaNegocioException error = assertThrows(
                ReglaNegocioException.class,
                () -> usuario.verificarCorreo(
                        usuario.codigoVerificacion().codigo(), AHORA.plusSeconds(1), otroRol));

        assertEquals("ROL_NO_ASIGNABLE", error.codigo());
        assertEquals(EstadoCuenta.PENDIENTE_VERIFICACION, usuario.estado());
    }

    @Test
    void rechazaDatosInvalidos() {
        assertThrows(ReglaNegocioException.class, () -> new Email("correo-invalido"));
        assertThrows(ReglaNegocioException.class, () -> new NombreCompleto(" ", "Perez"));
        assertThrows(ReglaNegocioException.class, () -> new Telefono(" "));
        assertThrows(ReglaNegocioException.class, () -> new PasswordHash("corta"));
        assertThrows(ReglaNegocioException.class, () -> new Contrasena("1234567"));
    }

    @Test
    void unaCuentaPublicaActivaPuedeIniciarSesion() {
        Usuario usuario = registrar();
        usuario.verificarCorreo(usuario.codigoVerificacion().codigo(), AHORA.plusSeconds(1), rolPublico());

        usuario.exigirSesionPublica();

        assertEquals(EstadoCuenta.ACTIVA, usuario.estado());
    }

    @Test
    void unaCuentaPendienteNoIniciaSesionPublica() {
        ReglaNegocioException error = assertThrows(ReglaNegocioException.class, () -> registrar().exigirSesionPublica());

        assertEquals("CUENTA_PENDIENTE", error.codigo());
    }

    @Test
    void unaCuentaSuspendidaNoIniciaSesionPublica() {
        Usuario usuario = cuenta(TipoCuenta.PUBLICA, EstadoCuenta.SUSPENDIDA);

        ReglaNegocioException error = assertThrows(ReglaNegocioException.class, usuario::exigirSesionPublica);

        assertEquals("CUENTA_SUSPENDIDA", error.codigo());
    }

    @Test
    void unaCuentaCorporativaNoIniciaSesionPublica() {
        Usuario usuario = cuenta(TipoCuenta.CORPORATIVA, EstadoCuenta.ACTIVA);

        ReglaNegocioException error = assertThrows(ReglaNegocioException.class, usuario::exigirSesionPublica);

        assertEquals("CUENTA_CORPORATIVA", error.codigo());
    }

    private Usuario cuenta(TipoCuenta tipo, EstadoCuenta estado) {
        return Usuario.reconstituir(
                UsuarioId.generar(),
                new NombreCompleto("Ana", "Perez"),
                new Email("ana@ejemplo.com"),
                new Telefono("70000000"),
                new PasswordHash("hash-de-prueba-valido"),
                tipo,
                estado,
                new RolId(1),
                AHORA,
                null);
    }

    private Usuario registrar() {
        return fabrica.registrarPublico(
                new NombreCompleto("Ana", "Perez"),
                new Email("Ana@Ejemplo.com"),
                new Telefono("70000000"),
                new PasswordHash("hash-de-prueba-valido"),
                AHORA);
    }

    private Rol rolPublico() {
        return Rol.reconstituir(
                new RolId(1),
                new NombreRol(NombreRol.USUARIO_PUBLICO),
                AmbitoRol.PUBLICO,
                true,
                true);
    }
}
