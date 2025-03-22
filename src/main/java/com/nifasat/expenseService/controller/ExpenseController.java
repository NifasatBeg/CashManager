package com.nifasat.expenseService.controller;

import com.nifasat.expenseService.dto.ExpenseCountDTO;
import com.nifasat.expenseService.dto.ExpenseDto;
import com.nifasat.expenseService.dto.MerchantSummaryDTO;
import com.nifasat.expenseService.entity.Expense;
import com.nifasat.expenseService.service.ExpenseService;
import jakarta.websocket.server.PathParam;
import org.antlr.v4.runtime.misc.NotNull;
import org.hibernate.annotations.NotFound;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
import java.util.stream.Collectors;

@RestController
public class ExpenseController {
    @Autowired
    private  ExpenseService expenseService;

    @GetMapping("/expense/v1/all")
    public ResponseEntity<List<ExpenseDto>> getExpenses(@RequestParam("user_id") @NotNull String userId){
        try {
            List<ExpenseDto> expenseDtos = expenseService.getExpenses(userId);
            return new ResponseEntity<>(expenseDtos, HttpStatus.OK);
        } catch (Exception ex){
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }
    @GetMapping("/expense/v1/rangeBasedExpense")
    public ResponseEntity<List<ExpenseDto>> getRangeExpenses(@RequestParam("user_id") @NotNull String userId, @RequestParam("start_date") @NotNull
                                                             Long startDate, @RequestParam("end_date") @NotNull Long endDate){
        try {
            List<ExpenseDto> expenseDtos = expenseService.getTimeBasedExpense(userId, startDate, endDate);
            return new ResponseEntity<>(expenseDtos, HttpStatus.OK);
        } catch (Exception ex){
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/expense/v1/merchant/rangeBasedExpense")
    public ResponseEntity<List<ExpenseDto>> getMerchantRangeExpenses(@RequestParam("user_id") @NotNull String userId, @RequestParam("merchant") @NotNull String merchant, @RequestParam("start_date") @NotNull
    Long startDate, @RequestParam("end_date") @NotNull Long endDate){
        try {
            List<ExpenseDto> expenseDtos = expenseService.getTimeAndMerchantBasedExpense(userId, merchant, startDate, endDate);
            return new ResponseEntity<>(expenseDtos, HttpStatus.OK);
        } catch (Exception ex){
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping(path="/expense/v1/addExpense")
    public ResponseEntity<Boolean> addExpenses(@RequestHeader(value = "X-User-Id") @NotNull String userId, @RequestBody ExpenseDto
                                               expenseDto){
        try{
            expenseDto.setUserId(userId);
            return new ResponseEntity<>(expenseService.createExpense(expenseDto), HttpStatus.OK);
        }catch (Exception ex){
            return new ResponseEntity<>(false, HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping(path="/expense/v1/deleteExpense")
    public ResponseEntity<String> deleteExpense(@RequestParam("user_id") @NotNull String userId, @RequestBody ExpenseDto expenseDto){
        Integer done = expenseService.deleteExpense(userId, expenseDto);
        if(done>0){
            return ResponseEntity.ok("Deleted");
        }else{
            return ResponseEntity.badRequest().body("Error occured");
        }
    }

    @GetMapping("/expense/v1/count")
    public ResponseEntity<List<ExpenseCountDTO>> getExpenseCountsByTimeframe(
            @RequestParam("user_id") String userId,
            @RequestParam("time_frame") String timeframe,
            @RequestParam("start_date") Long startDate,
            @RequestParam("end_date") Long endDate) {

        Date startTime = new Date(startDate);
        Date endTime = new Date(endDate);

        List<ExpenseCountDTO> expenses =
                expenseService.getExpenseCountsByTimeframe(userId, timeframe, startTime, endTime);
        return Optional.ofNullable(expenses).map(expenseCountDTOS -> ResponseEntity.ok(expenseCountDTOS))
                .orElse(ResponseEntity.badRequest().build());
    }

    @GetMapping("/expense/v1/merchant-summary")
    public ResponseEntity<List<MerchantSummaryDTO>> getMerchantSummary(
            @RequestParam("user_id") String userId,
            @RequestParam("start_date") Long startDate,
            @RequestParam("end_date") Long endDate) {

        Date startTime = new Date(startDate);
        Date endTime = new Date(endDate);

        return ResponseEntity.ok(expenseService.getMerchantSummary(userId, startTime, endTime));
    }

    @GetMapping("/expense/v1/summary")
    public ResponseEntity<Map<String, Object>> getExpenseSummary(@RequestParam("user_id") @NotNull String userId) {

        try {
            return ResponseEntity.ok(expenseService.getSummary(userId));
        } catch (EmptyResultDataAccessException e){
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }
}
