package com.holiday.holidayapi.controller;

import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.holiday.holidayapi.exception.ErrorMessage;
import com.holiday.holidayapi.model.Country;
import com.holiday.holidayapi.service.CountryService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/countries")
public class CountryController {

    @Autowired
    private CountryService countryService;

    // Add a new country
    @PostMapping("/add")
    public ResponseEntity<?> addCountry(@RequestBody @Valid Country country) {
    	String countryName = country.getCountryName();
    	String countryCode =country.getCountryCode();
    	
    	 if (countryCode == null || countryCode.isEmpty() && countryName == null || countryName.isEmpty()) {
             return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                  .body(new ErrorMessage("Country Code and Country Name cannot be null or empty."));
         }
        
  	  if (countryCode == null || countryCode.isEmpty()) {
          return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                               .body(new ErrorMessage("Country Code cannot be null or empty."));
      }
 	  // Validate that Country code has at least 4 
      if (countryCode.length() < 4) {
          return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                               .body(new ErrorMessage("Country Code must have at least length 4 ."));
      } 
	  
	  if (!Pattern.matches("^[0-9]+$", countryCode)) {  // Regex to check if country code has only numeric
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
	                             .body(new ErrorMessage("Country Code should only contain numbers."));
	    }
    	// Check if country name is null or empty
        if (countryName == null || countryName.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body(new ErrorMessage("Country Name cannot be null or empty."));
        }
     // Check if country name contains only letters
     
        if (!Pattern.matches("^[a-zA-Z\\s]+$", countryName)) {  // Regex to check if country name has only letters and spaces
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body(new ErrorMessage("Country Name should only contain letters and spaces."));
        }
        
        // Validate that countryName has at least 2 characters
        if (countryName.length() < 2) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body(new ErrorMessage("Country Name must have at least 2 characters."));
        } 
        try {
        	
        	// Add the country via service
            Country savedCountry = countryService.addCountry(country);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedCountry);
            
        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(new ErrorMessage(ex.getReason()));
        }
        
        catch (DataIntegrityViolationException e) {
            // Catching the exception for unique constraint violations (like duplicate country name)
            String errorMessage = "Duplicate entry detected for Country Code : " + countryCode + " or Country Name : " + countryName + " already exists.";
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body(new ErrorMessage(errorMessage)); }
    }

    // Delete a country by countryCode
    @DeleteMapping("/delete/{countryCode}")
    public ResponseEntity<?> deleteCountry(@PathVariable String countryCode) {
    	if (countryCode == null || countryCode.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body(new ErrorMessage("Country Code cannot be null or empty."));
        }
   	  // Validate that Country code has at least 4 
        if (countryCode.length() < 4) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body(new ErrorMessage("Country Code must have at least length 4 ."));
        } 
  	  
  	  if (!Pattern.matches("^[0-9]+$", countryCode)) {  // Regex to check if country code has only numeric
  	        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
  	                             .body(new ErrorMessage("Country Code should only contain numbers."));
  	    }
    	
        try {
            // Delete the country via service
            countryService.deleteCountry(countryCode);
            return ResponseEntity.status(HttpStatus.OK).body("Country deleted successfully");
        } catch (ResponseStatusException ex) {
        	  return ResponseEntity.status(ex.getStatusCode()).body(new ErrorMessage(ex.getReason()));
        }
    }
}

