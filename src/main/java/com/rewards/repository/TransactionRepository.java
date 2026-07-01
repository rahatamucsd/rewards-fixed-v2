package com.rewards.repository;

import com.rewards.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, String> {

    @Query("SELECT MAX(t.transactionDate) FROM Transaction t WHERE t.customer.customerId = :customerId")
    Optional<LocalDate> findMaxDateByCustomerId(@Param("customerId") String customerId);

    @Query("SELECT t FROM Transaction t WHERE t.customer.customerId = :customerId AND t.transactionDate >= :cutoff")
    List<Transaction> findByCustomerIdFromDate(@Param("customerId") String customerId, @Param("cutoff") LocalDate cutoff);
}
