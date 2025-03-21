package com.holiday.holidayapi.model;

import java.time.LocalDate;

public class GetHolidayListResponse {
    private String holidayName;
    private LocalDate holidayDate;

    // Constructor, getters, setters

    public GetHolidayListResponse(String holidayName, LocalDate holidayDate) {
        this.holidayName = holidayName;
        this.holidayDate = holidayDate;
    }

    public String getHolidayName() {
        return holidayName;
    }

    public void setHolidayName(String holidayName) {
        this.holidayName = holidayName;
    }

    public LocalDate getHolidayDate() {
        return holidayDate;
    }

    public void setHolidayDate(LocalDate holidayDate) {
        this.holidayDate = holidayDate;
    }
}

