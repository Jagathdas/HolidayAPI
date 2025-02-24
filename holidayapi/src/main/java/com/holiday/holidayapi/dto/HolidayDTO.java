package com.holiday.holidayapi.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotEmpty;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
public class HolidayDTO {

	
	@NotEmpty(message = "Country name cannot be empty")
    @Size(min = 2, max = 50, message = "Country name must be between 2 and 50 characters")
	@Pattern(regexp = "^[a-zA-Z\\s]+$", message = "Country name must contain only letters and spaces")
    private String country;

	
	
	@NotEmpty(message = "Holiday name cannot be empty")
    @Size(min = 2, max = 100, message = "Holiday name must be between 2 and 100 characters")
	@Pattern(regexp = "^[a-zA-Z\\s]+$", message = "Holiday description must contain only letters and spaces")
    private String name;

    @NotNull(message = "Holiday date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
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
