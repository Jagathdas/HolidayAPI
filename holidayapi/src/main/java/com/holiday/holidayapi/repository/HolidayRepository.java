package com.holiday.holidayapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.holiday.holidayapi.model.Holiday;
import com.holiday.holidayapi.model.HolidayUpdateRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface HolidayRepository extends JpaRepository<Holiday, Long> {
    List<Holiday> findByCountryName(String country);
    // Find holiday by ID and Country Code
    Holiday findByIdAndCountryCountryCode(Long id, String countryCode);

    // Find holiday by Country Code and Holiday Date
    Holiday findByCountryCountryCodeAndHolidayDate(String countryCode, LocalDate holidayDate);
    
    List<Holiday> findByCountryCountryCode(String countryCode); 
    
    Holiday findByCountryCountryCodeAndCountryNameAndHolidayNameAndHolidayDate(String countryCode, String countryName, String holidayName, LocalDate holidayDate);
}