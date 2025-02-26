package com.holiday.holidayapi.service;

import com.holiday.holidayapi.model.Country;
import com.holiday.holidayapi.model.FileProcessingResult;
import com.holiday.holidayapi.model.Holiday;
import com.holiday.holidayapi.repository.CountryRepository;
import com.holiday.holidayapi.repository.HolidayRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Service
public class HolidayService {

	private static final Logger logger = LoggerFactory.getLogger(HolidayService.class);
	
    @Autowired
    private CountryRepository countryRepository;
    
	@Autowired
    private HolidayRepository holidayRepository;

	
	  public List<Holiday> getHolidays(String country) { 
		  List<Holiday> holidays = holidayRepository.findByCountryName(country); 
		  
		  // If no holidays found for the   country, throw an exception 
	
	  if (holidays.isEmpty()) { 
		  throw new ResponseStatusException(HttpStatus.NOT_FOUND,"No holidays found for country: " + country); 
		  }
	  
	      return holidays; 
	  }
	 

    public Holiday addHoliday(Holiday holiday) {
    	
    	// Check if the country exists
        Country country = countryRepository.findById(holiday.getCountry().getCountryCode()).orElse(null);
        if (country == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Country code does not exist: " + country);
        }
        
        // Log the country name to verify it
        logger.info("Fetched country: {}", country.getCountryName());
        // Set the country to the holiday details
        holiday.setCountry(country);
        holiday.setCountryName(country.getCountryName());
        
        // Save the holiday details
        return holidayRepository.save(holiday);

   }
    
    
    // Case 1: Update holiday with ID and Country Code
    public Holiday updateHolidayByIdAndCountryCode(Long id, String countryCode, Holiday holidayDetails) {
        Holiday existingHoliday = holidayRepository.findByIdAndCountryCountryCode(id, countryCode);
        if (existingHoliday != null) {
            existingHoliday.setHolidayDate(holidayDetails.getHolidayDate());
            existingHoliday.setHolidayName(holidayDetails.getHolidayName());
            existingHoliday.setDayOfWeek();
            return holidayRepository.save(existingHoliday);
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Details not found: " );
    }

    // Case 2: Update holiday with Country Code and Date
    public Holiday updateHolidayByCountryCodeAndDate(String countryCode, LocalDate holidayDate, Holiday holidayDetails) {
        Holiday existingHoliday = holidayRepository.findByCountryCountryCodeAndHolidayDate(countryCode, holidayDate);
        if (existingHoliday == null) {
        	throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Details not found: " );
        
        }
    	
        existingHoliday.setHolidayName(holidayDetails.getHolidayName());
        return holidayRepository.save(existingHoliday);
    }

    public int deleteHolidayByCountryCodeAndDate(String countryCode, LocalDate holidayDate) {
        // Try to find the holiday by country code and holiday date
        Holiday holiday = holidayRepository.findByCountryCountryCodeAndHolidayDate(countryCode, holidayDate);
        int deletedCount = 0;
        if (holiday == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Holiday not found for the given country code and date");
        }

        // Delete the holiday
        holidayRepository.delete(holiday);
        // Increment the deleted count
        deletedCount++;
        return deletedCount;
    }
    
    public int deleteHolidaysByCountryCode(String countryCode) {
        // Get all holidays by country code
        List<Holiday> holidays = holidayRepository.findByCountryCountryCode(countryCode);
        
        if (holidays.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No holidays found for the given country code");
        }
        // Delete all holidays for the given country code
        holidayRepository.deleteAll(holidays);
        return holidays.size();
    }
    
 // Method to process the holiday file
    public FileProcessingResult processHolidayFile(MultipartFile file) throws IOException {
        List<String> errorMessages = new ArrayList<>();
        List<Holiday> processedHolidays = new ArrayList<>();
        int totalRecords = 0;
        int failedRecords = 0;
        // Validate holidayDate format (ISO format)
        LocalDate holidayDate;

        // Use Apache POI (for Excel) or OpenCSV (for CSV) to parse the file
        // Assuming the file is in CSV format, you can adjust this for Excel format too

        BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
        String line;

        while ((line = reader.readLine()) != null) {
            totalRecords++;

            String[] fields = line.split(",");
            if (fields.length != 4) {
                errorMessages.add("Invalid record format at line " + totalRecords);
                failedRecords++;
                continue;
            }

            String countryCode = fields[0].trim();
            String countryName = fields[1].trim();
            String holidayName = fields[2].trim();
            String holidayDateStr = fields[3].trim();
            
         // Check for missing or empty fields
            if (countryCode.isEmpty()) {
                errorMessages.add("Missing country code at line " + totalRecords);
                failedRecords++;
                continue;
            }
            if (countryName.isEmpty()) {
                errorMessages.add("Missing country name at line " + totalRecords);
                failedRecords++;
                continue;
            }
            if (holidayName.isEmpty()) {
                errorMessages.add("Missing holiday name at line " + totalRecords);
                failedRecords++;
                continue;
            }
            if (holidayDateStr.isEmpty()) {
                errorMessages.add("Missing holiday date at line " + totalRecords);
                failedRecords++;
                continue;
            }
           
            //logger.info("Fetched country from file: {}", countryName);
            // Validate and process the record
            try {
                // Check if the country exists in the database
                Country country = countryRepository.findByCountryCode(countryCode);
                //logger.info("Fetched countryName from DB: {}", country.getCountryName());
                if (country == null) {
                    errorMessages.add("Country with country code " + countryCode + " not found in database at line " + totalRecords);
                    failedRecords++;
                    continue;
                }
                if (!country.getCountryName().equals(countryName)) {
                    errorMessages.add("Country mismatch for line " + totalRecords + " : Expected " + country.getCountryName() + ", but found " + countryName);
                    failedRecords++;
                    continue;
                }

               
                try {
                	holidayDate = LocalDate.parse(holidayDateStr); // Assuming it's in ISO format (yyyy-MM-dd)
                } catch (Exception e) {
                    errorMessages.add("Invalid date format at line " + totalRecords + ": Expected yyyy-MM-dd but found " + holidayDateStr);
                    failedRecords++;
                    continue;
                }

                // Check for duplicates (existing holiday for this country and date)
                if (holidayRepository.findByCountryCountryCodeAndHolidayDate(countryCode, holidayDate) != null) {
                    errorMessages.add("Duplicate holiday for country code " + countryCode + " and holiday date " + holidayDateStr + " at line " + totalRecords);
                    failedRecords++;
                    continue;
                }

                // Create new Holiday entity
                Holiday holiday = new Holiday();
                holiday.setCountry(country);
                holiday.setCountryName(countryName);
                holiday.setHolidayDate(holidayDate);
                holiday.setHolidayName(holidayName);
                
                // Set the day of the week (you can use Java's DayOfWeek)
                holiday.setDayOfWeek(holidayDate.getDayOfWeek().toString());

                // Save the holiday
                holidayRepository.save(holiday);
                processedHolidays.add(holiday);

            } catch (Exception e) {
                errorMessages.add("Error processing line " + totalRecords + ": " + e.getMessage());
                failedRecords++;
            }
        }

        // Prepare the result
        FileProcessingResult result = new FileProcessingResult();
        result.setTotalRecords(totalRecords);
        result.setProcessedRecords(processedHolidays.size());
        result.setFailedRecords(failedRecords);
        result.setErrorMessages(errorMessages);
        result.setFileName(file.getOriginalFilename());

        return result;
    }

	/*
	 * 
	 * // Find holiday by country, name, and date public Holiday
	 * findByCountryAndNameAndDate(String country, String name, LocalDate date) {
	 * return holidayRepository.findByCountryAndNameAndDate(country, name, date);
	 * //.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
	 * "Holiday not found for country: " + country + ", name: " + name +
	 * ", and date: " + date)); }
	 */
 


}


