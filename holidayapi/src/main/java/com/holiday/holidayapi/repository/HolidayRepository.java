package com.holiday.holidayapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.holiday.holidayapi.model.Holiday;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface HolidayRepository extends JpaRepository<Holiday, Long> {
    List<Holiday> findByCountry(String country);
    Optional<Holiday> findByCountryAndNameAndDate(String country, String name, LocalDate date);
}