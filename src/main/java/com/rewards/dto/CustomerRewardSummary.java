package com.rewards.dto;

import java.util.List;

public class CustomerRewardSummary {

    private String customerId;
    private String customerName;
    private List<MonthlyReward> monthlyRewards;
    private int totalRewardPoints;

    public CustomerRewardSummary() {}

    public CustomerRewardSummary(String customerId, String customerName,
                                  List<MonthlyReward> monthlyRewards, int totalRewardPoints) {
        this.customerId = customerId;
        this.customerName = customerName;
        this.monthlyRewards = monthlyRewards;
        this.totalRewardPoints = totalRewardPoints;
    }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public List<MonthlyReward> getMonthlyRewards() { return monthlyRewards; }
    public void setMonthlyRewards(List<MonthlyReward> monthlyRewards) { this.monthlyRewards = monthlyRewards; }

    public int getTotalRewardPoints() { return totalRewardPoints; }
    public void setTotalRewardPoints(int totalRewardPoints) { this.totalRewardPoints = totalRewardPoints; }
}
