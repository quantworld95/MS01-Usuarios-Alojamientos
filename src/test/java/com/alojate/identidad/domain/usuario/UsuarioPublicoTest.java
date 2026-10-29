package com.alojate.identidad.domain.usuario;

import com.alojate.identidad.domain.exception.ReglaNegocioException;
import com.alojate.identidad.domain.rol.valueobject.RolId;
import com.alojate.identidad.domain.usuario.aggregate.Usuario;
import com.alojate.identidad.domain.usuario.valueobject.Email;
import com.alojate.identidad.domain.usuario.valueobject.EstadoCuenta;
import com.alojate.identidad.domain.usuario.valueobject.NombreCompleto;
import com.alojate.identidad.domain.usuario.valueobject.PasswordHash;
import com.alojate.identidad.domain.usuario.valueobject.Telefono;
import com.alojate.identidad.domain.usuario.valueobject.TipoCuenta;
import com.alojate.identidad.domain.usuario.valueobject.UsuarioId;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UsuarioPublicoTest {

    @Test
    void suspendeDesactivaYVuelveAActivar() {
        Usuario usuario = activo();

        usuario.suspender();
        assertEquals(EstadoCuenta.SUSPENDIDA, usuario.estado());

        usuario.desactivar();
        assertEquals(EstadoCuenta.DESACTIVADA, usuario.estado());
        assertEquals("CUENTA_DESACTIVADA", assertThrows(
                ReglaNegocioException.class, usuario::exigirSesionPublica).codigo());

        usuario.activar();
        assertEquals(EstadoCuenta.ACTIVA, usuario.estado());
    }

    @Test
    void unaCuentaPendienteNoSeSuspende() {
        Usuario usuario = Usuario.registrarPublico(
                new NombreCompleto("Ana", "Perez"),
                new Email("ana@ejemplo.com"),
                new Telefono("70000000"),
                new PasswordHash("hash-de-prueba-valido"),
                Instant.parse("2026-10-20T15:00:00Z"));

        assertEquals("CUENTA_NO_ACTIVA", assertThrows(ReglaNegocioException.class, usuario::suspender).codigo());
    }

    private Usuario activo() {
        return Usuario.reconstituir(
                UsuarioId.generar(),
                new NombreCompleto("Ana", "Perez"),
                new Email("ana@ejemplo.com"),
                new Telefono("70000000"),
                new PasswordHash("hash-de-prueba-valido"),
                TipoCuenta.PUBLICA,
                EstadoCuenta.ACTIVA,
                new RolId(1),
                Instant.parse("2026-10-20T15:00:00Z"),
                null);
    }
}
