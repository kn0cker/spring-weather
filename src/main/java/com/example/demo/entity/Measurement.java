package com.example.demo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Measurement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public String id;
}
