package com.alojate.config;

import com.alojate.identidad.application.ports.output.InterpreteDeSesion;
import com.alojate.identidad.application.ports.output.SesionAutenticada;
import com.alojate.identidad.domain.exception.ReglaNegocioException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class SesionAutenticacionFilter extends OncePerRequestFilter {

    private final InterpreteDeSesion sesiones;
    private final ObjectMapper json;

    public SesionAutenticacionFilter(InterpreteDeSesion sesiones, ObjectMapper json) {
        this.sesiones = sesiones;
        this.json = json;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }
        return esRutaPublica(request.getRequestURI(), request.getMethod());
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        try {
            String encabezado = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (encabezado == null || !encabezado.startsWith("Bearer ")) {
                escribirError(response, HttpServletResponse.SC_UNAUTHORIZED, "SESION_REQUERIDA", "Debes iniciar sesión");
                return;
            }
            SesionAutenticada sesion = sesiones.interpretar(encabezado.substring("Bearer ".length()).trim());
            SesionActual.establecer(sesion);
            chain.doFilter(request, response);
        } catch (ReglaNegocioException error) {
            int estado = "SESION_INVALIDA".equals(error.codigo()) || "SESION_REQUERIDA".equals(error.codigo())
                    ? HttpServletResponse.SC_UNAUTHORIZED
                    : HttpServletResponse.SC_BAD_REQUEST;
            escribirError(response, estado, error.codigo(), error.getMessage());
        } finally {
            SesionActual.limpiar();
        }
    }

    private boolean esRutaPublica(String path, String method) {
        if ("/error".equals(path)) {
            return true;
        }
        if (path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs") || "/swagger-ui.html".equals(path)) {
            return true;
        }
        if (path.equals("/sesiones") || path.startsWith("/sesiones/")) {
            return true;
        }
        if (path.startsWith("/usuarios/verificacion")) {
            return true;
        }
        return "/usuarios".equals(path) && HttpMethod.POST.matches(method);
    }

    private void escribirError(HttpServletResponse response, int estado, String codigo, String mensaje)
            throws IOException {
        response.setStatus(estado);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        json.writeValue(response.getOutputStream(), Map.of("codigo", codigo, "mensaje", mensaje));
    }
}
