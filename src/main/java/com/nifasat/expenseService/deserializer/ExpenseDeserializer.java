package com.nifasat.expenseService.deserializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.nifasat.expenseService.dto.ExpenseDto;
import org.apache.kafka.common.serialization.Deserializer;

public class ExpenseDeserializer implements Deserializer<ExpenseDto> {  // Added generic type
    @Override
    public ExpenseDto deserialize(String topic, byte[] data) {
        ObjectMapper objectMapper = new ObjectMapper();
        ExpenseDto expenseDto = null;
        try {
            expenseDto = objectMapper.readValue(data, ExpenseDto.class);
            System.out.println("Deserialized ExpenseDto: " + objectMapper.writeValueAsString(expenseDto));  // Add debugging
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return expenseDto;
    }
}