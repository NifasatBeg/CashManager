package com.nifasat.expenseService.repository;

import com.nifasat.expenseService.entity.Expense;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExpenseRepository extends CrudRepository<Expense, Long> {
    List<Expense> findByUserId(String userId);
    List<Expense> findByUserIdAndCreatedAtBetween(String userId, Date startTime, Date endTime);
    List<Expense> findByUserIdAndMerchantAndCreatedAtBetween(String userId, String merchant, Date startTime, Date endTime);
    Optional<Expense> findByUserIdAndExternalId(String userId, String externalId);
    Integer deleteByExternalId(String externalId);
}
