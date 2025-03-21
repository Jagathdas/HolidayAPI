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
        if (countryDetails != null) {
        	 throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Country Code and Country Name already exist: " + country);
           
        }
     // Check if the country with the same country code already exists in the database
        Country countryCode = countryRepository.findByCountryCode(country.getCountryCode());
        
        if (countryCode != null) {
            // If a country with the same country code exists, do not add it to the database
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Country Code already exists for other Country Name.");
        }
        return countryRepository.save(country);
    
    }

    // Method to delete a country by countryCode
    public void deleteCountry(String countryCode) {
        Optional<Country> country = countryRepository.findById(countryCode);
        if (country.isPresent()) {
            countryRepository.delete(country.get());
        } else {
            throw new RuntimeException("Country Code not found");
        }
    }
}

