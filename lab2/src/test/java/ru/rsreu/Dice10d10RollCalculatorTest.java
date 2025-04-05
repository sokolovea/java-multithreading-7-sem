package ru.rsreu;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.RepeatedTest;

class Dice10d10RollCalculatorTest {
    private static final double DEFAULT_EPS = 0.05;
    private static final int DEFAULT_BORDER_VALUE = 80;

    @RepeatedTest(50)
    public void valueShouldBeWithoutTooBigEps() {
        Dice10d10RollCalculator calculator = new Dice10d10RollCalculator(Dice10d10RollCalculatorTest.DEFAULT_EPS);
        double firstProbability =
                calculator.getProbabilitySumBeMoreBorder(Dice10d10RollCalculatorTest.DEFAULT_BORDER_VALUE);
        double secondProbability =
                calculator.getProbabilitySumBeMoreBorder(Dice10d10RollCalculatorTest.DEFAULT_BORDER_VALUE);
        double averageProbability = (firstProbability + secondProbability) / 2;
        Assertions.assertEquals(averageProbability, firstProbability, DEFAULT_EPS);
        Assertions.assertEquals(averageProbability, secondProbability, DEFAULT_EPS);
    }

}