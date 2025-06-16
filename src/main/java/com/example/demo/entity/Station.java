package com.example.demo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Station {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String stationId;
    private String name;
    private Double latitude;
    private Double longitude;
    private Double elevation;
    private LocalDateTime created;
    private LocalDateTime updated;
}
