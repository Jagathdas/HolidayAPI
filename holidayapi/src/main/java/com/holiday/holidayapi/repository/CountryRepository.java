package com.holiday.holidayapi.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.holiday.holidayapi.model.Country;
import com.holiday.holidayapi.model.Holiday;

public interface CountryRepository extends JpaRepository<Country, String> {
    // Optional custom query methods can go here
	 Country findByCountryCodeAndCountryName(String countryCode, String countryName);
	 Country findByCountryCode(String countryCode);
}
