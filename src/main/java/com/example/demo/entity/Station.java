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
    private Long latitude;
    private Long longitude;
    private Long elevation;
    private LocalDateTime created;
    private LocalDateTime updated;
}
