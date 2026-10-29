package com.alojate.identidad.domain.usuario.repository;

import com.alojate.identidad.domain.usuario.aggregate.Usuario;
import com.alojate.identidad.domain.usuario.valueobject.Email;
import com.alojate.identidad.domain.usuario.valueobject.UsuarioId;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository {

    void guardar(Usuario usuario);

    List<Usuario> listarCorporativos();

    List<Usuario> listarPublicos();

    Optional<Usuario> buscarPorCorreoPublico(Email email);

    Optional<Usuario> buscarPorCorreoCorporativo(Email email);

    Optional<Usuario> buscarPorId(UsuarioId id);

    boolean correoOcupado(Email email, UsuarioId excepto);
}
