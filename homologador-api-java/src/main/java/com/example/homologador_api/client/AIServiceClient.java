package com.example.homologador_api.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class AIServiceClient {

    private final RestTemplate restTemplate;

    // Inyectamos la URL del servicio de IA desde application.properties
    @Value("${ai.service.url}")
    private String aiServiceUrl;

    public AIServiceClient() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Llama al endpoint /embed del servicio de IA para obtener el vector de un
     * texto.
     * 
     * @param text El texto a convertir en vector.
     * @return Un array de floats que representa el vector semántico.
     */
    public float[] getEmbedding(String text) {
        // Construimos la URL completa del endpoint
        String url = aiServiceUrl + "/embed";

        // Creamos el cuerpo de la petición que espera el servicio de Python
        Map<String, String> requestPayload = Map.of("text", text);

        try {
            // Hacemos la llamada POST y esperamos un Map como respuesta
            Map<String, Object> response = restTemplate.postForObject(url, requestPayload, Map.class);

            // El JSON de respuesta tiene un campo "vector" que es una lista de números.
            // Jackson los deserializa como Double, así que necesitamos convertirlos a
            // float.
            List<Double> vectorAsDouble = (List<Double>) Objects.requireNonNull(response).get("vector");

            float[] vectorAsFloat = new float[vectorAsDouble.size()];
            for (int i = 0; i < vectorAsDouble.size(); i++) {
                vectorAsFloat[i] = vectorAsDouble.get(i).floatValue();
            }
            return vectorAsFloat;

        } catch (Exception e) {
            // En un caso real, aquí manejaríamos el error de forma más robusta
            // (e.g., lanzar una excepción customizada, reintentar, etc.)
            System.err.println("Error al llamar al servicio de IA: " + e.getMessage());
            throw new RuntimeException("No se pudo obtener el embedding del servicio de IA.", e);
        }
    }
}