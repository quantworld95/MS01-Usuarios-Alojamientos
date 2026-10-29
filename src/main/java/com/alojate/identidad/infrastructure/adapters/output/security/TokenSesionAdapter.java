package com.alojate.identidad.infrastructure.adapters.output.security;

import com.alojate.identidad.application.ports.output.EmisorDeSesion;
import com.alojate.identidad.application.ports.output.InterpreteDeSesion;
import com.alojate.identidad.application.ports.output.SesionAutenticada;
import com.alojate.identidad.domain.exception.ReglaNegocioException;
import com.alojate.identidad.domain.usuario.valueobject.UsuarioId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Component
public class TokenSesionAdapter implements EmisorDeSesion, InterpreteDeSesion {

    private final byte[] secreto;

    public TokenSesionAdapter(@Value("${alojate.sesion.secreto}") String secreto) {
        this.secreto = secreto.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public String emitir(UsuarioId usuarioId, String contexto) {
        return emitir(usuarioId, contexto, List.of());
    }

    @Override
    public String emitir(UsuarioId usuarioId, String contexto, List<String> permisos) {
        String cuerpo = usuarioId.valor() + "|" + contexto + "|" + String.join(",", permisos == null ? List.of() : permisos);
        String cuerpoCodificado = codificar(cuerpo.getBytes(StandardCharsets.UTF_8));
        String firma = codificar(firmar(cuerpo));
        return cuerpoCodificado + "." + firma;
    }

    @Override
    public SesionAutenticada interpretar(String token) {
        if (token == null || token.isBlank()) {
            throw new ReglaNegocioException("SESION_INVALIDA", "La sesión no es válida");
        }
        String[] partes = token.split("\\.", 2);
        if (partes.length != 2) {
            throw new ReglaNegocioException("SESION_INVALIDA", "La sesión no es válida");
        }
        String cuerpo;
        try {
            cuerpo = new String(Base64.getUrlDecoder().decode(partes[0]), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException error) {
            throw new ReglaNegocioException("SESION_INVALIDA", "La sesión no es válida");
        }
        byte[] esperada = firmar(cuerpo);
        byte[] recibida;
        try {
            recibida = Base64.getUrlDecoder().decode(partes[1]);
        } catch (IllegalArgumentException error) {
            throw new ReglaNegocioException("SESION_INVALIDA", "La sesión no es válida");
        }
        if (!MessageDigest.isEqual(esperada, recibida)) {
            throw new ReglaNegocioException("SESION_INVALIDA", "La sesión no es válida");
        }
        String[] campos = cuerpo.split("\\|", 3);
        if (campos.length < 2) {
            throw new ReglaNegocioException("SESION_INVALIDA", "La sesión no es válida");
        }
        UUID usuarioId;
        try {
            usuarioId = UUID.fromString(campos[0]);
        } catch (IllegalArgumentException error) {
            throw new ReglaNegocioException("SESION_INVALIDA", "La sesión no es válida");
        }
        List<String> permisos = campos.length < 3 || campos[2].isBlank()
                ? List.of()
                : Arrays.stream(campos[2].split(",")).filter(p -> !p.isBlank()).toList();
        return new SesionAutenticada(usuarioId, campos[1], permisos);
    }

    private byte[] firmar(String cuerpo) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secreto, "HmacSHA256"));
            return mac.doFinal(cuerpo.getBytes(StandardCharsets.UTF_8));
        } catch (Exception excepcion) {
            throw new IllegalStateException("No se pudo firmar la sesión", excepcion);
        }
    }

    private String codificar(byte[] valor) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(valor);
    }
}
