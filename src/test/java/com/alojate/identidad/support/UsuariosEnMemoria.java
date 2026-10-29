package com.alojate.identidad.support;

import com.alojate.identidad.domain.usuario.aggregate.Usuario;
import com.alojate.identidad.domain.usuario.repository.UsuarioRepository;
import com.alojate.identidad.domain.usuario.valueobject.Email;
import com.alojate.identidad.domain.usuario.valueobject.TipoCuenta;
import com.alojate.identidad.domain.usuario.valueobject.UsuarioId;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class UsuariosEnMemoria implements UsuarioRepository {

    private final Map<UUID, Usuario> porId = new HashMap<>();

    @Override
    public void guardar(Usuario usuario) {
        porId.put(usuario.id().valor(), usuario);
    }

    @Override
    public List<Usuario> listarCorporativos() {
        return porTipo(TipoCuenta.CORPORATIVA);
    }

    @Override
    public List<Usuario> listarPublicos() {
        return porTipo(TipoCuenta.PUBLICA);
    }

    @Override
    public Optional<Usuario> buscarPorCorreoPublico(Email email) {
        return buscar(email, TipoCuenta.PUBLICA);
    }

    @Override
    public Optional<Usuario> buscarPorCorreoCorporativo(Email email) {
        return buscar(email, TipoCuenta.CORPORATIVA);
    }

    @Override
    public Optional<Usuario> buscarPorId(UsuarioId id) {
        return Optional.ofNullable(porId.get(id.valor()));
    }

    @Override
    public boolean correoOcupado(Email email, UsuarioId excepto) {
        return porId.values().stream()
                .anyMatch(usuario -> usuario.email().valor().equals(email.valor())
                        && (excepto == null || !usuario.id().equals(excepto)));
    }

    public int cantidad() {
        return porId.size();
    }

    private List<Usuario> porTipo(TipoCuenta tipo) {
        return porId.values().stream().filter(usuario -> usuario.tipoCuenta() == tipo).toList();
    }

    private Optional<Usuario> buscar(Email email, TipoCuenta tipo) {
        return porId.values().stream()
                .filter(usuario -> usuario.tipoCuenta() == tipo && usuario.email().valor().equals(email.valor()))
                .findFirst();
    }
}
