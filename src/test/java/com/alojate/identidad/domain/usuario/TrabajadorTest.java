package com.alojate.identidad.domain.usuario;

import com.alojate.identidad.domain.exception.ReglaNegocioException;
import com.alojate.identidad.domain.rol.aggregate.Rol;
import com.alojate.identidad.domain.rol.valueobject.AmbitoRol;
import com.alojate.identidad.domain.rol.valueobject.NombreRol;
import com.alojate.identidad.domain.rol.valueobject.RolId;
import com.alojate.identidad.domain.usuario.aggregate.Usuario;
import com.alojate.identidad.domain.usuario.valueobject.Email;
import com.alojate.identidad.domain.usuario.valueobject.EstadoCuenta;
import com.alojate.identidad.domain.usuario.valueobject.NombreCompleto;
import com.alojate.identidad.domain.usuario.valueobject.PasswordHash;
import com.alojate.identidad.domain.usuario.valueobject.Telefono;
import com.alojate.identidad.domain.usuario.valueobject.TipoCuenta;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TrabajadorTest {

    private static final Instant AHORA = Instant.parse("2026-10-20T20:00:00Z");

    @Test
    void unTrabajadorQuedaActivoConSuRol() {
        Usuario trabajador = Usuario.registrarCorporativo(
                new NombreCompleto("Luis", "Quispe"),
                new Email("luis@alojate.bo"),
                new Telefono("70000001"),
                new PasswordHash("hash-de-prueba-valido"),
                rol("SUPERADMIN", AmbitoRol.CORPORATIVO, true),
                AHORA);

        assertEquals(TipoCuenta.CORPORATIVA, trabajador.tipoCuenta());
        assertEquals(EstadoCuenta.ACTIVA, trabajador.estado());
        assertEquals(2, trabajador.rolId().valor());
        assertNull(trabajador.codigoVerificacion());
    }

    @Test
    void rechazaElRolPublicoYUnoInactivo() {
        assertEquals("ROL_NO_ASIGNABLE", rechazo(rol("USUARIO_PUBLICO", AmbitoRol.PUBLICO, true)).codigo());
        assertEquals("ROL_INACTIVO", rechazo(rol("SOPORTE", AmbitoRol.CORPORATIVO, false)).codigo());
    }

    @Test
    void editarCambiaDatosYRolYPuedeDesactivar() {
        Usuario trabajador = trabajadorActivo();

        trabajador.modificarDatos(
                new NombreCompleto("Maria", "Lopez"),
                new Email("maria@alojate.bo"),
                new Telefono("72222222"));
        trabajador.asignarRol(rol("RECEPCION", AmbitoRol.CORPORATIVO, true));
        trabajador.desactivar();

        assertEquals("Maria", trabajador.nombre().nombre());
        assertEquals("maria@alojate.bo", trabajador.email().valor());
        assertEquals("72222222", trabajador.telefono().valor());
        assertEquals(EstadoCuenta.DESACTIVADA, trabajador.estado());
        assertEquals("CUENTA_DESACTIVADA", assertThrows(
                ReglaNegocioException.class, trabajador::exigirSesionAdministrativa).codigo());
    }

    @Test
    void reactivarVuelveADejarLaCuentaActiva() {
        Usuario trabajador = trabajadorActivo();
        trabajador.desactivar();

        trabajador.reactivar();

        assertEquals(EstadoCuenta.ACTIVA, trabajador.estado());
    }

    @Test
    void noAsignaElRolPublicoAlEditar() {
        Usuario trabajador = trabajadorActivo();

        ReglaNegocioException error = assertThrows(
                ReglaNegocioException.class,
                () -> trabajador.asignarRol(rol("USUARIO_PUBLICO", AmbitoRol.PUBLICO, true)));

        assertEquals("ROL_NO_ASIGNABLE", error.codigo());
        assertEquals(2, trabajador.rolId().valor());
    }

    private Usuario trabajadorActivo() {
        return Usuario.registrarCorporativo(
                new NombreCompleto("Luis", "Quispe"),
                new Email("luis@alojate.bo"),
                new Telefono("70000001"),
                new PasswordHash("hash-de-prueba-valido"),
                rol("SUPERADMIN", AmbitoRol.CORPORATIVO, true),
                AHORA);
    }

    private ReglaNegocioException rechazo(Rol rol) {
        return assertThrows(ReglaNegocioException.class, () -> Usuario.registrarCorporativo(
                new NombreCompleto("Luis", "Quispe"),
                new Email("luis@alojate.bo"),
                new Telefono("70000001"),
                new PasswordHash("hash-de-prueba-valido"),
                rol,
                AHORA));
    }

    private Rol rol(String nombre, AmbitoRol ambito, boolean activo) {
        return Rol.reconstituir(new RolId(2), new NombreRol(nombre), ambito, nombre.equals("USUARIO_PUBLICO"), activo);
    }
}
