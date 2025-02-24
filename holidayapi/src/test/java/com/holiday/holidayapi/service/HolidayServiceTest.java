package com.holiday.holidayapi.service;

import com.holiday.holidayapi.model.Holiday;
import com.holiday.holidayapi.repository.HolidayRepository;
import com.holiday.holidayapi.service.HolidayService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class HolidayServiceTest {

    @Mock
    private HolidayRepository holidayRepository;

    @InjectMocks
    private HolidayService holidayService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetHolidays_Success() {
        // Mock the repository to return a list of holidays
        String country = "USA";
        List<Holiday> holidays = Arrays.asList(new Holiday(country, "New Year", LocalDate.of(2025, 1, 1)));
        when(holidayRepository.findByCountry(country)).thenReturn(holidays);

        List<Holiday> result = holidayService.getHolidays(country);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("New Year", result.get(0).getName());
    }

    @Test
    public void testGetHolidays_NoHolidaysFound() {
        String country = "USA";
        when(holidayRepository.findByCountry(country)).thenReturn(Arrays.asList());

        ResponseStatusException thrown = assertThrows(ResponseStatusException.class, () -> {
            holidayService.getHolidays(country);
        });

        assertEquals(HttpStatus.NOT_FOUND, thrown.getStatusCode());
        assertEquals("No holidays found for country: " + country, thrown.getReason());
    }

    @Test
    public void testAddHoliday_Success() {
        String country = "USA";
        String name = "Independence Day";
        LocalDate date = LocalDate.of(2025, 7, 4);
        Holiday newHoliday = new Holiday(country, name, date);
        when(holidayRepository.findByCountryAndNameAndDate(country, name, date)).thenReturn(Optional.empty());
        when(holidayRepository.save(any(Holiday.class))).thenReturn(newHoliday);

        Holiday result = holidayService.addHoliday(country, name, date);

        assertNotNull(result);
        assertEquals(country, result.getCountry());
        assertEquals(name, result.getName());
        assertEquals(date, result.getDate());
    }

    @Test
    public void testAddHoliday_AlreadyExists() {
        String country = "USA";
        String name = "Independence Day";
        LocalDate date = LocalDate.of(2025, 7, 4);
        Holiday existingHoliday = new Holiday(country, name, date);
        when(holidayRepository.findByCountryAndNameAndDate(country, name, date)).thenReturn(Optional.of(existingHoliday));

        ResponseStatusException thrown = assertThrows(ResponseStatusException.class, () -> {
            holidayService.addHoliday(country, name, date);
        });

        assertEquals(HttpStatus.BAD_REQUEST, thrown.getStatusCode());
        assertEquals("Holiday with this name and date already exists for country: " + country, thrown.getReason());
    }

    @Test
    public void testUpdateHoliday_Success() {
        Long id = 1L;
        String country = "USA";
        String name = "Updated Holiday";
        LocalDate date = LocalDate.of(2025, 12, 25);
        Holiday existingHoliday = new Holiday("USA", "Old Holiday", LocalDate.of(2025, 12, 25));
        Holiday updatedHoliday = new Holiday(country, name, date);
        
        when(holidayRepository.findById(id)).thenReturn(Optional.of(existingHoliday));
        when(holidayRepository.save(any(Holiday.class))).thenReturn(updatedHoliday);

        Holiday result = holidayService.updateHoliday(id, country, name, date);

        assertNotNull(result);
        assertEquals(country, result.getCountry());
        assertEquals(name, result.getName());
        assertEquals(date, result.getDate());
    }

    @Test
    public void testUpdateHoliday_NotFound() {
        Long id = 1L;
        String country = "USA";
        String name = "Updated Holiday";
        LocalDate date = LocalDate.of(2025, 12, 25);

        when(holidayRepository.findById(id)).thenReturn(Optional.empty());

        ResponseStatusException thrown = assertThrows(ResponseStatusException.class, () -> {
            holidayService.updateHoliday(id, country, name, date);
        });

        assertEquals(HttpStatus.NOT_FOUND, thrown.getStatusCode());
        assertEquals("Holiday not found with ID: " + id, thrown.getReason());
    }

    @Test
    public void testDeleteHoliday_Success() {
        Long id = 1L;
        when(holidayRepository.existsById(id)).thenReturn(true);

        holidayService.deleteHoliday(id);

        verify(holidayRepository, times(1)).deleteById(id);
    }

    @Test
    public void testDeleteHoliday_NotFound() {
        Long id = 1L;
        when(holidayRepository.existsById(id)).thenReturn(false);

        ResponseStatusException thrown = assertThrows(ResponseStatusException.class, () -> {
            holidayService.deleteHoliday(id);
        });

        assertEquals(HttpStatus.NOT_FOUND, thrown.getStatusCode());
        assertEquals("Holiday not found with ID: " + id, thrown.getReason());
    }

    @Test
    public void testFindByCountryAndNameAndDate_Success() {
        String country = "USA";
        String name = "Independence Day";
        LocalDate date = LocalDate.of(2025, 7, 4);
        Holiday holiday = new Holiday(country, name, date);

        when(holidayRepository.findByCountryAndNameAndDate(country, name, date)).thenReturn(Optional.of(holiday));

        Holiday result = holidayService.findByCountryAndNameAndDate(country, name, date);

        assertNotNull(result);
        assertEquals(country, result.getCountry());
        assertEquals(name, result.getName());
        assertEquals(date, result.getDate());
    }

    @Test
    public void testFindByCountryAndNameAndDate_NotFound() {
        String country = "USA";
        String name = "Independence Day";
        LocalDate date = LocalDate.of(2025, 7, 4);

        when(holidayRepository.findByCountryAndNameAndDate(country, name, date)).thenReturn(Optional.empty());

        ResponseStatusException thrown = assertThrows(ResponseStatusException.class, () -> {
            holidayService.findByCountryAndNameAndDate(country, name, date);
        });

        assertEquals(HttpStatus.NOT_FOUND, thrown.getStatusCode());
        assertEquals("Holiday not found for country: " + country + ", name: " + name + ", and date: " + date, thrown.getReason());
    }
}
