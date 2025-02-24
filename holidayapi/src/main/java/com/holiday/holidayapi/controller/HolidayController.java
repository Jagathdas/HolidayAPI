package com.holiday.holidayapi.controller;

import com.holiday.holidayapi.dto.HolidayDTO;
import com.holiday.holidayapi.model.Holiday;
import com.holiday.holidayapi.service.HolidayService;
import com.holiday.holidayapi.exception.ErrorMessage;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
    @GetMapping
    public ResponseEntity<?>  getHolidays(
            @Parameter(description = "Country to fetch holidays for") @RequestParam @Valid @NotNull(message = "Country cannot be null") @NotEmpty(message = "Country cannot be empty") String country) {
    	try {
    		  
            return ResponseEntity.ok(holidayService.getHolidays(country));
        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(new ErrorMessage(ex.getReason()));
        }
    }

    // Add a new holiday
    @PostMapping("/add")
    public ResponseEntity<?> addHoliday(
            @Valid @RequestBody HolidayDTO holidayDTO) {
        try {
            Holiday holiday = holidayService.addHoliday(holidayDTO.getCountry(), holidayDTO.getName(), holidayDTO.getDate());
            return ResponseEntity.status(HttpStatus.CREATED).body(holiday);
        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(new ErrorMessage(ex.getReason()));
        }
    }

 // Update an existing holiday
    @PutMapping("/{id}")
    public ResponseEntity<?> updateHoliday(
            @PathVariable Long id,
            @Valid @RequestBody HolidayDTO holidayDTO) {
    	 try {
        Holiday holiday = holidayService.updateHoliday(id, holidayDTO.getCountry(), holidayDTO.getName(), holidayDTO.getDate());
        return ResponseEntity.ok(holiday);
    	 }
        catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(new ErrorMessage(ex.getReason()));
        }
    }

 // Delete a holiday
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteHoliday(@PathVariable Long id) {
    	try {
            holidayService.deleteHoliday(id);
            return ResponseEntity.ok("Holiday deleted successfully");
        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(new ErrorMessage(ex.getReason()));
        }
    }
    
 // Upload a file with holidays
    @PostMapping("/upload")
    public ResponseEntity<String> uploadHolidays(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("No file uploaded.");
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] holidayData = line.split(",");
                if (holidayData.length != 3) {
                    return ResponseEntity.badRequest().body("Invalid file format. Each line must contain 'Country,Name,Date'.");
                }

                String country = holidayData[0].trim();
                String name = holidayData[1].trim();
                LocalDate date = LocalDate.parse(holidayData[2].trim());
                // Avoid duplicates 
                Holiday existingHoliday = holidayService.findByCountryAndNameAndDate(country, name, date);
                // If the findByCountryAndNameAndDate method returns null, then it means no such holiday exists and the holiday can be added to the database.
                if (existingHoliday == null) {
                    holidayService.addHoliday(country, name, date);
                    
                }
            }
            return ResponseEntity.ok("File uploaded and holidays added successfully!");
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error reading file.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error uploading holidays.");
        }
    }

}