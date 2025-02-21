package com.holiday.holidayapi.service;

import com.holiday.holidayapi.model.Holiday;
import com.holiday.holidayapi.repository.HolidayRepository;
import com.holiday.holidayapi.service.HolidayService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HolidayServiceTest {

    @Mock
    private HolidayRepository holidayRepository;

    @InjectMocks
    private HolidayService holidayService;

    private Holiday holiday;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        holiday = new Holiday("USA", "Independence Day", LocalDate.of(2025, 7, 4));
    }

    @Test
    void testGetHolidays() {
        // Given
        List<Holiday> holidays = Arrays.asList(holiday);
        when(holidayRepository.findByCountry("USA")).thenReturn(holidays);

        // When
        List<Holiday> result = holidayService.getHolidays("USA");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Independence Day", result.get(0).getName());
    }

    @Test
    void testAddHoliday() {
        // Given
        when(holidayRepository.save(any(Holiday.class))).thenReturn(holiday);

        // When
        Holiday result = holidayService.addHoliday("USA", "Independence Day", LocalDate.of(2025, 7, 4));

        // Then
        assertNotNull(result);
        assertEquals("Independence Day", result.getName());
    }

    @Test
    void testUpdateHoliday() {
        // Given
        Holiday updatedHoliday = new Holiday("USA", "Updated Independence Day", LocalDate.of(2025, 7, 4));
        when(holidayRepository.findById(1L)).thenReturn(Optional.of(holiday));
        when(holidayRepository.save(any(Holiday.class))).thenReturn(updatedHoliday);

        // When
        Holiday result = holidayService.updateHoliday(1L, "USA", "Updated Independence Day", LocalDate.of(2025, 7, 4));

        // Then
        assertNotNull(result);
        assertEquals("Updated Independence Day", result.getName());
    }

    @Test
    void testDeleteHoliday() {
        // Given
        doNothing().when(holidayRepository).deleteById(1L);

        // When
        holidayService.deleteHoliday(1L);

        // Then
        verify(holidayRepository, times(1)).deleteById(1L);
    }
}
