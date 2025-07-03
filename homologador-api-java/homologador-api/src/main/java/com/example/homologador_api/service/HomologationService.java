package com.example.homologador_api.service;

import com.example.homologador_api.model.CarCatalog;
import com.example.homologador_api.DTO.CarSearchRequest;
import com.example.homologador_api.client.AIServiceClient;
import com.example.homologador_api.repository.CarCatalogRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class HomologationService {

    private final CarCatalogRepository carRepository;
    private final AIServiceClient aiServiceClient;

    // Inyección de dependencias a través del constructor
    public HomologationService(CarCatalogRepository carRepository, AIServiceClient aiServiceClient) {
        this.carRepository = carRepository;
        this.aiServiceClient = aiServiceClient;
    }

    /**
     * Orquesta el proceso de encontrar vehículos similares.
     * 
     * @param request La petición del usuario con los datos del vehículo.
     * @return Un mapa agrupado por aseguradora con las 10 mejores coincidencias.
     */
    public Map<String, List<String>> findAndGroupSimilar(CarSearchRequest request) {
        // 1. Construir la descripción de búsqueda del usuario.
        System.out.println("Construyendo la descripción de búsqueda del usuario.");
        String userQuery = String.format("%s %s %d %s",
                request.getMarca(), request.getSubmarca(), request.getAno(), request.getVersion());

        // 2. Llamar al servicio de IA para obtener el vector de la búsqueda del
        // usuario.
        System.out.println("Llamando al servicio de IA para obtener el vector de la búsqueda del");
        float[] queryVector = aiServiceClient.getEmbedding(userQuery);
        System.out.println("Llamado Completado");

        // 3. Obtener los candidatos de la BD.
        // ¡Optimización clave! Filtramos por marca, submarca y año.
        System.out.println("Obteniedo candidatos filtrados");
        List<CarCatalog> candidates = carRepository.findByOriginalMakeIgnoreCaseAndOriginalSubmakeIgnoreCaseAndYear(
                request.getMarca(), request.getSubmarca(), request.getAno());

        // 4. Calcular la similitud del coseno para cada candidato y usar un Stream para
        // procesar.
        System.out.println("Calculando la similitudes");
        return candidates.stream()
                // Mapeamos cada auto candidato a un objeto simple que contiene el auto y su
                // similitud.
                .map(car -> {
                    double similarity = calculateCosineSimilarity(queryVector, car.getVectorEmbedding());
                    // Puedes definir un umbral mínimo de similitud para filtrar resultados poco
                    // relevantes
                    // if (similarity < 0.6) { return null; }

                    // Imprime la similitud y la descripción unificada/original
                    System.out.printf(
                            "Aseguradora: %s | Descripción: %s | Descripción Unificada: %s | Similitud: %.4f%n",
                            car.getInsuranceCompanyIdAsString(),
                            car.getOriginalModelString(),
                            car.getUnifiedDescription(),
                            similarity);
                    return new SimilarityResult(car, similarity);
                })
                // .filter(Objects::nonNull) // Si usas un umbral, descomenta esto
                // 5. Ordenar los resultados por similitud de mayor a menor.
                .sorted((r1, r2) -> Double.compare(r2.getSimilarity(), r1.getSimilarity()))
                // 6. Agrupar los resultados por el nombre de la aseguradora.
                .collect(Collectors.groupingBy(
                        // La clave del grupo es el ID (nombre) de la aseguradora
                        result -> result.getCar().getInsuranceCompanyIdAsString(),
                        // Para cada grupo (aseguradora), creamos una lista con las descripciones
                        // originales
                        Collectors.mapping(
                                result -> result.getCar().getOriginalModelString(),
                                // 7. Limitar a los 10 mejores resultados por cada aseguradora.
                                Collectors.collectingAndThen(Collectors.toList(),
                                        list -> list.stream().limit(10).collect(Collectors.toList())))));
    }

    /**
     * Calcula la similitud del coseno entre dos vectores.
     * El resultado es un valor entre -1 y 1 (o 0 y 1 para embeddings), donde 1 es
     * máxima similitud.
     */
    private double calculateCosineSimilarity(float[] vectorA, float[] vectorB) {
        if (vectorA == null || vectorB == null || vectorA.length != vectorB.length) {
            return 0.0; // O lanzar una excepción
        }
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            normA += Math.pow(vectorA[i], 2);
            normB += Math.pow(vectorB[i], 2);
        }
        if (normA == 0 || normB == 0) {
            return 0.0;
        }
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    // Clase auxiliar interna para facilitar el manejo de los resultados con
    // streams.
    private static class SimilarityResult {
        private final CarCatalog car;
        private final double similarity;

        public SimilarityResult(CarCatalog car, double similarity) {
            this.car = car;
            this.similarity = similarity;
        }

        public CarCatalog getCar() {
            return car;
        }

        public double getSimilarity() {
            return similarity;
        }
    }
}