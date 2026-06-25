package com.rewards.service;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class RewardCalculator {

    private static final BigDecimal LOWER_THRESHOLD = new BigDecimal("50");
    private static final BigDecimal UPPER_THRESHOLD = new BigDecimal("100");

    public int calculatePoints(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount must be non-negative.");
        }
        int points = 0;
        if (amount.compareTo(UPPER_THRESHOLD) > 0) {
            points += amount.subtract(UPPER_THRESHOLD).multiply(BigDecimal.valueOf(2)).intValue();
            points += UPPER_THRESHOLD.subtract(LOWER_THRESHOLD).intValue();
        } else if (amount.compareTo(LOWER_THRESHOLD) > 0) {
            points += amount.subtract(LOWER_THRESHOLD).intValue();
        }
        return points;
    }
}
