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
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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

    public List<ExpenseCountDTO> getExpenseCountsByTimeframe(String userId, String timeframe, Date startTime, Date endTime) {
        List<Expense> expenses = expenseRepository.findByUserIdAndCreatedAtBetween(userId, startTime, endTime);

        // Group expenses based on timeframe
        Map<String, List<Expense>> groupedExpenses;

        switch (timeframe.toLowerCase()) {
            case "day":
                groupedExpenses = expenses.stream()
                        .collect(Collectors.groupingBy(expense -> formatDate(expense.getCreatedAt(), "yyyy-MM-dd")));
                break;
            case "week":
                groupedExpenses = expenses.stream()
                        .collect(Collectors.groupingBy(expense -> formatDate(expense.getCreatedAt(), "YYYY-'W'ww"))); // Year-Week format
                break;
            case "month":
                groupedExpenses = expenses.stream()
                        .collect(Collectors.groupingBy(expense -> formatDate(expense.getCreatedAt(), "yyyy-MM"))); // Proper year-month format
                break;
            case "year":
                groupedExpenses = expenses.stream()
                        .collect(Collectors.groupingBy(expense -> formatDate(expense.getCreatedAt(), "yyyy"))); // Just the year
                break;
            default:
                throw new IllegalArgumentException("Invalid timeframe: " + timeframe);
        }

        return groupedExpenses.entrySet().stream()
                .map(entry -> {
                    double totalAmount = entry.getValue().stream().mapToDouble(Expense::getAmount).sum();
                    return new ExpenseCountDTO(entry.getKey(), entry.getValue().size(), totalAmount);
                })
                .sorted(Comparator.comparing(ExpenseCountDTO::getTimePeriod))
                .collect(Collectors.toList());
    }

    // Utility function to convert Date to formatted String
    private String formatDate(Date date, String pattern) {
        return date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .format(DateTimeFormatter.ofPattern(pattern));
    }

    public Map<String, Object> getSummary(String userId) throws Exception{
        try {
            List<ExpenseDto> expenses = getExpenses(userId);
            if (expenses.isEmpty()) {
                throw new EmptyResultDataAccessException(0);
            }

            // Calculate largest expense
            ExpenseDto largestExpense = expenses.stream()
                    .max(Comparator.comparingDouble(ExpenseDto::getAmount))
                    .orElse(null);

            // Calculate average daily expense
            Map<String, Double> dailyExpenseMap = new HashMap<>();
            for (ExpenseDto expense : expenses) {
                String dateKey = new SimpleDateFormat("yyyy-MM-dd").format(expense.getCreatedAt());
                dailyExpenseMap.put(dateKey, dailyExpenseMap.getOrDefault(dateKey, 0.0) + expense.getAmount());
            }
            double averageDailyExpense = dailyExpenseMap.values().stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

            // Calculate top merchant by total spending
            Map<String, Double> merchantSpendMap = new HashMap<>();
            for (ExpenseDto expense : expenses) {
                merchantSpendMap.put(expense.getMerchant(), merchantSpendMap.getOrDefault(expense.getMerchant(), 0.0) + expense.getAmount());
            }
            String topMerchant = merchantSpendMap.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(null);

            // Prepare response
            Map<String, Object> response = new HashMap<>();
            response.put("largestExpense", largestExpense);
            response.put("averageDailyExpense", averageDailyExpense);
            response.put("topMerchant", topMerchant);

            return response;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

}
