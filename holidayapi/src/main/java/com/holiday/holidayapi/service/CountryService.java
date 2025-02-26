package com.holiday.holidayapi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.holiday.holidayapi.model.Country;
import com.holiday.holidayapi.repository.CountryRepository;

import java.util.Optional;

@Service
public class CountryService {

    @Autowired
    private CountryRepository countryRepository;

    // Method to add a country
    public Country addCountry(Country country) {
        
    	// Check if the country exists
        Country countryDetails = countryRepository.findByCountryCodeAndCountryName(country.getCountryCode(), country.getCountryName());
        if (countryDetails == null) {
           
            return countryRepository.save(country);
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Country code does not exist: " + country);
    }

    // Method to delete a country by countryCode
    public void deleteCountry(String countryCode) {
        Optional<Country> country = countryRepository.findById(countryCode);
        if (country.isPresent()) {
            countryRepository.delete(country.get());
        } else {
            throw new RuntimeException("Country not found");
        }
    }
}

