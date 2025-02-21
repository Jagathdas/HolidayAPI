package com.holiday.holidayapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.holiday.holidayapi.controller.HolidayController;
import com.holiday.holidayapi.dto.HolidayDTO;
import com.holiday.holidayapi.model.Holiday;
import com.holiday.holidayapi.service.HolidayService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.Arrays;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class HolidayControllerTest {

    @Mock
    private HolidayService holidayService;

    @InjectMocks
    private HolidayController holidayController;

    private MockMvc mockMvc;

    private Holiday holiday;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(holidayController).build();
        holiday = new Holiday("USA", "Independence Day", LocalDate.of(2025, 7, 4));
    }

    @Test
    void testGetHolidays() throws Exception {
        // Given
        when(holidayService.getHolidays("USA")).thenReturn(Arrays.asList(holiday));

        // When & Then
        mockMvc.perform(get("/api/holidays?country=USA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Independence Day"))
                .andExpect(jsonPath("$[0].country").value("USA"));
    }

    @Test
    void testAddHoliday() throws Exception {
        // Given
        HolidayDTO holidayDTO = new HolidayDTO("USA", "Independence Day", LocalDate.of(2025, 7, 4));
        when(holidayService.addHoliday(anyString(), anyString(), any(LocalDate.class))).thenReturn(holiday);

        // When & Then
        mockMvc.perform(post("/api/holidays")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(holidayDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Independence Day"))
                .andExpect(jsonPath("$.country").value("USA"));
    }

    @Test
    void testUpdateHoliday() throws Exception {
        // Given
        HolidayDTO holidayDTO = new HolidayDTO("USA", "Updated Independence Day", LocalDate.of(2025, 7, 4));
        when(holidayService.updateHoliday(eq(1L), anyString(), anyString(), any(LocalDate.class))).thenReturn(holiday);

        // When & Then
        mockMvc.perform(put("/api/holidays/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(holidayDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Independence Day"));
    }

    @Test
    void testDeleteHoliday() throws Exception {
        // Given
        doNothing().when(holidayService).deleteHoliday(1L);

        // When & Then
        mockMvc.perform(delete("/api/holidays/1"))
                .andExpect(status().isNoContent());
    }
}
