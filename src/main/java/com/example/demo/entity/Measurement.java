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
    private Long precipitation;
    private Long snowfall;
    private Long snowDepth;
    private Long maxTemperature;
    private Long minTemperature;
    private LocalDateTime peakWindGust;
    private Long peakWindGustSpeed;
    private String peakWindGustDirection;
    private LocalDateTime created;
    private LocalDateTime updated;
}
