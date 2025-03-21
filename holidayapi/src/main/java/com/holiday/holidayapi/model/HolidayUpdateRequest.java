package com.holiday.holidayapi.model;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Setter
@Getter
public class HolidayUpdateRequest {
    private String holidayName;
    private LocalDate holidayDate;
    @JsonIgnore
    private String dayOfWeek;

    
    // Set the day of the week based on the holidayDate
    public void setDayOfWeek() {
        if (holidayDate != null) {
            this.dayOfWeek = holidayDate.getDayOfWeek().toString(); // Sets the day of the week as a string (e.g., "MONDAY")
        }
    }
}

