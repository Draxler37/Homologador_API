package com.example.homologador_api.DTO;

import lombok.Data;

@Data
public class CarSearchRequest {
    private String marca;
    private String submarca;
    private int ano;
    private String modelo;
    private String version;
}