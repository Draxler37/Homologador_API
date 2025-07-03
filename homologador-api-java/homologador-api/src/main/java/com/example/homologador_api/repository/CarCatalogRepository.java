package com.example.homologador_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.homologador_api.model.CarCatalog;

import java.util.List;

public interface CarCatalogRepository extends JpaRepository<CarCatalog, Long> {
    List<CarCatalog> findByOriginalMakeIgnoreCaseAndOriginalSubmakeIgnoreCaseAndYear(
            String make, String submake, int year);
}
