package com.nifasat.expenseService.consumer;

import com.nifasat.expenseService.dto.ExpenseDto;
import com.nifasat.expenseService.service.ExpenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class ExpenseConsumer {
    @Autowired
    private ExpenseService expenseService;

    @KafkaListener(topics="${spring.kafka.topic.name}", groupId = "${spring.kafka.consumer.group-id}")
    public void listener(ExpenseDto expenseDto){
        try{
            expenseService.createExpense(expenseDto);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

}
