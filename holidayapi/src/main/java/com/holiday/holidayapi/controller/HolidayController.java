package com.holiday.holidayapi.controller;

import com.holiday.holidayapi.model.FileProcessingResult;
import com.holiday.holidayapi.model.GetHolidayListResponse;
import com.holiday.holidayapi.model.Holiday;
import com.holiday.holidayapi.model.HolidayListResponse;
import com.holiday.holidayapi.model.HolidayNameRequest;
import com.holiday.holidayapi.model.HolidayUpdateRequest;
import com.holiday.holidayapi.service.HolidayService;
import com.holiday.holidayapi.validation.HolidayDateValidator;
import com.holiday.holidayapi.exception.ErrorMessage;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
//import jakarta.validation.constraints.Pattern;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@RestController
@RequestMapping("/api/holidays")
@Tag(name = "Holiday API", description = "API for managing holidays")
@Validated 
public class HolidayController {

    @Autowired
    private HolidayService holidayService;

 // Get Holidays for a specific country
	
	  @GetMapping public ResponseEntity<?> getHolidays(@Parameter(description ="Country to fetch holidays for") @RequestParam String country) { 
		
		  if (country == null || country.isEmpty()) {
	            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
	                                 .body(new ErrorMessage("country Name cannot be null or empty."));
	        }
	        
		  // Validate that country Name has at least 2 characters
	        if (country.length() < 2) {
	            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
	                                 .body(new ErrorMessage("Country Name must have at least 2 characters."));
	        } 
	        if (!Pattern.matches("^[a-zA-Z\\s]+$", country)) {  // Regex to check if holiday name has only letters and space
		        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
		                             .body(new ErrorMessage("country name can only contain letters and spaces"));
		    }
		  try {
			  HolidayListResponse holidayListResponse  = holidayService.getHolidays(country);
	  return ResponseEntity.ok(holidayListResponse); 
	  } catch(ResponseStatusException ex) { return ResponseEntity.status(ex.getStatusCode()).body(new ErrorMessage(ex.getReason())); } 
		
	}
	 

    // Add a new holiday
    @PostMapping("/add")
    public ResponseEntity<?> addHoliday(
          @RequestBody Holiday holiday) {
    	
    	 if (holiday.getCountry().getCountryCode() == null || holiday.getCountry().getCountryCode().isEmpty() && holiday.getHolidayName() == null || holiday.getHolidayName().isEmpty() && holiday.getHolidayDate() == null ) {
             return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                  .body(new ErrorMessage("Country Code and Holiday Name and Holiday Date cannot be null or empty."));
         }
    	  if (holiday.getCountry().getCountryCode() == null || holiday.getCountry().getCountryCode().isEmpty()) {
              return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                   .body(new ErrorMessage("Country Code cannot be null or empty."));
          }
     	  // Validate that Country code has at least 4 
          if (holiday.getCountry().getCountryCode().length() < 4) {
              return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                   .body(new ErrorMessage("Country Code must have at least length 4 ."));
          } 
    	  
    	  if (!Pattern.matches("^[0-9]+$", holiday.getCountry().getCountryCode())) {  // Regex to check if country code has only numeric
    	        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
    	                             .body(new ErrorMessage("Country Code should only contain numbers."));
    	    }
    	   // Check if country name is null or empty
        if (holiday.getHolidayName() == null || holiday.getHolidayName().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body(new ErrorMessage("Holiday Name cannot be null or empty."));
        }
        
  	  // Validate that holiday Name has at least 2 characters
        if (holiday.getHolidayName().length() < 2) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body(new ErrorMessage("Holiday Name must have at least 2 characters."));
        } 
        
