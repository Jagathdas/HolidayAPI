package com.holiday.holidayapi.model;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import lombok.Data;
import lombok.Setter;
import lombok.Getter;

@Data
@Setter
@Getter
@Entity
@Table(name="holidays")
public class Holiday {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Country cannot be null")
    @Size(min = 2, max = 50, message = "Country name must be between 2 and 50 characters")
    @Column(nullable = false)
    private String country;

    @NotNull(message = "Holiday name cannot be null")
    @Size(min = 2, max = 100, message = "Holiday name must be between 2 and 100 characters")
    @Column(nullable = false)
    private String name;

    @NotNull(message = "Date cannot be null")
    private LocalDate date;

    // Constructors, getters and setters

    public Holiday() {
    }

    public Holiday(String country, String name, LocalDate date) {
        this.country = country;
        this.name = name;
        this.date = date;
    }

    // Getters and Setters
}

