package com.nifasat.expenseService.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nifasat.expenseService.dto.ExpenseDto;
import com.nifasat.expenseService.entity.Expense;
import com.nifasat.expenseService.repository.ExpenseRepository;
import jakarta.transaction.Transactional;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class ExpenseService {
    @Autowired
    private ExpenseRepository expenseRepository;
    private ObjectMapper objectMapper = new ObjectMapper();
    public boolean createExpense(ExpenseDto expenseDto){
        setCurrency(expenseDto);
        try{
            expenseRepository.save(objectMapper.convertValue(expenseDto, Expense.class));
            return true;
        }catch(Exception ex){
            return false;
        }
    }
    public boolean updateExpense(ExpenseDto expenseDto){
        Optional<Expense> foundExpense = expenseRepository.findByUserIdAndExternalId(expenseDto.getUserId(), expenseDto.getExternalId());
        return foundExpense
                .map(expense -> {
                    expense.setCurrency(Strings.isNotBlank(expenseDto.getCurrency())? expenseDto.getCurrency(): expense.getCurrency());
                    expense.setMerchant(Strings.isNotBlank(expenseDto.getMerchant())? expenseDto.getMerchant(): expense.getMerchant());
                    expense.setAmount(expenseDto.getAmount());
                    expenseRepository.save(expense);
                    return true;
                }).orElse(false);
    }
    public List<ExpenseDto> getExpenses(String userId){
        List<Expense>expenseList = expenseRepository.findByUserId(userId);
        return objectMapper.convertValue(expenseList, new TypeReference<List<ExpenseDto>>() {});
    }
    public List<ExpenseDto> getTimeBasedExpense(String userId, Long startDateLong, Long endDateLong){
        Date startDate = new Date(startDateLong);
        Date endDate = new Date(endDateLong);
        List<Expense>expenseList = expenseRepository.findByUserIdAndCreatedAtBetween(userId, startDate, endDate);
        return objectMapper.convertValue(expenseList, new TypeReference<List<ExpenseDto>>() {});
    }
    private void setCurrency(ExpenseDto expenseDto){
        if(Objects.isNull(expenseDto.getCurrency())){
            expenseDto.setCurrency(("INR"));
        }
    }
    private void setCreatedAt(ExpenseDto expenseDto){
        if(Objects.isNull(expenseDto.getCreatedAt())){
            expenseDto.setCreatedAt(new Date());
            System.out.println(expenseDto);
        }
    }

    @Transactional
    public Integer deleteExpense(String userId,ExpenseDto expenseDto){
        try{
            return expenseRepository.deleteByExternalId(expenseDto.getExternalId());
        }catch (Exception ex){
            ex.printStackTrace();
            return 0;
        }
    }
}
