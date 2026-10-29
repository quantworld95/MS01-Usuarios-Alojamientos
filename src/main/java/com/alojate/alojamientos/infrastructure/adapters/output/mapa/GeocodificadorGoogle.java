package com.alojate.alojamientos.infrastructure.adapters.output.mapa;

import com.alojate.alojamientos.application.ports.output.Geocodificador;
import com.alojate.alojamientos.application.ports.output.LugarGeocodificado;
import com.alojate.alojamientos.domain.exception.ReglaNegocioException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;

@Component
public class GeocodificadorGoogle implements Geocodificador {

    private final String apiKey;
    private final ObjectMapper json;
    private final RestClient http;

    public GeocodificadorGoogle(
            @Value("${alojate.mapas.api-key:}") String apiKey, ObjectMapper json) {
        this.apiKey = apiKey;
        this.json = json;
        this.http = RestClient.create();
    }

    @Override
    public LugarGeocodificado porDireccion(String direccion, String ciudad, String departamento) {
        String consulta = direccion;
        if (presente(ciudad)) {
            consulta = consulta + ", " + ciudad;
        }
        if (presente(departamento)) {
            consulta = consulta + ", " + departamento;
        }
        consulta = consulta + ", Bolivia";
        URI uri = UriComponentsBuilder
                .fromUriString("https://maps.googleapis.com/maps/api/geocode/json")
                .queryParam("address", consulta)
                .queryParam("components", "country:BO")
                .queryParam("language", "es")
                .queryParam("region", "bo")
                .queryParam("key", apiKey)
                .build()
                .encode()
                .toUri();
        return lugar(consultar(uri));
    }

    @Override
    public LugarGeocodificado porCoordenadas(BigDecimal latitud, BigDecimal longitud) {
        URI uri = UriComponentsBuilder
                .fromUriString("https://maps.googleapis.com/maps/api/geocode/json")
                .queryParam("latlng", latitud.toPlainString() + "," + longitud.toPlainString())
                .queryParam("language", "es")
                .queryParam("key", apiKey)
                .build()
                .encode()
                .toUri();
        return lugar(consultar(uri));
    }

    private JsonNode consultar(URI uri) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new ReglaNegocioException("MAPA_NO_DISPONIBLE", "El mapa no está configurado");
        }
        try {
            String cuerpo = http.get().uri(uri).retrieve().body(String.class);
            JsonNode respuesta = json.readTree(cuerpo == null ? "{}" : cuerpo);
            String estado = texto(respuesta, "status");
            if ("ZERO_RESULTS".equals(estado)) {
                throw new ReglaNegocioException("DATO_INVALIDO", "No se encontró esa ubicación en Bolivia");
            }
            if (!"OK".equals(estado) || respuesta.path("results").isEmpty()) {
                throw new ReglaNegocioException("MAPA_NO_DISPONIBLE", "No se pudo consultar el mapa");
            }
            return respuesta.path("results").get(0);
        } catch (RestClientException | java.io.IOException error) {
            throw new ReglaNegocioException("MAPA_NO_DISPONIBLE", "No se pudo consultar el mapa");
        }
    }

    private LugarGeocodificado lugar(JsonNode resultado) {
        if (!"BO".equals(componente(resultado, "country", true))) {
            throw new ReglaNegocioException("DATO_INVALIDO", "La ubicación queda fuera de Bolivia");
        }
        JsonNode punto = resultado.path("geometry").path("location");
        if (punto.get("lat") == null || punto.get("lng") == null) {
            throw new ReglaNegocioException("MAPA_NO_DISPONIBLE", "No se pudo consultar el mapa");
        }
        return new LugarGeocodificado(
                direccion(resultado),
                ciudad(resultado),
                departamento(componente(resultado, "administrative_area_level_1", false)),
                punto.get("lat").decimalValue().setScale(6, RoundingMode.HALF_UP),
                punto.get("lng").decimalValue().setScale(6, RoundingMode.HALF_UP));
    }

    private String direccion(JsonNode resultado) {
        String ruta = componente(resultado, "route", false);
        String numero = componente(resultado, "street_number", false);
        String linea = ruta == null ? "" : ruta;
        if (numero != null) {
            linea = (linea + " " + numero).trim();
        }
        if (linea.length() < 5) {
            String formateada = texto(resultado, "formatted_address");
            if (formateada != null) {
                int coma = formateada.indexOf(',');
                linea = coma >= 5 ? formateada.substring(0, coma) : formateada;
            }
        }
        if (linea.length() > 200) {
            linea = linea.substring(0, 200).trim();
        }
        if (linea.isBlank()) {
            throw new ReglaNegocioException("DATO_INVALIDO", "No se encontró la dirección");
        }
        return linea;
    }

    private String ciudad(JsonNode resultado) {
        String ciudad = componente(resultado, "locality", false);
        if (ciudad == null) {
            ciudad = componente(resultado, "administrative_area_level_2", false);
        }
        if (ciudad == null) {
            ciudad = componente(resultado, "sublocality", false);
        }
        if (ciudad == null || ciudad.isBlank()) {
            throw new ReglaNegocioException("DATO_INVALIDO", "No se encontró la ciudad");
        }
        return ciudad;
    }

    private static String departamento(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new ReglaNegocioException("DATO_INVALIDO", "No se encontró el departamento");
        }
        return texto.replace("Departamento de ", "")
                .replace("Department", "")
                .replace("Departamento", "")
                .trim();
    }

    private static String componente(JsonNode resultado, String tipo, boolean corto) {
        for (JsonNode componente : resultado.path("address_components")) {
            for (JsonNode tipoComponente : componente.path("types")) {
                if (tipo.equals(tipoComponente.asText())) {
                    return texto(componente, corto ? "short_name" : "long_name");
                }
            }
        }
        return null;
    }

    private static String texto(JsonNode nodo, String campo) {
        JsonNode valor = nodo.get(campo);
        if (valor == null || valor.isNull()) {
            return null;
        }
        String texto = valor.asText();
        return texto == null || texto.isBlank() ? null : texto;
    }

    private static boolean presente(String valor) {
        return valor != null && !valor.isBlank();
    }
}
