package com.rewards.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class RewardCalculatorTest {

    private final RewardCalculator calculator = new RewardCalculator();

    

    @Test
    void points_49_99_shouldBeZero() {
        assertEquals(0, calculator.calculatePoints(new BigDecimal("49.99")));
    }

    @Test
    void points_exactlyFifty_shouldBeZero() {
        assertEquals(0, calculator.calculatePoints(new BigDecimal("50.00")));
    }

    

    @Test
    void points_50_01_shouldBeZero() {
        assertEquals(0, calculator.calculatePoints(new BigDecimal("50.01")));
    }

    @Test
    void points_51_shouldBeOne() {
        assertEquals(1, calculator.calculatePoints(new BigDecimal("51.00")));
    }

    @Test
    void points_75_shouldBe25() {
        assertEquals(25, calculator.calculatePoints(new BigDecimal("75.00")));
    }

    

    @Test
    void points_exactlyHundred_shouldBe50() {
        assertEquals(50, calculator.calculatePoints(new BigDecimal("100.00")));
    }

    @Test
    void points_100_01_shouldBe50() {
        assertEquals(50, calculator.calculatePoints(new BigDecimal("100.01")));
    }

    @Test
    void points_101_shouldBe52() {
        assertEquals(52, calculator.calculatePoints(new BigDecimal("101.00")));
    }

    

    @Test
    void points_120_shouldBe90() {
        assertEquals(90, calculator.calculatePoints(new BigDecimal("120.00")));
    }

    @Test
    void points_200_shouldBe250() {
        assertEquals(250, calculator.calculatePoints(new BigDecimal("200.00")));
    }

    @Test
    void points_500_shouldBe850() {
        assertEquals(850, calculator.calculatePoints(new BigDecimal("500.00")));
    }

    

    @Test
    void points_zero_shouldBeZero() {
        assertEquals(0, calculator.calculatePoints(BigDecimal.ZERO));
    }

    @Test
    void points_negative_shouldThrowException() {
        assertThrows(IllegalArgumentException.class,
                () -> calculator.calculatePoints(new BigDecimal("-1.00")));
    }

    @Test
    void points_null_shouldThrowException() {
        assertThrows(IllegalArgumentException.class,
                () -> calculator.calculatePoints(null));
    }
}
