package com.alojate.identidad.domain.usuario.valueobject;

import com.alojate.identidad.domain.exception.ReglaNegocioException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;

public record CodigoVerificacion(String codigo, Instant vence) {

    public static final Duration VIGENCIA = Duration.ofHours(24);

    private static final SecureRandom ALEATORIO = new SecureRandom();

    public CodigoVerificacion {
        if (codigo == null || codigo.isBlank()) {
            throw new ReglaNegocioException("DATO_INVALIDO", "El código de verificación es obligatorio");
        }
        if (vence == null) {
            throw new ReglaNegocioException("DATO_INVALIDO", "El vencimiento del código es obligatorio");
        }
    }

    public static CodigoVerificacion generar(Instant ahora) {
        int numero = ALEATORIO.nextInt(1_000_000);
        return new CodigoVerificacion("%06d".formatted(numero), ahora.plus(VIGENCIA));
    }

    public void exigirValido(String codigoInformado, Instant ahora) {
        if (ahora == null || !ahora.isBefore(vence)) {
            throw new ReglaNegocioException("CODIGO_VENCIDO", "El código de verificación venció");
        }
        if (codigoInformado == null || !coincide(codigoInformado)) {
            throw new ReglaNegocioException("CODIGO_INCORRECTO", "El código de verificación no es válido");
        }
    }

    private boolean coincide(String informado) {
        byte[] esperado = codigo.getBytes(StandardCharsets.UTF_8);
        byte[] recibido = informado.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(esperado, recibido);
    }
}
