package com.alojate.alojamientos.infrastructure.adapters.output.persistence.repository;

import com.alojate.alojamientos.application.ports.output.NombreAnfitrion;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class NombreAnfitrionJdbc implements NombreAnfitrion {

    private final JdbcTemplate jdbc;

    public NombreAnfitrionJdbc(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Map<UUID, String> de(Collection<UUID> anfitriones) {
        if (anfitriones == null || anfitriones.isEmpty()) {
            return Map.of();
        }
        List<UUID> ids = anfitriones.stream().distinct().toList();
        String marcas = ids.stream().map(id -> "?").collect(Collectors.joining(","));
        Map<UUID, String> nombres = new HashMap<>();
        jdbc.query(
                "SELECT id_usuario, nombre, apellido FROM usuario WHERE id_usuario IN (" + marcas + ")",
                (fila, numero) -> nombres.put(
                        fila.getObject("id_usuario", UUID.class),
                        (fila.getString("nombre").trim() + " " + fila.getString("apellido").trim()).trim()),
                ids.toArray());
        return nombres;
    }
}
