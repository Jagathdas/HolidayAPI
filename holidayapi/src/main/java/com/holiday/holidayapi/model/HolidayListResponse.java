package com.holiday.holidayapi.model;

import java.util.List;

public class HolidayListResponse {

    private String countryName;
    private List<GetHolidayListResponse> holidays;

    // Constructor
    public HolidayListResponse(String countryName, List<GetHolidayListResponse> holidays) {
        this.countryName = countryName;
        this.holidays = holidays;
    }

    // Getters and Setters
    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public List<GetHolidayListResponse> getHolidays() {
        return holidays;
    }

    public void setHolidays(List<GetHolidayListResponse> holidays) {
        this.holidays = holidays;
    }
}
