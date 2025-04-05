package ru.rsreu;

import java.io.IOException;

/**
 * 7. вычислить вероятность того, что броски кубика 10с10 со взрывами будет больше 80
 */
public class Runner {
    private static final double DEFAULT_EPS = 0.0003;
    private static final int DEFAULT_BORDER_VALUE = 80;
    public static void main(String[] args) {
        Dice10d10RollCalculator diceCalculator =
                new Dice10d10RollCalculator(Runner.DEFAULT_EPS, Runner.DEFAULT_BORDER_VALUE);
        System.out.println("EPS = " + DEFAULT_EPS);
    }
}
