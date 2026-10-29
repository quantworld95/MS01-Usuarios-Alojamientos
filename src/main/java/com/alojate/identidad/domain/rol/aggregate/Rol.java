package com.alojate.identidad.domain.rol.aggregate;

import com.alojate.identidad.domain.exception.ReglaNegocioException;
import com.alojate.identidad.domain.modulo.valueobject.PermisoId;
import com.alojate.identidad.domain.rol.event.EventoDeRol;
import com.alojate.identidad.domain.rol.event.PermisosDeRolActualizados;
import com.alojate.identidad.domain.rol.service.PoliticaProteccionRol;
import com.alojate.identidad.domain.rol.valueobject.AmbitoRol;
import com.alojate.identidad.domain.rol.valueobject.DescripcionRol;
import com.alojate.identidad.domain.rol.valueobject.NombreRol;
import com.alojate.identidad.domain.rol.valueobject.RolId;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class Rol {

    private RolId id;
    private NombreRol nombre;
    private DescripcionRol descripcion;
    private final AmbitoRol ambito;
    private final boolean esSistema;
    private boolean activo;
    private Set<RolPermiso> permisos;
    private final List<EventoDeRol> eventos = new ArrayList<>();

    private Rol(
            RolId id,
            NombreRol nombre,
            DescripcionRol descripcion,
            AmbitoRol ambito,
            boolean esSistema,
            boolean activo,
            Set<RolPermiso> permisos) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.ambito = ambito;
        this.esSistema = esSistema;
        this.activo = activo;
        this.permisos = permisos;
    }

    public static Rol crear(NombreRol nombre, DescripcionRol descripcion, Set<PermisoId> permisos) {
        return new Rol(
                null,
                nombre,
                descripcion,
                AmbitoRol.CORPORATIVO,
                false,
                true,
                asignaciones(permisos, true));
    }

    public static Rol reconstituir(RolId id, NombreRol nombre, AmbitoRol ambito, boolean esSistema, boolean activo) {
        return reconstituir(id, nombre, new DescripcionRol(null), ambito, esSistema, activo, Set.of());
    }

    public static Rol reconstituir(
            RolId id,
            NombreRol nombre,
            DescripcionRol descripcion,
            AmbitoRol ambito,
            boolean esSistema,
            boolean activo,
            Set<PermisoId> permisos) {
        return new Rol(id, nombre, descripcion, ambito, esSistema, activo, asignaciones(permisos, false));
    }

    public void modificar(NombreRol nombre, DescripcionRol descripcion, Set<PermisoId> permisos) {
        new PoliticaProteccionRol().exigirModificable(this);
        if (id == null) {
            throw new ReglaNegocioException("DATO_INVALIDO", "El rol todavía no está guardado");
        }
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.permisos = asignaciones(permisos, true);
        this.eventos.add(new PermisosDeRolActualizados(id, permisos()));
    }

    public void cambiarActivo(boolean activo) {
        new PoliticaProteccionRol().exigirModificable(this);
        if (id == null) {
            throw new ReglaNegocioException("DATO_INVALIDO", "El rol todavía no está guardado");
        }
        this.activo = activo;
    }

    public Rol identificado(int idGenerado) {
        if (id != null) {
            throw new IllegalStateException("El rol ya tiene identificador");
        }
        Rol persistido = new Rol(
                new RolId(idGenerado), nombre, descripcion, ambito, esSistema, activo, permisos);
        persistido.eventos.add(new PermisosDeRolActualizados(persistido.id, persistido.permisos()));
        return persistido;
    }

    public boolean esUsuarioPublicoAsignable() {
        return esSistema && activo && ambito == AmbitoRol.PUBLICO && nombre.esUsuarioPublico();
    }

    public RolId id() {
        return id;
    }

    public NombreRol nombre() {
        return nombre;
    }

    public DescripcionRol descripcion() {
        return descripcion;
    }

    public AmbitoRol ambito() {
        return ambito;
    }

    public boolean esSistema() {
        return esSistema;
    }

    public boolean activo() {
        return activo;
    }

    public Set<PermisoId> permisos() {
        return permisos.stream().map(RolPermiso::permisoId).collect(Collectors.toUnmodifiableSet());
    }

    public List<EventoDeRol> eventos() {
        return List.copyOf(eventos);
    }

    private static Set<RolPermiso> asignaciones(Set<PermisoId> permisos, boolean exigirAlguno) {
        if (permisos == null || permisos.isEmpty()) {
            if (exigirAlguno) {
                throw new ReglaNegocioException("DATO_INVALIDO", "El rol debe incluir al menos un permiso");
            }
            return Set.of();
        }
        return permisos.stream().map(RolPermiso::new).collect(Collectors.toUnmodifiableSet());
    }
}
