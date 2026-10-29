package com.alojate.alojamientos.infrastructure.adapters.output.persistence.repository;

import com.alojate.alojamientos.application.ports.output.AccesoHuesped;
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
public class AccesoHuespedJdbc implements AccesoHuesped {

    private static final String BUSCAR = "BUSCAR_ALOJAMIENTOS";

    private final JdbcTemplate jdbc;
    private final Reloj reloj;

    public AccesoHuespedJdbc(JdbcTemplate jdbc, Reloj reloj) {
        this.jdbc = jdbc;
        this.reloj = reloj;
    }

    @Override
    public void exigirPuedeBuscar(UUID huespedId) {
        if (huespedId == null) {
            throw new ReglaNegocioException("DATO_INVALIDO", "El huésped es obligatorio");
        }
        List<Cuenta> cuentas = jdbc.query(
                """
                SELECT u.tipo_cuenta, u.estado, r.nombre AS rol, r.ambito, r.activo, r.es_sistema
                FROM usuario u
                LEFT JOIN rol r ON r.id_rol = u.id_rol
                WHERE u.id_usuario = ?
                """,
                (fila, numero) -> new Cuenta(
                        fila.getString("tipo_cuenta"),
                        fila.getString("estado"),
                        fila.getString("rol"),
                        fila.getString("ambito"),
                        fila.getBoolean("activo"),
                        fila.getBoolean("es_sistema")),
                huespedId);
        if (cuentas.isEmpty()) {
            throw new ReglaNegocioException("CUENTA_NO_ENCONTRADA", "La cuenta no existe");
        }
        Cuenta cuenta = cuentas.getFirst();
        if (!"PUBLICA".equals(cuenta.tipo()) || !"ACTIVA".equals(cuenta.estado())
                || !"USUARIO_PUBLICO".equals(cuenta.rol())
                || !"PUBLICO".equals(cuenta.ambito())
                || !cuenta.rolActivo()
                || !cuenta.rolSistema()) {
            throw new ReglaNegocioException(
                    "PERMISO_DENEGADO", "La cuenta no puede buscar alojamientos");
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
                huespedId));
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
                huespedId,
                Timestamp.from(reloj.ahora()),
                Timestamp.from(reloj.ahora())));
        if (!permisos.contains(BUSCAR) || restringidos.contains(BUSCAR)) {
            throw new ReglaNegocioException(
                    "PERMISO_DENEGADO", "La cuenta no puede buscar alojamientos");
        }
    }

    private record Cuenta(
            String tipo, String estado, String rol, String ambito, boolean rolActivo, boolean rolSistema) {
    }
}
