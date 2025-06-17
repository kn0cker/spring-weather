package com.example.demo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Data
@Getter
@Setter
public class Station implements Serializable {

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


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Station)) return false;
        Station station = (Station) o;
        return stationId.equals(station.stationId);
    }

    @Override
    public int hashCode() {
        return stationId.hashCode();
    }
}
