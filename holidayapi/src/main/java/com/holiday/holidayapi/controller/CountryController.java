package com.holiday.holidayapi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.holiday.holidayapi.exception.ErrorMessage;
import com.holiday.holidayapi.model.Country;
import com.holiday.holidayapi.service.CountryService;

@RestController
@RequestMapping("/api/countries")
public class CountryController {

    @Autowired
    private CountryService countryService;

    // Add a new country
    @PostMapping("/add")
    public ResponseEntity<?> addCountry(@RequestBody Country country) {
        try {
            // Add the country via service
            Country savedCountry = countryService.addCountry(country);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedCountry);
        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(new ErrorMessage(ex.getReason()));
        }
    }

    // Delete a country by countryCode
    @DeleteMapping("/delete/{countryCode}")
    public ResponseEntity<String> deleteCountry(@PathVariable String countryCode) {
        try {
            // Delete the country via service
            countryService.deleteCountry(countryCode);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Country deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Country not found");
        }
    }
}

