package com.example.homologador_api.model;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.Transient;
import jakarta.persistence.PostLoad;
import lombok.Data;

@Data
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
public class CarCatalog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long internalId;

    private int insuranceCompanyId;

    @Column(length = 1024) // Columna para almacenar el vector
    private float[] vectorEmbedding;

    // Campo transitorio que no se guarda en la BD, se calcula al momento.
    @Transient
    private String unifiedDescription;

    // --- Campos de datos originales ---
    private String originalMake;
    private String originalSubmake;
    private String originalModelString;
    private int year;
    private String originalTypeId; // Para casos como HDI

    // Mapeo de objetos anidados con Jackson
    @JsonProperty("make")
    private void unpackMake(Map<String, String> make) {
        this.originalMake = make.get("makeString");
        this.originalSubmake = make.get("submake");
    }

    @JsonProperty("model")
    private void unpackModel(Map<String, String> model) {
        this.originalModelString = model.get("modelString");
    }

    // Método para generar la descripción unificada después de cargar el objeto
    @PostLoad
    public void generateUnifiedDescription() {
        // Lógica para construir la descripción unificada a partir de los campos
        // originales
        StringBuilder sb = new StringBuilder();
        if (originalMake != null && !originalMake.isEmpty())
            sb.append(originalMake).append(" ");
        if (originalSubmake != null && !originalSubmake.isEmpty())
            sb.append(originalSubmake).append(" ");
        if (originalModelString != null && !originalModelString.isEmpty())
            sb.append(originalModelString).append(" ");
        if (year > 0)
            sb.append(year).append(" ");
        if (originalTypeId != null && !originalTypeId.isEmpty())
            sb.append(originalTypeId).append(" ");
        this.unifiedDescription = sb.toString().trim();
    }

    public String getInsuranceCompanyIdAsString() {
        return switch (this.insuranceCompanyId) {
            case 5 -> "chubb";
            case 27 -> "crabi";
            case 4 -> "gnp";
            case 34 -> "hdi";
            case 39 -> "mapfre";
            default -> "desconocida";
        };
    }
}