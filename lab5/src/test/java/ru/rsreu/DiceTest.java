package ru.rsreu;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;

import java.util.Random;

public class DiceTest {
    private final Random rand = new Random();
    private Dice dice;

    @BeforeEach
    public void getNewGameBone() {
        int min = 4;
        int max = 20;
        dice = new Dice(rand.nextInt(max - min + 1) + min);
    }

    @RepeatedTest(50)
    public void valueAfterRollShouldBeNonNegative() {
        Assertions.assertTrue(dice.getValueAfterRoll() > 0);
    }

    @RepeatedTest(50)
    public void valueAfterRollShouldNotBeMoreThanSidesNumber() {
        Assertions.assertTrue(dice.getValueAfterRoll() <= dice.getSidesNumber());
    }
}
