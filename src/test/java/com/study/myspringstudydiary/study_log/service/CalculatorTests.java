package com.study.myspringstudydiary.study_log.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class CalculatorTests {
    @Test
    public void add() {
        Calculator calculator = new Calculator();

        int actual = calculator.add(2, 3);
        int expected = 5;

        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void add_2() {
        Calculator calculator = new Calculator();
        int actual = calculator.add(3, 3);
        int expected = 6;

        Assertions.assertThat(actual).isEqualTo(expected);
    }
}
