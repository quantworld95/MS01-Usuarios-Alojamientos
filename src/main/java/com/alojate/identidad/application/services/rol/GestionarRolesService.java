package com.alojate.identidad.application.services.rol;

import com.alojate.identidad.application.dto.CambiarActivoRolCommand;
import com.alojate.identidad.application.dto.CrearRolCommand;
import com.alojate.identidad.application.dto.ModificarRolCommand;
import com.alojate.identidad.application.dto.ModuloResponse;
import com.alojate.identidad.application.dto.PermisoResponse;
import com.alojate.identidad.application.dto.RolResponse;
import com.alojate.identidad.application.ports.input.GestionarRoles;
import com.alojate.identidad.application.ports.output.AuditoriaPort;
import com.alojate.identidad.application.ports.output.UnitOfWork;
import com.alojate.identidad.domain.exception.ReglaNegocioException;
import com.alojate.identidad.domain.modulo.aggregate.Modulo;
import com.alojate.identidad.domain.modulo.repository.ModuloRepository;
import com.alojate.identidad.domain.modulo.valueobject.PermisoId;
import com.alojate.identidad.domain.rol.aggregate.Rol;
import com.alojate.identidad.domain.rol.event.PermisosDeRolActualizados;
import com.alojate.identidad.domain.rol.repository.RolRepository;
import com.alojate.identidad.domain.rol.valueobject.DescripcionRol;
import com.alojate.identidad.domain.rol.valueobject.NombreRol;
import com.alojate.identidad.domain.rol.valueobject.RolId;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class GestionarRolesService implements GestionarRoles {

    private final ModuloRepository modulos;
    private final RolRepository roles;
    private final UnitOfWork unitOfWork;
    private final AuditoriaPort auditoria;

    public GestionarRolesService(
            ModuloRepository modulos,
            RolRepository roles,
            UnitOfWork unitOfWork,
            AuditoriaPort auditoria) {
        this.modulos = modulos;
        this.roles = roles;
        this.unitOfWork = unitOfWork;
        this.auditoria = auditoria;
    }

    @Override
    public List<ModuloResponse> listarModulos() {
        return modulos.listar().stream().map(this::aModulo).toList();
    }

    @Override
    public List<RolResponse> listarRoles() {
        return roles.listar().stream().map(this::aRol).toList();
    }

    @Override
    public RolResponse obtener(int id) {
        return aRol(roles.buscarPorId(new RolId(id)).orElseThrow(this::noEncontrado));
    }

    @Override
    public RolResponse crear(CrearRolCommand comando) {
        NombreRol nombre = new NombreRol(comando.nombre());
        DescripcionRol descripcion = new DescripcionRol(comando.descripcion());
        Set<PermisoId> permisos = aPermisos(comando.permisos());
        Rol guardado = unitOfWork.ejecutar(() -> {
            if (roles.existeOtroConNombre(nombre, null)) {
                throw duplicado();
            }
            exigirAsignables(permisos);
            return roles.guardar(Rol.crear(nombre, descripcion, permisos));
        });
        auditar(guardado);
        return aRol(guardado);
    }

    @Override
    public RolResponse modificar(ModificarRolCommand comando) {
        NombreRol nombre = new NombreRol(comando.nombre());
        DescripcionRol descripcion = new DescripcionRol(comando.descripcion());
        Set<PermisoId> permisos = aPermisos(comando.permisos());
        RolId id = new RolId(comando.id());
        Rol guardado = unitOfWork.ejecutar(() -> {
            Rol rol = roles.buscarPorId(id).orElseThrow(this::noEncontrado);
            if (roles.existeOtroConNombre(nombre, rol.id())) {
                throw duplicado();
            }
            exigirAsignables(permisos);
            rol.modificar(nombre, descripcion, permisos);
            return roles.guardar(rol);
        });
        auditar(guardado);
        return aRol(guardado);
    }

    @Override
    public RolResponse cambiarActivo(CambiarActivoRolCommand comando) {
        if (comando.activo() == null) {
            throw new ReglaNegocioException("DATO_INVALIDO", "El estado del rol es obligatorio");
        }
        RolId id = new RolId(comando.id());
        Rol guardado = unitOfWork.ejecutar(() -> {
            Rol rol = roles.buscarPorId(id).orElseThrow(this::noEncontrado);
            rol.cambiarActivo(comando.activo());
            return roles.guardar(rol);
        });
        return aRol(guardado);
    }

    private void exigirAsignables(Set<PermisoId> permisos) {
        if (modulos.buscarAsignables(permisos).size() != permisos.size()) {
            throw new ReglaNegocioException(
                    "PERMISO_NO_ASIGNABLE", "Hay permisos que no existen o no se pueden asignar");
        }
    }

    private void auditar(Rol rol) {
        rol.eventos().stream()
                .filter(PermisosDeRolActualizados.class::isInstance)
                .map(PermisosDeRolActualizados.class::cast)
                .forEach(auditoria::registrar);
    }

    private Set<PermisoId> aPermisos(List<Integer> permisos) {
        if (permisos == null) {
            return Set.of();
        }
        Set<PermisoId> ids = new HashSet<>();
        for (Integer permiso : permisos) {
            if (permiso == null) {
                throw new ReglaNegocioException("DATO_INVALIDO", "El permiso es obligatorio");
            }
            ids.add(new PermisoId(permiso));
        }
        return Set.copyOf(ids);
    }

    private ModuloResponse aModulo(Modulo modulo) {
        List<PermisoResponse> permisos = modulo.permisos().stream()
                .map(permiso -> new PermisoResponse(
                        permiso.id().valor(),
                        permiso.codigo().valor(),
                        permiso.nombre(),
                        permiso.descripcion(),
                        permiso.asignable()))
                .toList();
        return new ModuloResponse(
                modulo.id().valor(), modulo.nombre(), modulo.descripcion(), modulo.orden(), permisos);
    }

    private RolResponse aRol(Rol rol) {
        List<Integer> permisos = rol.permisos().stream().map(PermisoId::valor).sorted().toList();
        return new RolResponse(
                rol.id().valor(),
                rol.nombre().valor(),
                rol.descripcion().valor(),
                rol.ambito().name(),
                rol.esSistema(),
                rol.activo(),
                permisos);
    }

    private ReglaNegocioException duplicado() {
        return new ReglaNegocioException("ROL_DUPLICADO", "Ya existe un rol con ese nombre");
    }

    private ReglaNegocioException noEncontrado() {
        return new ReglaNegocioException("ROL_NO_ENCONTRADO", "El rol no existe");
    }
}
