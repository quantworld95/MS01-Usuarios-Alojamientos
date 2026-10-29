package com.alojate.alojamientos.infrastructure.adapters.output.persistence.repository;

import com.alojate.alojamientos.application.ports.output.AccesoAdministrador;
import com.alojate.alojamientos.application.ports.output.Reloj;
import com.alojate.alojamientos.domain.exception.ReglaNegocioException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
public class AccesoAdministradorJdbc implements AccesoAdministrador {

    private static final String MODERAR = "MODERAR_ALOJAMIENTOS";

    private final JdbcTemplate jdbc;
    private final Reloj reloj;

    public AccesoAdministradorJdbc(JdbcTemplate jdbc, Reloj reloj) {
        this.jdbc = jdbc;
        this.reloj = reloj;
    }

    @Override
    public void exigirPuedeModerar(UUID administradorId) {
        if (administradorId == null) {
            throw new ReglaNegocioException("DATO_INVALIDO", "El administrador es obligatorio");
        }
        List<Cuenta> cuentas = jdbc.query(
                """
                SELECT u.tipo_cuenta, u.estado, r.ambito, r.activo
                FROM usuario u
                LEFT JOIN rol r ON r.id_rol = u.id_rol
                WHERE u.id_usuario = ?
                """,
                (fila, numero) -> new Cuenta(
                        fila.getString("tipo_cuenta"),
                        fila.getString("estado"),
                        fila.getString("ambito"),
                        fila.getBoolean("activo")),
                administradorId);
        if (cuentas.isEmpty()) {
            throw new ReglaNegocioException("CUENTA_NO_ENCONTRADA", "La cuenta no existe");
        }
        Cuenta cuenta = cuentas.getFirst();
        if (!"CORPORATIVA".equals(cuenta.tipo()) || !"ACTIVA".equals(cuenta.estado())
                || !"CORPORATIVO".equals(cuenta.ambito())
                || !cuenta.rolActivo()) {
            throw new ReglaNegocioException(
                    "PERMISO_DENEGADO", "La cuenta no puede moderar alojamientos");
        }
        Set<String> permisos = new HashSet<>(jdbc.query(
                """
                SELECT p.codigo
                FROM usuario u
                JOIN rol_permiso rp ON rp.id_rol = u.id_rol
                JOIN permiso p ON p.id_permiso = rp.id_permiso
                WHERE u.id_usuario = ?
                """,
                (fila, numero) -> fila.getString("codigo"),
                administradorId));
        Set<String> restringidos = new HashSet<>(jdbc.query(
                """
                SELECT p.codigo
                FROM restriccion_usuario ru
                JOIN permiso p ON p.id_permiso = ru.id_permiso
                WHERE ru.id_usuario = ?
                  AND ru.activa = TRUE
                  AND ru.fecha_inicio <= ?
                  AND (ru.fecha_fin IS NULL OR ru.fecha_fin > ?)
                """,
                (fila, numero) -> fila.getString("codigo"),
                administradorId,
                Timestamp.from(reloj.ahora()),
                Timestamp.from(reloj.ahora())));
        if (!permisos.contains(MODERAR) || restringidos.contains(MODERAR)) {
            throw new ReglaNegocioException(
                    "PERMISO_DENEGADO", "La cuenta no puede moderar alojamientos");
        }
    }

    private record Cuenta(String tipo, String estado, String ambito, boolean rolActivo) {
    }
}
