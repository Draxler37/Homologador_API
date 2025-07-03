package com.example.homologador_api.service;

import com.example.homologador_api.client.AIServiceClient;
import com.example.homologador_api.model.CarCatalog;
import com.example.homologador_api.repository.CarCatalogRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import java.io.InputStream;
import java.util.List;

/**
 * Este servicio se ejecuta una sola vez al arrancar la aplicación
 * para poblar la base de datos con los datos de los archivos JSON.
 * Es el responsable de "vectorizar" los datos usando el servicio de IA.
 */
@Component
public class DataIngestionService implements CommandLineRunner {

    private final CarCatalogRepository repository;
    private final AIServiceClient aiServiceClient; // Inyectamos el cliente de IA

    public DataIngestionService(CarCatalogRepository repository, AIServiceClient aiServiceClient) {
        this.repository = repository;
        this.aiServiceClient = aiServiceClient;
    }

    @Override
    public void run(String... args) throws Exception {
        // Esta comprobación es CRUCIAL. Evita que los datos se carguen cada vez que se
        // reinicia el servidor.
        if (repository.count() > 0) {
            System.out.println("La base de datos ya contiene datos. Saltando el proceso de ingestión.");
            return;
        }

        System.out.println("Base de datos vacía. Iniciando el proceso de ingestión de datos...");

        // Lista de los archivos JSON a procesar
        String[] dataFiles = {
                "data/hdi-data.json",
                "data/mapfre-data.json"
        };

        for (String filePath : dataFiles) {
            loadDataForInsurer(filePath);
        }

        System.out.println("¡Ingestión de datos completada! La base de datos está lista.");
    }

    private void loadDataForInsurer(String jsonPath) {
        ObjectMapper mapper = new ObjectMapper();

        try (InputStream inputStream = new ClassPathResource(jsonPath).getInputStream()) {
            // Lee el archivo JSON y lo convierte en una lista de objetos CarCatalog
            List<CarCatalog> cars = mapper.readValue(inputStream, new TypeReference<List<CarCatalog>>() {
            });

            System.out.println("Procesando " + cars.size() + " registros desde " + jsonPath + "...");

            for (CarCatalog car : cars) {
                // Genera la descripción unificada que se usará para la vectorización
                car.generateUnifiedDescription();

                // Llama al servicio de IA a través de nuestro cliente para obtener el vector
                float[] vector = aiServiceClient.getEmbedding(car.getUnifiedDescription());

                // Asigna el vector obtenido al objeto antes de guardarlo
                car.setVectorEmbedding(vector);

                // Guarda el objeto completo (con datos y vector) en la base de datos
                repository.save(car);
            }
        } catch (Exception e) {
            System.err.println("Error al cargar o procesar el archivo " + jsonPath + ": " + e.getMessage());
            // En un sistema de producción, podrías querer detener el proceso o registrar el
            // error de forma más formal.
        }
    }
}