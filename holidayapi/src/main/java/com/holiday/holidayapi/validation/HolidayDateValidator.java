package com.holiday.holidayapi.validation;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.holiday.holidayapi.exception.ErrorMessage;

public class HolidayDateValidator {

    // Method to validate holiday date against current date
    public static ResponseEntity<ErrorMessage> validateHolidayDate(LocalDate holidayDate) {
        // Get the components of the holiday date
        int holidayYear = holidayDate.getYear();
        int holidayMonth = holidayDate.getMonthValue();
        int holidayDay = holidayDate.getDayOfMonth();

        // Get the current date components
        LocalDate currentDate = LocalDate.now();
        int currentYear = currentDate.getYear();
        int currentMonth = currentDate.getMonthValue();
        int currentDay = currentDate.getDayOfMonth();

        // Check if the holiday year is in the future or the past
        if (holidayYear > currentYear) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorMessage("Holiday date cannot be in the future year."));
        }

        if (holidayYear < currentYear) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorMessage("Holiday date cannot be in a past year."));
        }

        // If the year is the current year, check the month and day
        if (holidayYear == currentYear) {
			
			/*
			 * if (holidayMonth > currentMonth || (holidayMonth == currentMonth &&
			 * holidayDay > currentDay)) { return
			 * ResponseEntity.status(HttpStatus.BAD_REQUEST) .body(new
			 * ErrorMessage("Holiday date cannot be in the future month or day.")); }
			 */
			 

            if (holidayMonth < currentMonth || (holidayMonth == currentMonth && holidayDay < currentDay)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorMessage("Holiday date cannot be in the past month or day."));
            }
        }

        // If no validation errors, return null (valid date)
        return null;
    }
    
    //Date validation
    public static boolean isValid(LocalDate holidayDate) {
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
}
