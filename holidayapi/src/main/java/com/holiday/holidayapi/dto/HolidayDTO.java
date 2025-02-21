package com.holiday.holidayapi.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Future;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
public class HolidayDTO {

    @NotNull(message = "Country is required")
    @Size(min = 2, max = 50, message = "Country name must be between 2 and 50 characters")
    private String country;

    @NotNull(message = "Holiday name is required")
    @Size(min = 2, max = 100, message = "Holiday name must be between 2 and 100 characters")
    private String name;

    @NotNull(message = "Holiday date is required and Invalid date format Please use YYYY-MM-DD\"")
    @Future(message = "Holiday date cannot be in the past")
    private LocalDate date;

    // Constructors, getters and setters

    public HolidayDTO() {
    }

    public HolidayDTO(String country, String name, LocalDate date) {
        this.country = country;
        this.name = name;
        this.date = date;
    }

    // Getters and Setters
}
