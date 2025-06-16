package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Measurement {

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
