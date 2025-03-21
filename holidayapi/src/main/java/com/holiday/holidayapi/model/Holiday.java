package com.holiday.holidayapi.model;


import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.Setter;
import lombok.Getter;

@Data
@Setter
@Getter
@Entity
@Table(name="Federal_holidays")
public class Holiday {


	    @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    @JsonIgnore 
	    private Long Id;
	    
	    @ManyToOne
	    @JoinColumn(name = "country_code", referencedColumnName = "countryCode", nullable = false)
	    private Country country;
	    
	    @JsonIgnore 
	    private String countryName;
	    
	    
	     private String holidayName;
	    
	   
	   // @FutureOrPresent(message = "Holiday date cannot be in the future")
	   // @ValidDateFormat(message = "Holiday date must be in the format yyyy-MM-dd")
	   // @JsonFormat(pattern = "yyyy-MM-dd") 
	    private LocalDate holidayDate;
	    
	    @JsonIgnore
	    private String dayOfWeek;

	   
	    // Set the day of the week based on the holidayDate
	    public void setDayOfWeek() {
	        if (holidayDate != null) {
	            this.dayOfWeek = holidayDate.getDayOfWeek().toString(); // Sets the day of the week as a string (e.g., "MONDAY")
	        }
	    }

}

