package com.rewards.service;

import com.rewards.dto.CustomerRewardSummary;
import com.rewards.dto.MonthlyReward;
import com.rewards.entity.Transaction;
import com.rewards.exception.CustomerNotFoundException;
import com.rewards.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RewardsServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private RewardsService rewardsService;

    // ── Test data ─────────────────────────────────────────────────────────────

    private static final List<Transaction> ALICE = Arrays.asList(
        txn("T001","C001","Alice Johnson", 2024,1,5,  "120.00"),
        txn("T002","C001","Alice Johnson", 2024,1,18,  "75.00"),
        txn("T003","C001","Alice Johnson", 2024,1,27, "200.00"),
        txn("T004","C001","Alice Johnson", 2024,2,3,   "45.00"),
        txn("T005","C001","Alice Johnson", 2024,2,14, "130.00"),
        txn("T006","C001","Alice Johnson", 2024,2,22,  "88.00"),
        txn("T007","C001","Alice Johnson", 2024,3,10, "175.00"),
        txn("T008","C001","Alice Johnson", 2024,3,25,  "60.00")
    );

    private static final List<Transaction> DAVID = Arrays.asList(
        txn("T024","C004","David Lee",     2024,1,6,  "500.00"),
        txn("T025","C004","David Lee",     2024,2,11,  "75.00"),
        txn("T026","C004","David Lee",     2024,2,26, "115.00"),
        txn("T027","C004","David Lee",     2024,3,14,  "95.00"),
        txn("T028","C004","David Lee",     2024,3,20, "160.00")
    );

    private static Transaction txn(String id, String custId, String name,
                                    int y, int m, int d, String amount) {
        return new Transaction(id, custId, name,
                LocalDate.of(y, m, d), new BigDecimal(amount));
    }

    // ── Points: boundary below lower threshold ────────────────────────────────

    @Test
    void points_49_99_shouldBeZero() {
        assertEquals(0, rewardsService.calculatePoints(new BigDecimal("49.99")));
    }

    @Test
    void points_exactlyFifty_shouldBeZero() {
        assertEquals(0, rewardsService.calculatePoints(new BigDecimal("50.00")));
    }

    // ── Points: boundary above lower threshold ────────────────────────────────

    @Test
    void points_50_01_shouldBeZero() {
        assertEquals(0, rewardsService.calculatePoints(new BigDecimal("50.01")));
    }

    @Test
    void points_51_shouldBeOne() {
        assertEquals(1, rewardsService.calculatePoints(new BigDecimal("51.00")));
    }

    @Test
    void points_75_shouldBe25() {
        assertEquals(25, rewardsService.calculatePoints(new BigDecimal("75.00")));
    }

    // ── Points: boundary at upper threshold ───────────────────────────────────

    @Test
    void points_exactlyHundred_shouldBe50() {
        assertEquals(50, rewardsService.calculatePoints(new BigDecimal("100.00")));
    }

    @Test
    void points_100_01_shouldBe50() {
        assertEquals(50, rewardsService.calculatePoints(new BigDecimal("100.01")));
    }

    @Test
    void points_101_shouldBe52() {
        assertEquals(52, rewardsService.calculatePoints(new BigDecimal("101.00")));
    }

    // ── Points: standard examples ─────────────────────────────────────────────

    @Test
    void points_120_shouldBe90() {
        assertEquals(90, rewardsService.calculatePoints(new BigDecimal("120.00")));
    }

    @Test
    void points_200_shouldBe250() {
        assertEquals(250, rewardsService.calculatePoints(new BigDecimal("200.00")));
    }

    @Test
    void points_500_shouldBe850() {
        assertEquals(850, rewardsService.calculatePoints(new BigDecimal("500.00")));
    }

    // ── Points: edge cases ────────────────────────────────────────────────────

    @Test
    void points_zero_shouldBeZero() {
        assertEquals(0, rewardsService.calculatePoints(BigDecimal.ZERO));
    }

    @Test
    void points_negative_shouldThrowException() {
        assertThrows(IllegalArgumentException.class,
                () -> rewardsService.calculatePoints(new BigDecimal("-1.00")));
    }

    @Test
    void points_null_shouldThrowException() {
        assertThrows(IllegalArgumentException.class,
                () -> rewardsService.calculatePoints(null));
    }

    // ── Alice Johnson (C001) monthly breakdown ────────────────────────────────

    @Test
    void alice_januaryPoints_shouldBe365() {
        when(transactionRepository.findByCustomerId("C001")).thenReturn(ALICE);
        CustomerRewardSummary alice = rewardsService.getCustomerRewards("C001", 3);
        int jan = alice.getMonthlyRewards().stream()
                .filter(m -> m.getMonth() == 1 && m.getYear() == 2024)
                .mapToInt(MonthlyReward::getRewardPoints).sum();
        assertEquals(365, jan);
    }

    @Test
    void alice_februaryPoints_shouldBe148() {
        when(transactionRepository.findByCustomerId("C001")).thenReturn(ALICE);
        CustomerRewardSummary alice = rewardsService.getCustomerRewards("C001", 3);
        int feb = alice.getMonthlyRewards().stream()
                .filter(m -> m.getMonth() == 2 && m.getYear() == 2024)
                .mapToInt(MonthlyReward::getRewardPoints).sum();
        assertEquals(148, feb);
    }

    @Test
    void alice_marchPoints_shouldBe210() {
        when(transactionRepository.findByCustomerId("C001")).thenReturn(ALICE);
        CustomerRewardSummary alice = rewardsService.getCustomerRewards("C001", 3);
        int mar = alice.getMonthlyRewards().stream()
                .filter(m -> m.getMonth() == 3 && m.getYear() == 2024)
                .mapToInt(MonthlyReward::getRewardPoints).sum();
        assertEquals(210, mar);
    }

    @Test
    void alice_totalRewardPoints_shouldBe723() {
        when(transactionRepository.findByCustomerId("C001")).thenReturn(ALICE);
        assertEquals(723, rewardsService.getCustomerRewards("C001", 3).getTotalRewardPoints());
    }

    // ── David Lee (C004) ──────────────────────────────────────────────────────

    @Test
    void david_januaryPoints_shouldBe850() {
        when(transactionRepository.findByCustomerId("C004")).thenReturn(DAVID);
        CustomerRewardSummary david = rewardsService.getCustomerRewards("C004", 3);
        int jan = david.getMonthlyRewards().stream()
                .filter(m -> m.getMonth() == 1)
                .mapToInt(MonthlyReward::getRewardPoints).sum();
        assertEquals(850, jan);
    }

    // ── months filter ─────────────────────────────────────────────────────────

    @Test
    void months_1_shouldReturnOnlyMostRecentMonth() {
        when(transactionRepository.findByCustomerId("C001")).thenReturn(ALICE);
        CustomerRewardSummary alice = rewardsService.getCustomerRewards("C001", 1);
        assertEquals(1, alice.getMonthlyRewards().size());
        assertEquals(3, alice.getMonthlyRewards().get(0).getMonth());
    }

    // ── Not found ─────────────────────────────────────────────────────────────

    @Test
    void unknownCustomer_shouldThrowCustomerNotFoundException() {
        when(transactionRepository.findByCustomerId("UNKNOWN"))
                .thenReturn(Collections.emptyList());
        assertThrows(CustomerNotFoundException.class,
                () -> rewardsService.getCustomerRewards("UNKNOWN", 3));
    }

}
