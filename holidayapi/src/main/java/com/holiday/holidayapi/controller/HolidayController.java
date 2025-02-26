package com.holiday.holidayapi.controller;

import com.holiday.holidayapi.dto.HolidayDTO;
import com.holiday.holidayapi.model.FileProcessingResult;
import com.holiday.holidayapi.model.Holiday;
import com.holiday.holidayapi.service.HolidayService;
import com.holiday.holidayapi.exception.ErrorMessage;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/holidays")
@Tag(name = "Holiday API", description = "API for managing holidays")
@Validated 
public class HolidayController {

    @Autowired
    private HolidayService holidayService;

 // Get Holidays for a specific country
	
	  @GetMapping public ResponseEntity<?> getHolidays(@Parameter(description ="Country to fetch holidays for") @RequestParam @Valid @NotNull(message ="Country cannot be null") @NotEmpty(message = "Country cannot be empty") String country) { 
		try {
	  
	  return ResponseEntity.ok(holidayService.getHolidays(country)); 
	  } catch(ResponseStatusException ex) { return ResponseEntity.status(ex.getStatusCode()).body(new ErrorMessage(ex.getReason())); } 
		
	}
	 

    // Add a new holiday
    @PostMapping("/add")
    public ResponseEntity<?> addHoliday(
            @Valid @RequestBody Holiday holiday) {
        try {
        	 Holiday savedHolidayDetails = holidayService.addHoliday(holiday);
         // Include the country_name in the response (Optional)
            String countryName = savedHolidayDetails.getCountry().getCountryName();
            return ResponseEntity.status(HttpStatus.CREATED).body(holiday);
        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(new ErrorMessage(ex.getReason()));
        }
    }

    
    // Case 1: Update holiday date and holiday name with help of  holiday with ID and Country Code
    @PutMapping("/{id}/{countryCode}")
    public ResponseEntity<?> updateHolidayByIdAndCountryCode(
            @PathVariable("id") Long id,
            @PathVariable("countryCode") String countryCode,
            @RequestBody Holiday holidayDetails) {
    try {
        Holiday updatedHoliday = holidayService.updateHolidayByIdAndCountryCode(id, countryCode, holidayDetails);
     
            return ResponseEntity.ok(updatedHoliday);
        
    } 
        catch (ResponseStatusException ex) { 
        	return ResponseEntity.status(ex.getStatusCode()).body(new ErrorMessage(ex.getReason())); }
    }
    
    
    // Case 2: Update holiday name with help of Country Code and holiday Date
    @PutMapping("/country/{countryCode}/{holidayDate}")
    public ResponseEntity<?> updateHolidayByCountryCodeAndDate(
            @PathVariable("countryCode") String countryCode,
            @PathVariable("holidayDate") LocalDate holidayDate,
            @RequestBody Holiday holidayDetails) {

        
    try {    
    	Holiday updatedHoliday = holidayService.updateHolidayByCountryCodeAndDate(countryCode, holidayDate, holidayDetails);
       return ResponseEntity.ok(updatedHoliday);
      } 
        catch (ResponseStatusException ex) { 
        	return ResponseEntity.status(ex.getStatusCode()).body(new ErrorMessage(ex.getReason())); }
    }
    
    
    //Case 1: delete records with condition of county code and holiday date
    @DeleteMapping("/country/{countryCode}/{holidayDate}")
    public ResponseEntity<?> deleteHolidayByCountryCodeAndDate(
            @PathVariable("countryCode") String countryCode,
            @PathVariable("holidayDate") LocalDate holidayDate) {

        try {
        	int deletedCount = holidayService.deleteHolidayByCountryCodeAndDate(countryCode, holidayDate);
        	 // Return success message
            return ResponseEntity.ok("Successfully deleted records " + deletedCount + " holiday(s) for country code " + countryCode + " and date " + holidayDate);
        } catch (ResponseStatusException ex) {
           return ResponseEntity.status(ex.getStatusCode()).body(new ErrorMessage(ex.getReason()));
        }
        catch (Exception e) {
            // Handle any other exceptions (e.g., invalid date format, etc.)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body("An error occurred while processing the request: " + e.getMessage());
        }
    }
    
    // bulk delete
    //Case 2: delete records with condition of country code
    @DeleteMapping("/country/{countryCode}")
    public ResponseEntity<?> deleteHolidaysByCountryCode(
            @PathVariable("countryCode") String countryCode) {

        try {
            int deletedCount = holidayService.deleteHolidaysByCountryCode(countryCode);
            return ResponseEntity.ok("Successfully deleted records" + deletedCount + " holiday(s) for country code " + countryCode); 
        } catch (ResponseStatusException ex) {
        	return ResponseEntity.status(ex.getStatusCode()).body(new ErrorMessage(ex.getReason()));
         }
         catch (Exception e) {
             // Handle any other exceptions (e.g., invalid date format, etc.)
             return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                  .body("An error occurred while processing the request: " + e.getMessage());
         }
    }



        // Endpoint to upload and process the file
        @PostMapping(value="/upload", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
        public ResponseEntity<?> uploadHolidayFile(@RequestParam("file") MultipartFile file) {
        	if(file.isEmpty()) { 
        		return ResponseEntity.badRequest().body("No file uploaded."); }
            try {
                // Process the uploaded file and get results
                FileProcessingResult result = holidayService.processHolidayFile(file);
                return ResponseEntity.ok(result);
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new ErrorMessage("File processing failed: " + e.getMessage()));
            }
        }
    


	/*
	 * * // Upload a file with holidays
	 * 
	 * @PostMapping("/upload") public ResponseEntity<String>
	 * uploadHolidays(@RequestParam("file") MultipartFile file) { if
	 * (file.isEmpty()) { return
	 * ResponseEntity.badRequest().body("No file uploaded."); }
	 * 
	 * try (BufferedReader reader = new BufferedReader(new
	 * InputStreamReader(file.getInputStream()))) { String line; while ((line =
	 * reader.readLine()) != null) { String[] holidayData = line.split(","); if
	 * (holidayData.length != 3) { return ResponseEntity.badRequest().
	 * body("Invalid file format. Each line must contain 'Country,Name,Date'."); }
	 * 
	 * String country = holidayData[0].trim(); String name = holidayData[1].trim();
	 * LocalDate date = LocalDate.parse(holidayData[2].trim()); // Avoid duplicates
	 * Holiday existingHoliday = holidayService.findByCountryAndNameAndDate(country,
	 * name, date); // If the findByCountryAndNameAndDate method returns null, then
	 * it means no such holiday exists and the holiday can be added to the database.
	 * if (existingHoliday == null) { holidayService.addHoliday(country, name,
	 * date);
	 * 
	 * } } return
	 * ResponseEntity.ok("File uploaded and holidays added successfully!"); } catch
	 * (IOException e) { return
	 * ResponseEntity.status(500).body("Error reading file."); } catch (Exception e)
	 * { return ResponseEntity.status(500).body("Error uploading holidays."); } }
	 */

}