package com.holiday.holidayapi.model;

import java.util.List;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Setter
@Getter
public class FileProcessingResult {
    private String fileName;
    private int totalRecords;
    private int processedRecords;
    private int failedRecords;
    private List<String> errorMessages;

    // Getters and Setters
}
