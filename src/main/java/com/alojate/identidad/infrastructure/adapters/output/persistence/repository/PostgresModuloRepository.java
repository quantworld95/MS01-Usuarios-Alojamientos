package com.alojate.identidad.infrastructure.adapters.output.persistence.repository;

import com.alojate.identidad.domain.modulo.aggregate.Modulo;
import com.alojate.identidad.domain.modulo.aggregate.Permiso;
import com.alojate.identidad.domain.modulo.repository.ModuloRepository;
import com.alojate.identidad.domain.modulo.valueobject.CodigoPermiso;
import com.alojate.identidad.domain.modulo.valueobject.ModuloId;
import com.alojate.identidad.domain.modulo.valueobject.PermisoId;
import com.alojate.identidad.infrastructure.adapters.output.persistence.entity.ModuloJpa;
import com.alojate.identidad.infrastructure.adapters.output.persistence.entity.PermisoJpa;
import com.alojate.identidad.infrastructure.adapters.output.persistence.jpa.ModuloJpaRepository;
import com.alojate.identidad.infrastructure.adapters.output.persistence.jpa.PermisoJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class PostgresModuloRepository implements ModuloRepository {

    private final ModuloJpaRepository modulos;
    private final PermisoJpaRepository permisos;

    public PostgresModuloRepository(ModuloJpaRepository modulos, PermisoJpaRepository permisos) {
        this.modulos = modulos;
        this.permisos = permisos;
    }

    @Override
    public List<Modulo> listar() {
        return modulos.listarConPermisos().stream()
                .map(fila -> Modulo.reconstituir(
                        new ModuloId(fila.getIdModulo()),
                        fila.getNombre(),
                        fila.getDescripcion(),
                        fila.getOrden(),
                        fila.getPermisos().stream()
                                .sorted(Comparator.comparing(PermisoJpa::getCodigo))
                                .map(this::aPermiso)
                                .toList()))
                .toList();
    }

    @Override
    public Set<PermisoId> buscarAsignables(Set<PermisoId> solicitados) {
        if (solicitados.isEmpty()) {
            return Set.of();
        }
        List<Integer> ids = solicitados.stream().map(PermisoId::valor).toList();
        return permisos.findByAsignableTrueAndIdPermisoIn(ids).stream()
                .map(fila -> new PermisoId(fila.getIdPermiso()))
                .collect(Collectors.toSet());
    }

    private Permiso aPermiso(PermisoJpa fila) {
        return Permiso.reconstituir(
                new PermisoId(fila.getIdPermiso()),
                new CodigoPermiso(fila.getCodigo()),
                fila.getNombre(),
                fila.getDescripcion(),
                fila.isAsignable());
    }
}
