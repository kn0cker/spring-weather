package com.example.demo.controller;

import com.example.demo.repository.MeasurementRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/measurements")
public class MeasurementController {

    private MeasurementRepository measurementRepository;

    public MeasurementController(MeasurementRepository measurementRepository) {
        this.measurementRepository = measurementRepository;
    }

    @GetMapping("/count")
    public String getMeasurementCount() {

        return "Total measurements: " + measurementRepository.count();
    }
}
