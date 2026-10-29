package com.alojate.config;

import com.alojate.identidad.application.ports.output.SesionAutenticada;
import com.alojate.identidad.domain.exception.ReglaNegocioException;

import java.util.UUID;

public final class SesionActual {

    private static final ThreadLocal<SesionAutenticada> ACTUAL = new ThreadLocal<>();

    private SesionActual() {
    }

    public static void establecer(SesionAutenticada sesion) {
        ACTUAL.set(sesion);
    }

    public static void limpiar() {
        ACTUAL.remove();
    }

    public static SesionAutenticada exigir() {
        SesionAutenticada sesion = ACTUAL.get();
        if (sesion == null) {
            throw new ReglaNegocioException("SESION_REQUERIDA", "Debes iniciar sesión");
        }
        return sesion;
    }

    public static UUID exigirUsuarioId() {
        return exigir().usuarioId();
    }

    public static void exigirPermiso(String codigo) {
        SesionAutenticada sesion = exigir();
        if (sesion.permisos() == null || !sesion.permisos().contains(codigo)) {
            throw new ReglaNegocioException(
                    "PERMISO_DENEGADO", "No tienes permiso para esta operación");
        }
    }
}
