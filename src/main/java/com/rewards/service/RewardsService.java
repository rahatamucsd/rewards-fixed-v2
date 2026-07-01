package com.rewards.service;

import com.rewards.dto.CustomerRewardSummary;
import com.rewards.dto.MonthlyReward;
import com.rewards.entity.Transaction;
import com.rewards.exception.CustomerNotFoundException;
import com.rewards.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RewardsService {

    private static final Logger log = LoggerFactory.getLogger(RewardsService.class);

    private final TransactionRepository transactionRepository;
    private final RewardCalculator rewardCalculator;

    @Autowired
    public RewardsService(TransactionRepository transactionRepository, RewardCalculator rewardCalculator) {
        this.transactionRepository = transactionRepository;
        this.rewardCalculator = rewardCalculator;
    }

    public CustomerRewardSummary getCustomerRewards(String customerId, int months) {
        log.debug("Resolving rewards for customerId={} months={}", customerId, months);
        // Anchor to the latest transaction date so results are stable for this dataset.
        LocalDate latest = transactionRepository.findMaxDateByCustomerId(customerId)
                .orElseThrow(() -> {
                    log.warn("No transactions found for customerId={}", customerId);
                    return new CustomerNotFoundException(customerId);
                });
        LocalDate cutoff = latest.withDayOfMonth(1).minusMonths(months - 1);
        log.debug("Date window for customerId={}: {} to {}", customerId, cutoff, latest);
        List<Transaction> transactions = transactionRepository.findByCustomerIdFromDate(customerId, cutoff);
        log.debug("Fetched {} transactions for customerId={}", transactions.size(), customerId);
        return buildSummary(customerId, transactions);
    }

    private CustomerRewardSummary buildSummary(String customerId, List<Transaction> txns) {
        String customerName = txns.get(0).getCustomer().getCustomerName();

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
