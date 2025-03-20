package com.nifasat.expenseService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExpenseCountDTO {
    private String timePeriod; // Could be a date, month, or year depending on the filter
    private int count;
    private Double totalAmount;
}