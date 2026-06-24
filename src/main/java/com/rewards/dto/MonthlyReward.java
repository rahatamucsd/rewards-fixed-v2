package com.rewards.dto;

import java.math.BigDecimal;

public class MonthlyReward {

    private int month;
    private int year;
    private BigDecimal totalAmount;
    private int rewardPoints;

    public MonthlyReward() {}

    public MonthlyReward(int month, int year, BigDecimal totalAmount, int rewardPoints) {
        this.month = month;
        this.year = year;
        this.totalAmount = totalAmount;
        this.rewardPoints = rewardPoints;
    }

    public int getMonth() { return month; }
    public void setMonth(int month) { this.month = month; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public int getRewardPoints() { return rewardPoints; }
    public void setRewardPoints(int rewardPoints) { this.rewardPoints = rewardPoints; }
}
