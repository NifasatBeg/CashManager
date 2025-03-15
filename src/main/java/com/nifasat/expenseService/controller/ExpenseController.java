package com.nifasat.expenseService.controller;

import com.nifasat.expenseService.dto.ExpenseDto;
import com.nifasat.expenseService.service.ExpenseService;
import jakarta.websocket.server.PathParam;
import org.antlr.v4.runtime.misc.NotNull;
import org.hibernate.annotations.NotFound;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

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
    @GetMapping("/expense/v1/range")
    public ResponseEntity<List<ExpenseDto>> getRangeExpenses(@RequestParam("user_id") @NotNull String userId, @RequestParam("start_date") @NotNull
                                                             Long startDate, @RequestParam("end_date") @NotNull Long endDate){
        try {
            List<ExpenseDto> expenseDtos = expenseService.getTimeBasedExpense(userId, startDate, endDate);
            return new ResponseEntity<>(expenseDtos, HttpStatus.OK);
        } catch (Exception ex){
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping(path="/addExpense")
    public ResponseEntity<Boolean> addExpenses(@RequestHeader(value = "X-User-Id") @NotNull String userId, @RequestBody ExpenseDto
                                               expenseDto){
        try{
            expenseDto.setUserId(userId);
            return new ResponseEntity<>(expenseService.createExpense(expenseDto), HttpStatus.OK);
        }catch (Exception ex){
            return new ResponseEntity<>(false, HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping(path="/deleteExpense")
    public ResponseEntity<String> deleteExpense(@RequestParam("user_id") @NotNull String userId, @RequestBody ExpenseDto expenseDto){
        Integer done = expenseService.deleteExpense(userId, expenseDto);
        if(done>0){
            return ResponseEntity.ok("Deleted");
        }else{
            return ResponseEntity.badRequest().body("Error occured");
        }
    }

}
