package com.alojate.identidad.infrastructure.adapters.output.persistence.repository;

import com.alojate.identidad.domain.exception.ReglaNegocioException;
import com.alojate.identidad.domain.modulo.valueobject.PermisoId;
import com.alojate.identidad.domain.rol.aggregate.Rol;
import com.alojate.identidad.domain.rol.repository.RolRepository;
import com.alojate.identidad.domain.rol.valueobject.AmbitoRol;
import com.alojate.identidad.domain.rol.valueobject.NombreRol;
import com.alojate.identidad.domain.rol.valueobject.RolId;
import com.alojate.identidad.infrastructure.adapters.output.persistence.entity.RolJpa;
import com.alojate.identidad.infrastructure.adapters.output.persistence.entity.RolPermisoJpa;
import com.alojate.identidad.infrastructure.adapters.output.persistence.jpa.PermisoJpaRepository;
import com.alojate.identidad.infrastructure.adapters.output.persistence.jpa.RolJpaRepository;
import com.alojate.identidad.infrastructure.adapters.output.persistence.mapper.RolMapper;
import com.alojate.identidad.infrastructure.adapters.output.persistence.mapper.UsuarioMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class PostgresRolRepository implements RolRepository {

    private final RolJpaRepository roles;
    private final PermisoJpaRepository permisos;
    private final UsuarioMapper usuarios;
    private final RolMapper mapper = new RolMapper();

    public PostgresRolRepository(
            RolJpaRepository roles, PermisoJpaRepository permisos, UsuarioMapper usuarios) {
        this.roles = roles;
        this.permisos = permisos;
        this.usuarios = usuarios;
    }

    @Override
    public Optional<Rol> buscarUsuarioPublico() {
        return roles.findByNombreAndAmbitoAndEsSistemaTrueAndActivoTrue(
                        NombreRol.USUARIO_PUBLICO, AmbitoRol.PUBLICO.name())
                .map(usuarios::aDominio);
    }

    @Override
    public List<Rol> listar() {
        return roles.listarConPermisos().stream().map(mapper::aDominio).toList();
    }

    @Override
    public Optional<Rol> buscarPorId(RolId id) {
        return roles.buscarConPermisos(id.valor()).map(mapper::aDominio);
    }

    @Override
    public boolean existeOtroConNombre(NombreRol nombre, RolId excepto) {
        if (excepto == null) {
            return roles.existsByNombreIgnoreCase(nombre.valor());
        }
        return roles.contarOtroNombre(nombre.valor(), excepto.valor()) > 0;
    }

    @Override
    public Rol guardar(Rol rol) {
        if (rol.id() == null) {
            RolJpa jpa = new RolJpa();
            mapper.copiar(rol, jpa);
            vincular(jpa, rol.permisos());
            jpa = roles.saveAndFlush(jpa);
            return rol.identificado(jpa.getIdRol());
        }
        RolJpa jpa = roles.buscarConPermisos(rol.id().valor()).orElseThrow(this::noEncontrado);
        mapper.copiar(rol, jpa);
        sincronizar(jpa, rol.permisos());
        roles.saveAndFlush(jpa);
        return rol;
    }

    private void vincular(RolJpa jpa, Set<PermisoId> permisosDelRol) {
        for (PermisoId permiso : permisosDelRol) {
            jpa.getPermisos().add(fila(jpa, permiso));
        }
    }

    private void sincronizar(RolJpa jpa, Set<PermisoId> permisosDelRol) {
        Set<Integer> deseados = permisosDelRol.stream().map(PermisoId::valor).collect(Collectors.toSet());
        jpa.getPermisos().removeIf(fila -> !deseados.contains(fila.getId().getIdPermiso()));
        Set<Integer> actuales = jpa.getPermisos().stream()
                .map(fila -> fila.getId().getIdPermiso())
                .collect(Collectors.toSet());
        for (PermisoId permiso : permisosDelRol) {
            if (!actuales.contains(permiso.valor())) {
                jpa.getPermisos().add(fila(jpa, permiso));
            }
        }
    }

    private RolPermisoJpa fila(RolJpa rol, PermisoId permiso) {
        RolPermisoJpa fila = new RolPermisoJpa();
        fila.setRol(rol);
        fila.setPermiso(permisos.getReferenceById(permiso.valor()));
        return fila;
    }

    private ReglaNegocioException noEncontrado() {
        return new ReglaNegocioException("ROL_NO_ENCONTRADO", "El rol no existe");
    }
}
