package com.example.homologador_api.controller;

import com.example.homologador_api.DTO.CarSearchRequest;
import com.example.homologador_api.service.HomologationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/homologador") // Ruta base para este controlador
public class HomologationController {

    private final HomologationService homologationService;

    // Inyectamos el servicio a través del constructor
    public HomologationController(HomologationService homologationService) {
        this.homologationService = homologationService;
    }

    /**
     * Endpoint para buscar y homologar versiones de autos entre aseguradoras.
     * 
     * @param request El cuerpo de la petición JSON con los datos del auto a buscar.
     * @return Un JSON con los resultados agrupados por aseguradora.
     */
    @PostMapping("/buscar")
    public ResponseEntity<Map<String, List<String>>> findSimilarVehicles(@RequestBody CarSearchRequest request) {
        // Validaciones básicas de entrada
        if (request.getMarca() == null || request.getSubmarca() == null || request.getAno() == 0) {
            return ResponseEntity.badRequest().build(); // Devuelve un error 400 si faltan datos clave
        }

        // Llama al servicio para que haga el trabajo pesado
        Map<String, List<String>> results = homologationService.findAndGroupSimilar(request);

        // Devuelve los resultados con un código de estado 200 OK.
        // Spring se encarga de convertir el Map a un JSON automáticamente.
        return ResponseEntity.ok(results);
    }
}