package com.holiday.holidayapi.service;

import com.holiday.holidayapi.model.Holiday;
import com.holiday.holidayapi.repository.HolidayRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class HolidayService {

    @Autowired
    private HolidayRepository holidayRepository;

    public List<Holiday> getHolidays(String country) {
        return holidayRepository.findByCountry(country);
        //exception handling if list zero
    }

    public Holiday addHoliday(String country, String name, LocalDate date) {
        Holiday holiday = new Holiday(country, name, date);
        return holidayRepository.save(holiday);
        
        //duplicate ck with name and date
    }

    public Holiday updateHoliday(Long id, String country, String name, LocalDate date) {
        Holiday holiday = holidayRepository.findById(id).orElseThrow(() -> new RuntimeException("Holiday not found"));
        holiday.setCountry(country);
        holiday.setName(name);
        holiday.setDate(date);
        return holidayRepository.save(holiday);
    }

    public void deleteHoliday(Long id) {
        holidayRepository.deleteById(id);
        
        // validation and error handling
    }

    public Holiday findByCountryAndNameAndDate(String country, String name, LocalDate date) {
    	
        return holidayRepository.findByCountryAndNameAndDate(country, name, date);
    }
    


}


