package com.holiday.holidayapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.holiday.holidayapi.model.Country;

public interface CountryRepository extends JpaRepository<Country, String> {
     Country findByCountryCodeAndCountryName(String countryCode, String countryName);
	 Country findByCountryCode(String countryCode);
}
