package com.rewards.service;

import com.rewards.dto.CustomerRewardSummary;
import com.rewards.dto.MonthlyReward;
import com.rewards.entity.Transaction;
import com.rewards.exception.CustomerNotFoundException;
import com.rewards.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RewardsService {

    private final TransactionRepository transactionRepository;
    private final RewardCalculator rewardCalculator;

    @Autowired
    public RewardsService(TransactionRepository transactionRepository, RewardCalculator rewardCalculator) {
        this.transactionRepository = transactionRepository;
        this.rewardCalculator = rewardCalculator;
    }

    public CustomerRewardSummary getCustomerRewards(String customerId, int months) {
        List<Transaction> transactions = transactionRepository.findByCustomerId(customerId);
        List<Transaction> filtered = filterByMonths(transactions, months);
        if (filtered.isEmpty()) {
            throw new CustomerNotFoundException(customerId);
        }
        return buildSummary(customerId, filtered);
    }

    // Anchored to the latest transaction date, not wall-clock, so results are
    // stable regardless of when the API is called relative to the dataset's range.
    private List<Transaction> filterByMonths(List<Transaction> all, int months) {
        if (all.isEmpty()) return all;
        LocalDate latestDate = all.stream()
                .map(Transaction::getTransactionDate)
                .max(Comparator.naturalOrder())
                .orElse(LocalDate.now());
        LocalDate cutoff = latestDate.withDayOfMonth(1).minusMonths(months - 1);
        return all.stream()
                .filter(t -> !t.getTransactionDate().isBefore(cutoff))
                .collect(Collectors.toList());
    }

    private CustomerRewardSummary buildSummary(String customerId, List<Transaction> txns) {
        String customerName = txns.get(0).getCustomerName();

        List<MonthlyReward> monthlyRewards = txns.stream()
                .collect(Collectors.groupingBy(t -> YearMonth.from(t.getTransactionDate())))
                .entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> toMonthlyReward(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        int totalRewardPoints = monthlyRewards.stream()
                .mapToInt(MonthlyReward::getRewardPoints)
                .sum();

        return new CustomerRewardSummary(customerId, customerName, monthlyRewards, totalRewardPoints);
    }

    private MonthlyReward toMonthlyReward(YearMonth ym, List<Transaction> txns) {
        BigDecimal totalAmount = txns.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int rewardPoints = txns.stream()
                .mapToInt(t -> rewardCalculator.calculatePoints(t.getAmount()))
                .sum();
        return new MonthlyReward(ym.getMonthValue(), ym.getYear(), totalAmount, rewardPoints);
    }
}
