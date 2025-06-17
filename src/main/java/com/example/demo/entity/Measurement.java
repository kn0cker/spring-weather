package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Data
public class Measurement implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @ManyToOne(fetch = FetchType.LAZY)
    private Station station;
    private LocalDateTime date;
    private Double precipitation;
    private Double snowfall;
    private Double snowDepth;
    private Double maxTemperature;
    private Double minTemperature;
    private LocalDateTime peakWindGust;
    private Double peakWindGustSpeed;
    private String peakWindGustDirection;
    private LocalDateTime created;
    private LocalDateTime updated;
}