        if (!Pattern.matches("^[a-zA-Z\\s]+$", holiday.getHolidayName())) {  // Regex to check if holiday name has only letters and space
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
	                             .body(new ErrorMessage("Holiday name can only contain letters and spaces"));
	    }
        if (holiday.getHolidayDate() == null ) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body(new ErrorMessage("Holiday Date cannot be null."));
        }
        
        if (!HolidayDateValidator.isValid(holiday.getHolidayDate())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body(new ErrorMessage("Invalid date format. Please use the correct format: yyyy-MM-dd."));
        }
        
    
     // Validate the holiday date using the utility method
        ResponseEntity<ErrorMessage> validationResponse = HolidayDateValidator.validateHolidayDate(holiday.getHolidayDate());
        
        // If validation fails, return the error response
        if (validationResponse != null) {
            return validationResponse;  // returns the error response from validation
        }
        
        try {
        	holidayService.addHoliday(holiday);
         return ResponseEntity.status(HttpStatus.CREATED).body(holiday);
        }
        catch (ResponseStatusException ex) {
        	 return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                     .body("An error occurred while processing the request: " + ex.getMessage());
        }
        
        catch (DataIntegrityViolationException e) {
            // Catching the exception for unique constraint violations (like duplicate country name)
            String errorMessage = "Duplicate entry detected for Country Code : " + holiday.getCountry().getCountryCode() + " , Holiday Name : " + holiday.getHolidayName() + " , Holiday Date : " + holiday.getHolidayDate() + " already exists.";
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body(new ErrorMessage(errorMessage)); }
    
    catch (Exception e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("An error occurred while processing the request: " + e.getMessage());
        }
}
 
    // Case 1: Update holiday date and holiday name with help of  holiday ID and Country Code
    @PutMapping("/{id}/{countryCode}")
    public ResponseEntity<?> updateHolidayByIdAndCountryCode(
            @PathVariable("id") Long id,
            @PathVariable("countryCode") String countryCode,
            @RequestBody HolidayUpdateRequest holidayUpdateRequests) {
    	
    	String holidayName = holidayUpdateRequests.getHolidayName();
    	LocalDate holidayDate = holidayUpdateRequests.getHolidayDate();
  	  if (id == null ) {
          return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                               .body(new ErrorMessage("Holiday ID cannot be null"));
      }
  	  
  	if (!Pattern.matches("^[0-9]*\\.?[0-9]+$", id.toString())) {  // Regex to check if Holiday ID has only numeric
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                             .body(new ErrorMessage("Holiday ID should only contain numbers."));
    }
	   // Check if country name is null or empty
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
    if (holidayDate == null ) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                             .body(new ErrorMessage("Holiday Date cannot be null."));
    }
    
    if (!HolidayDateValidator.isValid(holidayDate)) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                             .body(new ErrorMessage("Invalid date format. Please use the correct format: yyyy-MM-dd."));
    }
    

 // Validate the holiday date using the utility method
    ResponseEntity<ErrorMessage> validationResponse = HolidayDateValidator.validateHolidayDate(holidayDate);
    
    // If validation fails, return the error response
    if (validationResponse != null) {
        return validationResponse;  // returns the error response from validation
    }

    if (holidayName == null || holidayName.isEmpty() ) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                             .body(new ErrorMessage("Holiday name cannot be null."));
    }
    
 // Validate that Holiday name has at least 2 characters
    if (holidayName.length() < 2) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                             .body(new ErrorMessage("Holiday Name must have at least 2 characters."));
    }
    
    if (!Pattern.matches("^[a-zA-Z\\s]+$", holidayName)) {  // Regex to check if holiday name has only letters and space
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                             .body(new ErrorMessage("Holiday name can only contain letters and spaces"));
    }
    // Validate that holiday Name contains only alphabetic characters (letters)
    if (!Pattern.matches("^(?=(.*[a-zA-Z]){3})[a-zA-Z\\s]+$", holidayName)) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                             .body(new ErrorMessage("Holiday Name must contain atleast 3 alphabetic characters."));
    }
    
    try {
        Holiday updatedHoliday = holidayService.updateHolidayByIdAndCountryCode(id, countryCode, holidayUpdateRequests);
     
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
            @RequestBody HolidayNameRequest  holidayNameRequest) {
    	// Extract holidayName from the request body
      String holidayName = holidayNameRequest.getHolidayName();

    	   // Check if country name is null or empty
        if (countryCode == null || countryCode.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body(new ErrorMessage("Country Code cannot be null or empty."));
        }
        // Validate that Country Code has at least 2 characters
        if (countryCode.length() < 4) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body(new ErrorMessage("Country Code must have at least length 4."));
        }

        
        if (!Pattern.matches("^[0-9]+$", countryCode)) {  // Regex to check if country code has only numeric
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body(new ErrorMessage("Country Code should only contain numbers."));
        }
        if (!HolidayDateValidator.isValid(holidayDate)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body(new ErrorMessage("Invalid date format. Please use the correct format: yyyy-MM-dd."));
        }
        

     // Validate the holiday date using the utility method
        ResponseEntity<ErrorMessage> validationResponse = HolidayDateValidator.validateHolidayDate(holidayDate);
        
        // If validation fails, return the error response
        if (validationResponse != null) {
            return validationResponse;  // returns the error response from validation
        }

        if (holidayName == null || holidayName.isEmpty() ) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body(new ErrorMessage("Holiday name cannot be null."));
        }
        // Validate that holiday Name has at least 2 characters
        if (holidayName.length() < 2) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body(new ErrorMessage("Holiday Name must have at least 2 characters."));
        }
        if (!Pattern.matches("^[a-zA-Z\\s]+$", holidayName)) {  // Regex to check if holiday name has only letters and space
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body(new ErrorMessage("Holiday name can only contain letters and spaces"));
        }
     // Validate that holiday Name contains only alphabetic characters (letters)
        if (!Pattern.matches("^(?=(.*[a-zA-Z]){3})[a-zA-Z\\s]+$", holidayName)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body(new ErrorMessage("Holiday Name must contain atleast 3 alphabetic characters."));
        }
    try {    
    	Holiday updatedHoliday = holidayService.updateHolidayByCountryCodeAndDate(countryCode, holidayDate, holidayName);
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
    	
    	 // Check if country name is null or empty
        if (countryCode == null || countryCode.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body(new ErrorMessage("Country Code cannot be null or empty."));
        }
        
     // Validate that Country Code has at least 2 characters
        if (countryCode.length() < 4) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body(new ErrorMessage("Country Code must have at least length 4."));
        }
        if (!Pattern.matches("^[0-9]+$", countryCode)) {  // Regex to check if country code has only numeric
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body(new ErrorMessage("Country Code should only contain numbers."));
        }
        if (holidayDate == null ) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body(new ErrorMessage("Holiday Date cannot be null."));
        }
        if (!HolidayDateValidator.isValid(holidayDate)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body(new ErrorMessage("Invalid date format. Please use the correct format: yyyy-MM-dd."));
        }
        

     // Validate the holiday date using the utility method
        ResponseEntity<ErrorMessage> validationResponse = HolidayDateValidator.validateHolidayDate(holidayDate);
        
        // If validation fails, return the error response
        if (validationResponse != null) {
            return validationResponse;  // returns the error response from validation
        }

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
    	
    	// Check if country name is null or empty
        if (countryCode == null || countryCode.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body(new ErrorMessage("Country Code cannot be null or empty."));
        }
        
     // Validate that Country Code has at least 2 characters
        if (countryCode.length() < 4) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body(new ErrorMessage("Country Code must have at least length 4."));
        }
        
        if (!Pattern.matches("^[0-9]+$", countryCode)) {  // Regex to check if country code has only numeric
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body(new ErrorMessage("Country Code should only contain numbers."));
        }

        try {
            int deletedCount = holidayService.deleteHolidaysByCountryCode(countryCode);
            return ResponseEntity.ok("Successfully deleted records " + deletedCount + " holiday(s) for country code " + countryCode); 
        } catch (ResponseStatusException ex) {
        	return ResponseEntity.status(ex.getStatusCode()).body(new ErrorMessage(ex.getReason()));
         }
         catch (Exception e) {
             // Handle any other exceptions (e.g., invalid date format, etc.)
             return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                  .body("An error occurred while processing the request: " + e.getMessage());
         }
    }



        // upload and process the file
        @PostMapping(value="/upload", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
        public ResponseEntity<?> uploadHolidayFile(@RequestParam("files") List<MultipartFile> files) {
        	if(files.isEmpty()) { 
        		return ResponseEntity.badRequest().body("No file uploaded."); }
        
            // Validate if all files are CSV
            List<String> invalidFiles = new ArrayList<>();
            for (MultipartFile file : files) {
                if (!isCSVFile(file)) {
                    invalidFiles.add(file.getOriginalFilename());
                }
            }

            // If there are invalid files, return an error response
            if (!invalidFiles.isEmpty()) {
                return ResponseEntity.badRequest().body("Invalid file(s): " + String.join(", ", invalidFiles) + ". Only CSV files are allowed.");
            }
            try {
            	
            	List<FileProcessingResult> allResults = holidayService.processHolidayFiles(files);
                return ResponseEntity.ok(allResults); // Respond with the results of all files processed
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new ErrorMessage("File processing failed: " + e.getMessage()));
            }
        }
    
        
        //Date validation
        public boolean isValid(LocalDate holidayDate) {
        	final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            if (holidayDate == null ) {
                return false;  // Skip validation if the field is empty (handled by @NotNull)
            }

            // Convert the LocalDate to a string with the expected format and validate
            try {
                String dateString = holidayDate.format(formatter);  // Format it to a string in yyyy-MM-dd
                LocalDate parsedDate = LocalDate.parse(dateString, formatter);  // Try parsing it back to LocalDate
                return holidayDate.equals(parsedDate);  // If both match, the date format is valid
            } catch (DateTimeParseException e) {
                // If parsing fails, it's an invalid format
                return false;
            }
        }
        
     // Helper method to check if a file is CSV
        private boolean isCSVFile(MultipartFile file) {
            // Check the file extension
            String fileName = file.getOriginalFilename();
            if (fileName == null || !fileName.toLowerCase().endsWith(".csv")) {
                return false;
            }

            // Check MIME type (optional, to further verify that the file is a CSV)
            String mimeType = file.getContentType();
            return mimeType != null && mimeType.equals("text/csv");
        }
    
        
        //DatetimeparseException handle
        @ExceptionHandler(DateTimeParseException.class)
        public ResponseEntity<?> handleDateTimeParseException(DateTimeParseException ex) {
            // Print custom error message for DateTimeParseException
            String errorMessage = "Invalid date format. Please use the correct format: yyyy-MM-dd.";
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorMessage(errorMessage));
           
        }
        
        @ExceptionHandler(NumberFormatException.class)
        public ResponseEntity<?> handleNumberFormatException(NumberFormatException e) {
            // This method will be triggered if there's a failure to convert a non-numeric string to Long
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body(new ErrorMessage("Holiday ID should be a valid number."));
        }

}