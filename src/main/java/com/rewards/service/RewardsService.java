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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RewardsService {

    private static final BigDecimal LOWER_THRESHOLD = new BigDecimal("50");
    private static final BigDecimal UPPER_THRESHOLD = new BigDecimal("100");
    private static final int LOWER_MULTIPLIER = 1;
    private static final int UPPER_MULTIPLIER = 2;

    private final TransactionRepository transactionRepository;

    @Autowired
    public RewardsService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public CustomerRewardSummary getCustomerRewards(String customerId, int months) {
        List<Transaction> transactions = transactionRepository.findByCustomerId(customerId);
        List<Transaction> filtered = filterByMonths(transactions, months);

        if (filtered.isEmpty()) {
            throw new CustomerNotFoundException(customerId);
        }
        return buildSummaries(filtered).get(0);
    }

    public int calculatePoints(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount must be non-negative.");
        }
        int points = 0;
        if (amount.compareTo(UPPER_THRESHOLD) > 0) {
            points += amount.subtract(UPPER_THRESHOLD)
                           .multiply(BigDecimal.valueOf(UPPER_MULTIPLIER))
                           .intValue();
            points += UPPER_THRESHOLD.subtract(LOWER_THRESHOLD)
                                     .multiply(BigDecimal.valueOf(LOWER_MULTIPLIER))
                                     .intValue();
        } else if (amount.compareTo(LOWER_THRESHOLD) > 0) {
            points += amount.subtract(LOWER_THRESHOLD)
                           .multiply(BigDecimal.valueOf(LOWER_MULTIPLIER))
                           .intValue();
        }
        return points;
    }

    /**
     * Keeps transactions within the most recent {@code months} calendar months,
     * anchored to the latest transaction date in the provided list rather than
     * the current wall-clock date. This ensures the API returns correct results
     * regardless of when it is run relative to the dataset's date range.
     */
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

    private List<CustomerRewardSummary> buildSummaries(List<Transaction> transactions) {
        Map<String, List<Transaction>> byCustomer = transactions.stream()
                .collect(Collectors.groupingBy(Transaction::getCustomerId));

        List<CustomerRewardSummary> summaries = new ArrayList<>();

        for (Map.Entry<String, List<Transaction>> entry : byCustomer.entrySet()) {
            List<Transaction> txns = entry.getValue();
            String customerName = txns.get(0).getCustomerName();

            Map<String, List<Transaction>> byMonth = txns.stream()
                    .collect(Collectors.groupingBy(t ->
                            t.getTransactionDate().getYear() + "-" +
                            String.format("%02d", t.getTransactionDate().getMonthValue())));

            List<MonthlyReward> monthlyRewards = byMonth.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .map(e -> {
                        String[] parts = e.getKey().split("-");
                        int year  = Integer.parseInt(parts[0]);
                        int month = Integer.parseInt(parts[1]);

                        BigDecimal totalAmount = e.getValue().stream()
                                .map(Transaction::getAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                        int rewardPoints = e.getValue().stream()
                                .mapToInt(t -> calculatePoints(t.getAmount()))
                                .sum();

                        return new MonthlyReward(month, year, totalAmount, rewardPoints);
                    })
                    .collect(Collectors.toList());

            int totalRewardPoints = monthlyRewards.stream()
                    .mapToInt(MonthlyReward::getRewardPoints)
                    .sum();

            summaries.add(new CustomerRewardSummary(
                    entry.getKey(), customerName, monthlyRewards, totalRewardPoints));
        }

        summaries.sort(Comparator.comparing(CustomerRewardSummary::getCustomerId));
        return summaries;
    }
}
