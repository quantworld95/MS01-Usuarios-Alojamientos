package com.alojate.identidad.domain.rol;

import com.alojate.identidad.domain.exception.ReglaNegocioException;
import com.alojate.identidad.domain.modulo.valueobject.PermisoId;
import com.alojate.identidad.domain.rol.aggregate.Rol;
import com.alojate.identidad.domain.rol.event.PermisosDeRolActualizados;
import com.alojate.identidad.domain.rol.valueobject.AmbitoRol;
import com.alojate.identidad.domain.rol.valueobject.DescripcionRol;
import com.alojate.identidad.domain.rol.valueobject.NombreRol;
import com.alojate.identidad.domain.rol.valueobject.RolId;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RolTest {

    @Test
    void unRolNuevoQuedaCorporativoYRegistraSusPermisosAlGuardarse() {
        Rol rol = Rol.crear(
                new NombreRol("Soporte"),
                new DescripcionRol("Atiende incidencias"),
                Set.of(new PermisoId(2), new PermisoId(3)));

        Rol guardado = rol.identificado(8);

        assertEquals(8, guardado.id().valor());
        assertEquals(AmbitoRol.CORPORATIVO, guardado.ambito());
        assertEquals(Set.of(new PermisoId(2), new PermisoId(3)), guardado.permisos());
        assertEquals(1, guardado.eventos().size());
        assertTrue(guardado.eventos().getFirst() instanceof PermisosDeRolActualizados);
    }

    @Test
    void modificarReemplazaElConjuntoDePermisos() {
        Rol rol = Rol.reconstituir(
                new RolId(4),
                new NombreRol("Soporte"),
                new DescripcionRol("Inicial"),
                AmbitoRol.CORPORATIVO,
                false,
                true,
                Set.of(new PermisoId(1), new PermisoId(2)));

        rol.modificar(new NombreRol("Mesa de ayuda"), new DescripcionRol("Actualizado"), Set.of(new PermisoId(3)));

        assertEquals("Mesa de ayuda", rol.nombre().valor());
        assertEquals(Set.of(new PermisoId(3)), rol.permisos());
    }

    @Test
    void unRolDeSistemaNoSeModifica() {
        Rol publico = Rol.reconstituir(
                new RolId(1), new NombreRol(NombreRol.USUARIO_PUBLICO), AmbitoRol.PUBLICO, true, true);
        Rol superadmin = Rol.reconstituir(
                new RolId(2),
                new NombreRol("SUPERADMIN"),
                new DescripcionRol("Administración total"),
                AmbitoRol.CORPORATIVO,
                true,
                true,
                Set.of(new PermisoId(1)));

        assertEquals("ROL_PROTEGIDO", rechazo(publico).codigo());
        assertEquals("ROL_PROTEGIDO", rechazo(superadmin).codigo());
        assertEquals("ROL_PROTEGIDO", rechazoDeEstado(publico).codigo());
        assertEquals("ROL_PROTEGIDO", rechazoDeEstado(superadmin).codigo());
        assertTrue(publico.activo());
        assertTrue(superadmin.activo());
    }

    @Test
    void unRolCorporativoSeDesactivaYSeVuelveAActivar() {
        Rol rol = Rol.reconstituir(
                new RolId(4),
                new NombreRol("Soporte"),
                new DescripcionRol("Inicial"),
                AmbitoRol.CORPORATIVO,
                false,
                true,
                Set.of(new PermisoId(1)));

        rol.cambiarActivo(false);
        assertFalse(rol.activo());

        rol.cambiarActivo(true);
        assertTrue(rol.activo());
    }

    private ReglaNegocioException rechazo(Rol rol) {
        return assertThrows(
                ReglaNegocioException.class,
                () -> rol.modificar(rol.nombre(), rol.descripcion(), Set.of(new PermisoId(1))));
    }

    private ReglaNegocioException rechazoDeEstado(Rol rol) {
        return assertThrows(ReglaNegocioException.class, () -> rol.cambiarActivo(false));
    }
}
