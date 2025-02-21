package com.holiday.holidayapi.controller;

import com.holiday.holidayapi.dto.HolidayDTO;
import com.holiday.holidayapi.model.Holiday;
import com.holiday.holidayapi.service.HolidayService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.util.List;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/holidays")
@Tag(name = "Holiday API", description = "API for managing holidays")
public class HolidayController {

    @Autowired
    private HolidayService holidayService;

    @GetMapping
    public List<Holiday> getHolidays(
            @Parameter(description = "Country to fetch holidays for") @RequestParam String country) {
        return holidayService.getHolidays(country);
    }

  
    @PostMapping
    public ResponseEntity<Holiday> addHoliday(
            @Valid @RequestBody HolidayDTO holidayDTO) {
        Holiday holiday = holidayService.addHoliday(holidayDTO.getCountry(), holidayDTO.getName(), holidayDTO.getDate());
        return ResponseEntity.ok(holiday);
    }

  
    @PutMapping("/{id}")
    public ResponseEntity<Holiday> updateHoliday(
            @PathVariable Long id,
            @Valid @RequestBody HolidayDTO holidayDTO) {
        Holiday holiday = holidayService.updateHoliday(id, holidayDTO.getCountry(), holidayDTO.getName(), holidayDTO.getDate());
        return ResponseEntity.ok(holiday);
    }

   
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHoliday(@PathVariable Long id) {
        holidayService.deleteHoliday(id);
        return ResponseEntity.noContent().build();
    }
    
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
                if (existingHoliday == null) {
                    holidayService.addHoliday(country, name, date);
                    
                    //error handling
                    //file error message print
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