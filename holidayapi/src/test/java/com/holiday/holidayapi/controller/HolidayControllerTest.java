package com.holiday.holidayapi.controller;

import com.holiday.holidayapi.dto.HolidayDTO;
import com.holiday.holidayapi.model.Holiday;
import com.holiday.holidayapi.service.HolidayService;
import com.holiday.holidayapi.exception.ErrorMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class HolidayControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private HolidayService holidayService;

    @InjectMocks
    private HolidayController holidayController;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(holidayController).build();
    }

    @Test
    public void testGetHolidays() throws Exception {
        String country = "USA";
        List<Holiday> holidays = Arrays.asList(new Holiday(country, "New Year", LocalDate.of(2025, 1, 1)));

        when(holidayService.getHolidays(eq(country))).thenReturn(holidays);

        mockMvc.perform(get("/api/holidays?country=USA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("New Year"));
    }

    @Test
    public void testGetHolidays_Exception() throws Exception {
        String country = "USA";
        when(holidayService.getHolidays(eq(country))).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Holidays not found"));

        mockMvc.perform(get("/api/holidays?country=USA"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Holidays not found"));
    }

    @Test
    public void testAddHoliday() throws Exception {
        HolidayDTO holidayDTO = new HolidayDTO("USA", "Independence Day", LocalDate.of(2025, 7, 4));
        Holiday holiday = new Holiday("USA", "Independence Day", LocalDate.of(2025, 7, 4));

        when(holidayService.addHoliday(eq("USA"), eq("Independence Day"), eq(LocalDate.of(2025, 7, 4)))).thenReturn(holiday);

        mockMvc.perform(post("/api/holidays/add")
                        .contentType("application/json")
                        .content("{\"country\":\"USA\",\"name\":\"Independence Day\",\"date\":\"2025-07-04\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Independence Day"));
    }

    @Test
    public void testUpdateHoliday() throws Exception {
        Long id = 1L;
        HolidayDTO holidayDTO = new HolidayDTO("USA", "Updated Holiday", LocalDate.of(2025, 12, 25));
        Holiday updatedHoliday = new Holiday("USA", "Updated Holiday", LocalDate.of(2025, 12, 25));

        when(holidayService.updateHoliday(eq(id), eq("USA"), eq("Updated Holiday"), eq(LocalDate.of(2025, 12, 25)))).thenReturn(updatedHoliday);

        mockMvc.perform(put("/api/holidays/{id}", id)
                        .contentType("application/json")
                        .content("{\"country\":\"USA\",\"name\":\"Updated Holiday\",\"date\":\"2025-12-25\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Holiday"));
    }

    @Test
    public void testDeleteHoliday() throws Exception {
        Long id = 1L;

        mockMvc.perform(delete("/api/holidays/{id}", id))
                .andExpect(status().isOk())
                .andExpect(content().string("Holiday deleted successfully"));
    }

    @Test
    public void testUploadHolidays() throws Exception {
        // Simulate the file upload and holiday upload functionality.
        // You can mock the file parsing and holiday addition to verify behavior.

        mockMvc.perform(multipart("/api/holidays/upload")
                        .file("file", "USA,Christmas,2025-12-25\nCanada,National Day,2025-07-01".getBytes()))
                .andExpect(status().isOk())
                .andExpect(content().string("File uploaded and holidays added successfully!"));
    }

    @Test
    public void testUploadHolidays_Error() throws Exception {
        // Simulate error due to invalid file format
        mockMvc.perform(multipart("/api/holidays/upload")
                        .file("file", "InvalidFormat".getBytes()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid file format. Each line must contain 'Country,Name,Date'."));
    }
}
