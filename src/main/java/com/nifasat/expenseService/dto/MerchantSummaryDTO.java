package com.nifasat.expenseService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MerchantSummaryDTO {
    private String merchant;
    private int count;
    private Double totalAmount;
}
