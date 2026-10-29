package com.alojate.alojamientos.infrastructure.adapters.output.imagen;

import com.alojate.alojamientos.application.ports.output.AlmacenDeImagenes;
import com.alojate.alojamientos.application.ports.output.ContenidoImagen;
import com.alojate.alojamientos.application.ports.output.ImagenAlmacenada;
import com.alojate.alojamientos.domain.exception.ReglaNegocioException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class AlmacenDeImagenesCloudinary implements AlmacenDeImagenes {

    private static final String CARPETA = "alojate";

    private final String nube;
    private final String apiKey;
    private final String apiSecret;
    private final ObjectMapper json;
    private final RestClient http;

    public AlmacenDeImagenesCloudinary(
            @Value("${alojate.imagenes.cloud-name:}") String nube,
            @Value("${alojate.imagenes.api-key:}") String apiKey,
            @Value("${alojate.imagenes.api-secret:}") String apiSecret,
            ObjectMapper json) {
        this.nube = nube;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.json = json;
        this.http = RestClient.create();
    }

    @Override
    public ImagenAlmacenada publicar(ContenidoImagen contenido) {
        exigirConfigurado();
        String marca = String.valueOf(Instant.now().getEpochSecond());
        String firma = firmar(Map.of("folder", CARPETA, "timestamp", marca));
        // Data URI evita fallos I/O del multipart con RestClient/JDK HttpClient.
        MultiValueMap<String, String> cuerpo = new LinkedMultiValueMap<>();
        cuerpo.add("file", dataUri(contenido));
        cuerpo.add("api_key", apiKey);
        cuerpo.add("timestamp", marca);
        cuerpo.add("folder", CARPETA);
        cuerpo.add("signature", firma);
        JsonNode respuesta = enviar("/image/upload", cuerpo);
        String identificador = texto(respuesta, "public_id");
        String url = texto(respuesta, "secure_url");
        if (identificador == null || url == null) {
            throw new ReglaNegocioException("ALMACEN_NO_DISPONIBLE", "No se pudo guardar la imagen");
        }
        return new ImagenAlmacenada(identificador, url);
    }

    @Override
    public void retirar(String identificadorAlmacen) {
        if (identificadorAlmacen == null || identificadorAlmacen.isBlank()) {
            throw new ReglaNegocioException("DATO_INVALIDO", "La fotografía no tiene archivo");
        }
        exigirConfigurado();
        String marca = String.valueOf(Instant.now().getEpochSecond());
        String firma = firmar(Map.of("public_id", identificadorAlmacen, "timestamp", marca));
        MultiValueMap<String, String> cuerpo = new LinkedMultiValueMap<>();
        cuerpo.add("public_id", identificadorAlmacen);
        cuerpo.add("api_key", apiKey);
        cuerpo.add("timestamp", marca);
        cuerpo.add("signature", firma);
        JsonNode respuesta = enviar("/image/destroy", cuerpo);
        String resultado = texto(respuesta, "result");
        if (!"ok".equals(resultado) && !"not found".equals(resultado)) {
            throw new ReglaNegocioException("ALMACEN_NO_DISPONIBLE", "No se pudo retirar la imagen");
        }
    }

    private JsonNode enviar(String ruta, MultiValueMap<String, String> cuerpo) {
        try {
            String texto = http.post()
                    .uri("https://api.cloudinary.com/v1_1/{nube}" + ruta, nube)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(cuerpo)
                    .retrieve()
                    .body(String.class);
            JsonNode respuesta = json.readTree(texto == null ? "{}" : texto);
            if (respuesta.hasNonNull("error")) {
                String detalle = respuesta.path("error").path("message").asText("error de Cloudinary");
                throw new ReglaNegocioException("ALMACEN_NO_DISPONIBLE", "Cloudinary: " + detalle);
            }
            return respuesta;
        } catch (ReglaNegocioException error) {
            throw error;
        } catch (RestClientException | java.io.IOException error) {
            throw new ReglaNegocioException(
                    "ALMACEN_NO_DISPONIBLE",
                    "No se pudo comunicar con Cloudinary (" + detalleError(error) + ")");
        }
    }

    private static String dataUri(ContenidoImagen contenido) {
        String tipo = contenido.tipoContenido() == null || contenido.tipoContenido().isBlank()
                ? "image/jpeg"
                : contenido.tipoContenido();
        return "data:" + tipo + ";base64," + Base64.getEncoder().encodeToString(contenido.contenido());
    }

    private static String detalleError(Throwable error) {
        StringBuilder texto = new StringBuilder();
        Throwable actual = error;
        while (actual != null) {
            String mensaje = actual.getMessage();
            if (mensaje != null && !mensaje.isBlank()) {
                if (!texto.isEmpty()) {
                    texto.append(" | ");
                }
                texto.append(actual.getClass().getSimpleName()).append(": ").append(mensaje);
            }
            actual = actual.getCause();
        }
        return texto.isEmpty() ? error.getClass().getSimpleName() : texto.toString();
    }

    private void exigirConfigurado() {
        if (nube.isBlank() || apiKey.isBlank() || apiSecret.isBlank()) {
            throw new ReglaNegocioException("ALMACEN_NO_DISPONIBLE", "El almacén de imágenes no está configurado");
        }
    }

    private String firmar(Map<String, String> parametros) {
        String base = parametros.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entrada -> entrada.getKey() + "=" + entrada.getValue())
                .collect(Collectors.joining("&"));
        return sha1(base + apiSecret);
    }

    private static String sha1(String valor) {
        try {
            byte[] bytes = MessageDigest.getInstance("SHA-1").digest(valor.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(bytes.length * 2);
            for (byte dato : bytes) {
                hex.append(String.format("%02x", dato));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException error) {
            throw new IllegalStateException(error);
        }
    }

    private static String texto(JsonNode nodo, String campo) {
        JsonNode valor = nodo.get(campo);
        if (valor == null || valor.isNull()) {
            return null;
        }
        return valor.asText();
    }
}
