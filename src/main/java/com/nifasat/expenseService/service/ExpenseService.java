package com.nifasat.expenseService.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nifasat.expenseService.dto.ExpenseCountDTO;
import com.nifasat.expenseService.dto.ExpenseDto;
import com.nifasat.expenseService.dto.MerchantSummaryDTO;
import com.nifasat.expenseService.entity.Expense;
import com.nifasat.expenseService.repository.ExpenseRepository;
import jakarta.transaction.Transactional;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

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

    public List<ExpenseDto> getTimeAndMerchantBasedExpense(String userId, String merchant, Long startDateLong, Long endDateLong){
        Date startDate = new Date(startDateLong);
        Date endDate = new Date(endDateLong);
        List<Expense>expenseList = expenseRepository.findByUserIdAndMerchantAndCreatedAtBetween(userId, merchant, startDate, endDate);
        return objectMapper.convertValue(expenseList, new TypeReference<List<ExpenseDto>>(){});
    }
    private void setCurrency(ExpenseDto expenseDto){
        if(Objects.isNull(expenseDto.getCurrency())){
            expenseDto.setCurrency(("INR"));
        }
    }

    public List<MerchantSummaryDTO> getMerchantSummary(String userId, Date startTime, Date endTime){
        List<Expense> expenses = expenseRepository.findByUserIdAndCreatedAtBetween(userId, startTime, endTime);

        // Group expenses by merchant
        Map<String, List<Expense>> groupedByMerchant = expenses.stream()
                .collect(Collectors.groupingBy(Expense::getMerchant));

        // Create summary DTOs
        List<MerchantSummaryDTO> merchantSummaries = groupedByMerchant.entrySet().stream()
                .map(entry -> {
                    String merchant = entry.getKey();
                    List<Expense> merchantExpenses = entry.getValue();
                    Double totalAmount = merchantExpenses.stream()
                            .mapToDouble(Expense::getAmount)
                            .sum();
                    int count = merchantExpenses.size();
                    return new MerchantSummaryDTO(merchant, count, totalAmount);
                })
                .sorted(Comparator.comparing(MerchantSummaryDTO::getTotalAmount).reversed())
                .collect(Collectors.toList());

        return merchantSummaries;
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

    public List<ExpenseCountDTO> getExpenseCountsByTimeframe(String userId, String timeframe, Date startTime, Date endTime){

        List<Expense> expenses = expenseRepository.findByUserIdAndCreatedAtBetween(userId, startTime, endTime);

        // Group expenses based on timeframe
        Map<String, List<Expense>> groupedExpenses = new HashMap<>();

        switch (timeframe.toLowerCase()) {
            case "day":
                groupedExpenses = expenses.stream()
                        .collect(Collectors.groupingBy(expense -> {
                            Date date = expense.getCreatedAt();
                            return date.toString();
                        }));
                break;
            case "month":
                groupedExpenses = expenses.stream()
                        .collect(Collectors.groupingBy(expense -> {
                            Date date = expense.getCreatedAt();
                            return date.getYear() + "-" + String.format("%02d", date.getMonth());
                        }));
                break;
            case "year":
                groupedExpenses = expenses.stream()
                        .collect(Collectors.groupingBy(expense -> {
                            Date date = expense.getCreatedAt();
                            return String.valueOf(date.getYear());
                        }));
                break;
            default:
                return null;
        }
        List<ExpenseCountDTO> result = groupedExpenses.entrySet().stream()
                .map(entry -> {
                    Double totalAmount = entry.getValue().stream()
                            .mapToDouble(Expense::getAmount)
                            .sum();
                    return new ExpenseCountDTO(entry.getKey(), entry.getValue().size(), totalAmount);
                })
                .sorted(Comparator.comparing(ExpenseCountDTO::getTimePeriod))
                .collect(Collectors.toList());

        return result;
    }
}
