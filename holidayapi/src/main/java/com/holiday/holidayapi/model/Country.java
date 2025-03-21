package com.holiday.holidayapi.model;


import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Setter
@Getter
@Entity
@Table(name="country")
public class Country {
	
    @Id
    private String countryCode; // country_code as primary key
    
   
    private String countryName;
    
	/*
	 * @OneToMany(mappedBy = "country", cascade = CascadeType.ALL, orphanRemoval =
	 * true) private List<Holiday> federalHolidays;
	 */

}

