package com.holiday.holidayapi.service;

import com.holiday.holidayapi.model.Holiday;
import com.holiday.holidayapi.repository.HolidayRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class HolidayService {

    @Autowired
    private HolidayRepository holidayRepository;

    public List<Holiday> getHolidays(String country) {
    	 List<Holiday> holidays = holidayRepository.findByCountry(country);
        // If no holidays found for the country, throw an exception
        if (holidays.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No holidays found for country: " + country);
        }

        return holidays;
    }

    public Holiday addHoliday(String country, String name, LocalDate date) {
    	// Check if a holiday with the same country, name, and date already exists
        Optional<Holiday> existingHoliday = holidayRepository.findByCountryAndNameAndDate(country, name, date);
        if (existingHoliday.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Holiday with this name and date already exists for country: " + country);
        }
        
        Holiday holiday = new Holiday(country, name, date);
        return holidayRepository.save(holiday);
        
       
    }

    public Holiday updateHoliday(Long id, String country, String name, LocalDate date) {
        Holiday holiday = holidayRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Holiday not found with ID: " + id));
        holiday.setCountry(country);
        holiday.setName(name);
        holiday.setDate(date);
        return holidayRepository.save(holiday);
    }

    public void deleteHoliday(Long id) {
    	
    	// Check if holiday exists before deleting
        if (!holidayRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Holiday not found with ID: " + id);
        }
        holidayRepository.deleteById(id);
        
    }

 // Find holiday by country, name, and date
    public Holiday findByCountryAndNameAndDate(String country, String name, LocalDate date) {
        return holidayRepository.findByCountryAndNameAndDate(country, name, date)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Holiday not found for country: " + country + ", name: " + name + ", and date: " + date));
    }
 


}


