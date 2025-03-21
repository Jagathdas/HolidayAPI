package com.holiday.holidayapi.service;

import com.holiday.holidayapi.exception.ErrorMessage;
import com.holiday.holidayapi.model.Country;
import com.holiday.holidayapi.model.FileProcessingResult;
import com.holiday.holidayapi.model.GetHolidayListResponse;
import com.holiday.holidayapi.model.Holiday;
import com.holiday.holidayapi.model.HolidayListResponse;
import com.holiday.holidayapi.model.HolidayUpdateRequest;
import com.holiday.holidayapi.repository.CountryRepository;
import com.holiday.holidayapi.repository.HolidayRepository;
import com.holiday.holidayapi.validation.HolidayDateValidator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;


@Service
public class HolidayService {

	private static final Logger logger = LoggerFactory.getLogger(HolidayService.class);
	
    @Autowired
    private CountryRepository countryRepository;
    
	@Autowired
    private HolidayRepository holidayRepository;

	
	  public HolidayListResponse getHolidays(String country) { 
		  List<Holiday> holidays = holidayRepository.findByCountryName(country); 
		  
		  // If no holidays found for the   country, throw an exception 
	
	  if (holidays.isEmpty()) { 
		  throw new ResponseStatusException(HttpStatus.NOT_FOUND,"No holidays found for country: " + country); 
		  }
	// Map the Holiday entities to GetHolidayListResponse 
	  List<GetHolidayListResponse> holidayResponses = holidays.stream()
              .map(holiday -> new GetHolidayListResponse(holiday.getHolidayName(), holiday.getHolidayDate()))
              .collect(Collectors.toList());
	  // Wrap the response in HolidayListResponse with the country name and holiday list
      return new HolidayListResponse(country, holidayResponses);
	    //  return holidays; 
	  }
	 

