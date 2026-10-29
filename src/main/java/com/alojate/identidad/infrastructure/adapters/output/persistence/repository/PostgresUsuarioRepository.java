package com.alojate.identidad.infrastructure.adapters.output.persistence.repository;

import com.alojate.identidad.domain.usuario.aggregate.Usuario;
import com.alojate.identidad.domain.usuario.repository.UsuarioRepository;
import com.alojate.identidad.domain.usuario.valueobject.Email;
import com.alojate.identidad.domain.usuario.valueobject.TipoCuenta;
import com.alojate.identidad.domain.usuario.valueobject.UsuarioId;
import com.alojate.identidad.infrastructure.adapters.output.persistence.entity.RolJpa;
import com.alojate.identidad.infrastructure.adapters.output.persistence.jpa.RolJpaRepository;
import com.alojate.identidad.infrastructure.adapters.output.persistence.jpa.UsuarioJpaRepository;
import com.alojate.identidad.infrastructure.adapters.output.persistence.mapper.UsuarioMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class PostgresUsuarioRepository implements UsuarioRepository {

    private final UsuarioJpaRepository jpa;
    private final RolJpaRepository roles;
    private final UsuarioMapper mapper;

    public PostgresUsuarioRepository(
            UsuarioJpaRepository jpa, RolJpaRepository roles, UsuarioMapper mapper) {
        this.jpa = jpa;
        this.roles = roles;
        this.mapper = mapper;
    }

    @Override
    public void guardar(Usuario usuario) {
        RolJpa rol = usuario.rolId() == null ? null : roles.getReferenceById(usuario.rolId().valor());
        jpa.save(mapper.aJpa(usuario, rol));
    }

    @Override
    public List<Usuario> listarCorporativos() {
        return jpa.listarCorporativos().stream().map(mapper::aDominio).toList();
    }

    @Override
    public List<Usuario> listarPublicos() {
        return jpa.listarPublicos().stream().map(mapper::aDominio).toList();
    }

    @Override
    public Optional<Usuario> buscarPorCorreoPublico(Email email) {
        return jpa.findByEmailAndTipoCuenta(email.valor(), TipoCuenta.PUBLICA.name())
                .map(mapper::aDominio);
    }

    @Override
    public Optional<Usuario> buscarPorCorreoCorporativo(Email email) {
        return jpa.findByEmailAndTipoCuenta(email.valor(), TipoCuenta.CORPORATIVA.name())
                .map(mapper::aDominio);
    }

    @Override
    public Optional<Usuario> buscarPorId(UsuarioId id) {
        return jpa.buscarPorId(id.valor()).map(mapper::aDominio);
    }

    @Override
    public boolean correoOcupado(Email email, UsuarioId excepto) {
        if (excepto == null) {
            return jpa.existsByEmail(email.valor());
        }
        return jpa.existsByEmailAndIdUsuarioNot(email.valor(), excepto.valor());
    }
}
