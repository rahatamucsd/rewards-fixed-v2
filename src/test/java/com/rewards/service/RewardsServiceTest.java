package com.rewards.service;

import com.rewards.dto.CustomerRewardSummary;
import com.rewards.dto.MonthlyReward;
import com.rewards.entity.Customer;
import com.rewards.entity.Transaction;
import com.rewards.exception.CustomerNotFoundException;
import com.rewards.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RewardsServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    private RewardsService rewardsService;

    @BeforeEach
    void setUp() {
        rewardsService = new RewardsService(transactionRepository, new RewardCalculator());
    }

    

    private static final LocalDate ALICE_MAX  = LocalDate.of(2024, 3, 25);
    private static final LocalDate BOB_MAX    = LocalDate.of(2024, 3, 29);
    private static final LocalDate CAROL_MAX  = LocalDate.of(2024, 3, 30);
    private static final LocalDate DAVID_MAX  = LocalDate.of(2024, 3, 20);
    private static final LocalDate CUTOFF_3M  = LocalDate.of(2024, 1,  1); // months=3 from any March-2024 max

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

    private static final List<Transaction> ALICE_MARCH = Arrays.asList(
        txn("T007","C001","Alice Johnson", 2024,3,10, "175.00"),
        txn("T008","C001","Alice Johnson", 2024,3,25,  "60.00")
    );

    private static final List<Transaction> BOB = Arrays.asList(
        txn("T009","C002","Bob Martinez",  2024,1,8,   "55.00"),
        txn("T010","C002","Bob Martinez",  2024,1,20, "310.00"),
        txn("T011","C002","Bob Martinez",  2024,2,5,   "95.00"),
        txn("T012","C002","Bob Martinez",  2024,2,19,  "40.00"),
        txn("T013","C002","Bob Martinez",  2024,2,28, "150.00"),
        txn("T014","C002","Bob Martinez",  2024,3,7,  "220.00"),
        txn("T015","C002","Bob Martinez",  2024,3,15,  "80.00"),
        txn("T016","C002","Bob Martinez",  2024,3,29, "100.00")
    );

    private static final List<Transaction> CAROL = Arrays.asList(
        txn("T017","C003","Carol Smith",   2024,1,12,  "30.00"),
        txn("T018","C003","Carol Smith",   2024,1,24, "110.00"),
        txn("T019","C003","Carol Smith",   2024,2,8,  "250.00"),
        txn("T020","C003","Carol Smith",   2024,2,17,  "65.00"),
        txn("T021","C003","Carol Smith",   2024,3,3,   "90.00"),
        txn("T022","C003","Carol Smith",   2024,3,22, "140.00"),
        txn("T023","C003","Carol Smith",   2024,3,30,  "50.00")
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
        return new Transaction(id, new Customer(custId, name),
                LocalDate.of(y, m, d), new BigDecimal(amount));
    }

    private void stubCustomer(String id, LocalDate max, List<Transaction> rows) {
        when(transactionRepository.findMaxDateByCustomerId(id)).thenReturn(Optional.of(max));
        when(transactionRepository.findByCustomerIdFromDate(id, CUTOFF_3M)).thenReturn(rows);
    }

    

    @Test
    void alice_januaryPoints_shouldBe365() {
        stubCustomer("C001", ALICE_MAX, ALICE);
        int jan = rewardsService.getCustomerRewards("C001", 3).getMonthlyRewards().stream()
                .filter(m -> m.getMonth() == 1 && m.getYear() == 2024)
                .mapToInt(MonthlyReward::getRewardPoints).sum();
        assertEquals(365, jan);
    }

    @Test
    void alice_februaryPoints_shouldBe148() {
        stubCustomer("C001", ALICE_MAX, ALICE);
        int feb = rewardsService.getCustomerRewards("C001", 3).getMonthlyRewards().stream()
                .filter(m -> m.getMonth() == 2 && m.getYear() == 2024)
                .mapToInt(MonthlyReward::getRewardPoints).sum();
        assertEquals(148, feb);
    }

    @Test
    void alice_marchPoints_shouldBe210() {
        stubCustomer("C001", ALICE_MAX, ALICE);
        int mar = rewardsService.getCustomerRewards("C001", 3).getMonthlyRewards().stream()
                .filter(m -> m.getMonth() == 3 && m.getYear() == 2024)
                .mapToInt(MonthlyReward::getRewardPoints).sum();
        assertEquals(210, mar);
    }

    @Test
    void alice_totalRewardPoints_shouldBe723() {
        stubCustomer("C001", ALICE_MAX, ALICE);
        assertEquals(723, rewardsService.getCustomerRewards("C001", 3).getTotalRewardPoints());
    }

    

    @Test
    void bob_januaryPoints_shouldBe475() {
        stubCustomer("C002", BOB_MAX, BOB);
        int jan = rewardsService.getCustomerRewards("C002", 3).getMonthlyRewards().stream()
                .filter(m -> m.getMonth() == 1)
                .mapToInt(MonthlyReward::getRewardPoints).sum();
        assertEquals(475, jan);
    }

    @Test
    void bob_februaryPoints_shouldBe195() {
        stubCustomer("C002", BOB_MAX, BOB);
        int feb = rewardsService.getCustomerRewards("C002", 3).getMonthlyRewards().stream()
                .filter(m -> m.getMonth() == 2)
                .mapToInt(MonthlyReward::getRewardPoints).sum();
        assertEquals(195, feb);
    }

    @Test
    void bob_marchPoints_shouldBe370() {
        stubCustomer("C002", BOB_MAX, BOB);
        int mar = rewardsService.getCustomerRewards("C002", 3).getMonthlyRewards().stream()
                .filter(m -> m.getMonth() == 3)
                .mapToInt(MonthlyReward::getRewardPoints).sum();
        assertEquals(370, mar);
    }

    @Test
    void bob_totalRewardPoints_shouldBe1040() {
        stubCustomer("C002", BOB_MAX, BOB);
        assertEquals(1040, rewardsService.getCustomerRewards("C002", 3).getTotalRewardPoints());
    }

    

    @Test
    void carol_januaryPoints_shouldBe70() {
        stubCustomer("C003", CAROL_MAX, CAROL);
        int jan = rewardsService.getCustomerRewards("C003", 3).getMonthlyRewards().stream()
                .filter(m -> m.getMonth() == 1)
                .mapToInt(MonthlyReward::getRewardPoints).sum();
        assertEquals(70, jan);
    }

    @Test
    void carol_februaryPoints_shouldBe365() {
        stubCustomer("C003", CAROL_MAX, CAROL);
        int feb = rewardsService.getCustomerRewards("C003", 3).getMonthlyRewards().stream()
                .filter(m -> m.getMonth() == 2)
                .mapToInt(MonthlyReward::getRewardPoints).sum();
        assertEquals(365, feb);
    }

    @Test
    void carol_marchPoints_shouldBe170() {
        stubCustomer("C003", CAROL_MAX, CAROL);
        int mar = rewardsService.getCustomerRewards("C003", 3).getMonthlyRewards().stream()
                .filter(m -> m.getMonth() == 3)
                .mapToInt(MonthlyReward::getRewardPoints).sum();
        assertEquals(170, mar);
    }

    @Test
    void carol_totalRewardPoints_shouldBe605() {
        stubCustomer("C003", CAROL_MAX, CAROL);
        assertEquals(605, rewardsService.getCustomerRewards("C003", 3).getTotalRewardPoints());
    }

    

    @Test
    void david_januaryPoints_shouldBe850() {
        stubCustomer("C004", DAVID_MAX, DAVID);
        int jan = rewardsService.getCustomerRewards("C004", 3).getMonthlyRewards().stream()
                .filter(m -> m.getMonth() == 1)
                .mapToInt(MonthlyReward::getRewardPoints).sum();
        assertEquals(850, jan);
    }

    

    @Test
    void months_1_shouldReturnOnlyMostRecentMonth() {
        LocalDate cutoff = LocalDate.of(2024, 3, 1);
        when(transactionRepository.findMaxDateByCustomerId("C001")).thenReturn(Optional.of(ALICE_MAX));
        when(transactionRepository.findByCustomerIdFromDate("C001", cutoff)).thenReturn(ALICE_MARCH);
        CustomerRewardSummary alice = rewardsService.getCustomerRewards("C001", 1);
        assertEquals(1, alice.getMonthlyRewards().size());
        assertEquals(3, alice.getMonthlyRewards().get(0).getMonth());
    }

    

    @Test
    void unknownCustomer_shouldThrowCustomerNotFoundException() {
        when(transactionRepository.findMaxDateByCustomerId("UNKNOWN")).thenReturn(Optional.empty());
        assertThrows(CustomerNotFoundException.class,
                () -> rewardsService.getCustomerRewards("UNKNOWN", 3));
    }
}
