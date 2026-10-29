package com.alojate.identidad.application.services.rol;

import com.alojate.identidad.application.dto.CambiarActivoRolCommand;
import com.alojate.identidad.application.dto.CrearRolCommand;
import com.alojate.identidad.application.dto.ModificarRolCommand;
import com.alojate.identidad.application.ports.output.AuditoriaPort;
import com.alojate.identidad.domain.exception.ReglaNegocioException;
import com.alojate.identidad.domain.modulo.repository.ModuloRepository;
import com.alojate.identidad.domain.modulo.valueobject.PermisoId;
import com.alojate.identidad.domain.rol.aggregate.Rol;
import com.alojate.identidad.domain.rol.event.PermisosDeRolActualizados;
import com.alojate.identidad.domain.rol.valueobject.AmbitoRol;
import com.alojate.identidad.domain.rol.valueobject.DescripcionRol;
import com.alojate.identidad.domain.rol.valueobject.NombreRol;
import com.alojate.identidad.domain.rol.valueobject.RolId;
import com.alojate.identidad.support.RolesEnMemoria;
import com.alojate.identidad.support.UnidadDeTrabajoEnMemoria;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GestionarRolesServiceTest {

    private final RolesEnMemoria roles = RolesEnMemoria.con(
            Rol.reconstituir(new RolId(1), new NombreRol("USUARIO_PUBLICO"), AmbitoRol.PUBLICO, true, true),
            Rol.reconstituir(
                    new RolId(2),
                    new NombreRol("SUPERADMIN"),
                    new DescripcionRol("Total"),
                    AmbitoRol.CORPORATIVO,
                    true,
                    true,
                    Set.of(new PermisoId(1))));
    private final AuditoriaEnMemoria auditoria = new AuditoriaEnMemoria();
    private final GestionarRolesService servicio = new GestionarRolesService(
            new ModulosEnMemoria(Set.of(new PermisoId(1), new PermisoId(2))),
            roles,
            new UnidadDeTrabajoEnMemoria(),
            auditoria);

    @Test
    void creaUnRolConSusPermisosYLoAudita() {
        var respuesta = servicio.crear(new CrearRolCommand("Soporte", "Incidencias", List.of(1, 2)));

        assertEquals(List.of(1, 2), respuesta.permisos());
        assertEquals(1, auditoria.eventos.size());
        assertTrue(roles.buscarPorId(new RolId(respuesta.id())).isPresent());
    }

    @Test
    void rechazaUnNombreDuplicadoSinAuditar() {
        servicio.crear(new CrearRolCommand("Soporte", null, List.of(1)));

        ReglaNegocioException error = assertThrows(
                ReglaNegocioException.class,
                () -> servicio.crear(new CrearRolCommand("soporte", null, List.of(1))));

        assertEquals("ROL_DUPLICADO", error.codigo());
        assertEquals(1, auditoria.eventos.size());
    }

    @Test
    void rechazaUnPermisoInexistente() {
        ReglaNegocioException error = assertThrows(
                ReglaNegocioException.class,
                () -> servicio.crear(new CrearRolCommand("Soporte", null, List.of(99))));

        assertEquals("PERMISO_NO_ASIGNABLE", error.codigo());
        assertTrue(auditoria.eventos.isEmpty());
    }

    @Test
    void modificarReemplazaLosPermisos() {
        var creado = servicio.crear(new CrearRolCommand("Soporte", null, List.of(1, 2)));

        var actualizado = servicio.modificar(new ModificarRolCommand(creado.id(), "Soporte", "Nuevo", List.of(2)));

        assertEquals(List.of(2), actualizado.permisos());
        assertEquals(List.of(2), roles.buscarPorId(new RolId(creado.id())).orElseThrow().permisos().stream()
                .map(PermisoId::valor).sorted().toList());
    }

    @Test
    void noModificaLosRolesDeSistema() {
        ReglaNegocioException publico = assertThrows(
                ReglaNegocioException.class,
                () -> servicio.modificar(new ModificarRolCommand(1, "USUARIO_PUBLICO", null, List.of(1))));
        ReglaNegocioException superadmin = assertThrows(
                ReglaNegocioException.class,
                () -> servicio.modificar(new ModificarRolCommand(2, "SUPERADMIN", null, List.of(1))));

        assertEquals("ROL_PROTEGIDO", publico.codigo());
        assertEquals("ROL_PROTEGIDO", superadmin.codigo());
        assertTrue(auditoria.eventos.isEmpty());
    }

    @Test
    void obtieneUnRolPorId() {
        var creado = servicio.crear(new CrearRolCommand("Soporte", "Incidencias", List.of(1)));

        var obtenido = servicio.obtener(creado.id());

        assertEquals(creado.id(), obtenido.id());
        assertEquals("Soporte", obtenido.nombre());
        assertTrue(obtenido.activo());
    }

    @Test
    void desactivaYReactivaUnRolCorporativo() {
        var creado = servicio.crear(new CrearRolCommand("Soporte", null, List.of(1)));

        var inactivo = servicio.cambiarActivo(new CambiarActivoRolCommand(creado.id(), false));
        var activo = servicio.cambiarActivo(new CambiarActivoRolCommand(creado.id(), true));

        assertFalse(inactivo.activo());
        assertTrue(activo.activo());
        assertTrue(roles.buscarPorId(new RolId(creado.id())).orElseThrow().activo());
    }

    @Test
    void noCambiaElEstadoDeUnRolDeSistema() {
        ReglaNegocioException error = assertThrows(
                ReglaNegocioException.class,
                () -> servicio.cambiarActivo(new CambiarActivoRolCommand(2, false)));

        assertEquals("ROL_PROTEGIDO", error.codigo());
        assertTrue(roles.buscarPorId(new RolId(2)).orElseThrow().activo());
    }

    private static final class ModulosEnMemoria implements ModuloRepository {
        private final Set<PermisoId> asignables;

        private ModulosEnMemoria(Set<PermisoId> asignables) {
            this.asignables = asignables;
        }

        @Override
        public List<com.alojate.identidad.domain.modulo.aggregate.Modulo> listar() {
            return List.of();
        }

        @Override
        public Set<PermisoId> buscarAsignables(Set<PermisoId> permisos) {
            return permisos.stream().filter(asignables::contains).collect(java.util.stream.Collectors.toSet());
        }
    }

    private static final class AuditoriaEnMemoria implements AuditoriaPort {
        private final List<PermisosDeRolActualizados> eventos = new ArrayList<>();

        @Override
        public void registrar(PermisosDeRolActualizados evento) {
            eventos.add(evento);
        }

        @Override
        public void registrar(com.alojate.identidad.domain.usuario.event.TrabajadorIncorporado evento) {
        }
    }
}