    public Holiday addHoliday(Holiday holiday) {
    	
    	// Check if the country exists
        Country country = countryRepository.findById(holiday.getCountry().getCountryCode()).orElse(null);
        if (country == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Country code does not exist: " + country);
        }
        
        LocalDate holidayDate = holiday.getHolidayDate();
        Holiday holidays = holidayRepository.findByCountryCountryCodeAndCountryNameAndHolidayNameAndHolidayDate(holiday.getCountry().getCountryCode(), holiday.getCountryName(), holiday.getHolidayName() , holidayDate); 
        
        if (holidays != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Holiday name or holiday date already exist: ");
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
    public Holiday updateHolidayByIdAndCountryCode(Long id, String countryCode, HolidayUpdateRequest holidayUpdateRequests) {
        Holiday existingHoliday = holidayRepository.findByIdAndCountryCountryCode(id, countryCode);
        if (existingHoliday != null) {
            existingHoliday.setHolidayDate(holidayUpdateRequests.getHolidayDate());
            existingHoliday.setHolidayName(holidayUpdateRequests.getHolidayName());
            existingHoliday.setDayOfWeek();
            return holidayRepository.save(existingHoliday);
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Holiday Details not found for the given country code or Id " );
    }

    // Case 2: Update holiday with Country Code and Date
    public Holiday updateHolidayByCountryCodeAndDate(String countryCode, LocalDate holidayDate, String holidayName) {
        Holiday existingHoliday = holidayRepository.findByCountryCountryCodeAndHolidayDate(countryCode, holidayDate);
        if (existingHoliday == null) {
        	throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Holiday Details not found for the given country code and date " );
        
        }
    	
        existingHoliday.setHolidayName(holidayName);
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
    public List<FileProcessingResult>  processHolidayFiles(List<MultipartFile> files) throws IOException {
    	 List<FileProcessingResult> allResults = new ArrayList<>();
    	 
    	for (MultipartFile file : files) {
        List<String> errorMessages = new ArrayList<>();
        List<Holiday> processedHolidays = new ArrayList<>();
        int totalRecords = 0;
        int failedRecords = 0;
        // Validate holidayDate format (ISO format)
        LocalDate holidayDate;

        // Use Apache POI (for Excel) or OpenCSV (for CSV) to parse the file
        // Assuming the file is in CSV format, you can adjust this for Excel format too

        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()));
                CSVReader reader = new CSVReader(br)) {
       //String line;

        // Read the first line as the header (column names)
     // Read the first line as the header (column names)
        String[] header = reader.readNext();
      
        if (header == null) {
            // Handle empty file scenario
            errorMessages.add("Empty file: No data found.");
            allResults.add(createFileProcessingResult(file, totalRecords, processedHolidays.size(), failedRecords, errorMessages));
            continue;
        }
        Map<String, Integer> columnIndexMap = new HashMap<>();

        // Map the column names to their indices
        for (int i = 0; i < header.length; i++) {
        	 columnIndexMap.put(header[i].trim(), i);
        }
        // Process the CSV file row by row
        String[] line;
       
        
        while ((line = reader.readNext()) != null) {
       
            totalRecords++;
        
            // Validate that the line has the expected number of fields (columns)
            if (line.length != header.length) {
                errorMessages.add("Invalid record format at line " + totalRecords);
                failedRecords++;
                continue;
            }

            // Dynamically map fields to column names using the columnIndexMap
            String countryCode = line[columnIndexMap.get("CountryCode")].trim();
            String countryName = line[columnIndexMap.get("CountryName")].trim();
            String holidayName = line[columnIndexMap.get("HolidayName")].trim();
            String holidayDateStr = line[columnIndexMap.get("HolidayDate")].trim();

            
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
            if (countryCode.length() < 4) {
            	errorMessages.add("Country Code must have at least length 4 at line" + totalRecords);
            	failedRecords++;
                continue;
            }
            if (!Pattern.matches("^[0-9]+$", countryCode)) {  // Regex to check if country code has only numeric
            	 errorMessages.add("Country Code should only contain numbers at line" + totalRecords);
            	 failedRecords++;
                 continue;
            }
         // Validate that holiday Name has at least 2 characters
            if (countryName.length() < 2) {
            	 errorMessages.add("Country Name must have at least 2 characters at line" + totalRecords);
                failedRecords++;
                continue;
            }
            if (!Pattern.matches("^[a-zA-Z\\s]+$", countryName)) {  // Regex to check if holiday name has only letters and space
            	 errorMessages.add("Country name can only contain letters and spaces at line" + totalRecords);
            	 failedRecords++;
                 continue;
            }
            
            // Validate that holiday Name has at least 2 characters
            if (holidayName.length() < 2) {
            	 errorMessages.add("Holiday Name must have at least 2 characters at line" + totalRecords);
                failedRecords++;
                continue;
            }
            if (!Pattern.matches("^[a-zA-Z\\s]+$", holidayName)) {  // Regex to check if holiday name has only letters and space
            	 errorMessages.add("Holiday name can only contain letters and spaces at line" + totalRecords);
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
                
                int holidayYear = holidayDate.getYear();
                LocalDate currentDate = LocalDate.now();
                int currentYear = currentDate.getYear();
             // Check if the holiday year is in the future or the past
                if (holidayYear > currentYear) {
                	 errorMessages.add("Holiday date cannot be in the future year at line" + totalRecords);
                	 failedRecords++;
                     continue;
                }

                if (holidayYear < currentYear) {
                	 errorMessages.add("Holiday date cannot be in a past year at line" + totalRecords);
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
        
    	} catch (CsvValidationException e) {
    		errorMessages.add("Error reading CSV file: " + e.getMessage());
        }

        // Prepare the result
        FileProcessingResult result = new FileProcessingResult();
        result.setTotalRecords(totalRecords);
        result.setProcessedRecords(processedHolidays.size());
        result.setFailedRecords(failedRecords);
        result.setErrorMessages(errorMessages);
        result.setFileName(file.getOriginalFilename());
        allResults.add(result);
    }
        return allResults;
    }

 // Helper method to create FileProcessingResult
    private FileProcessingResult createFileProcessingResult(MultipartFile file, int totalRecords, int processedRecords, int failedRecords, List<String> errorMessages) {
        FileProcessingResult result = new FileProcessingResult();
        result.setTotalRecords(totalRecords);
        result.setProcessedRecords(processedRecords);
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


