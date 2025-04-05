package ru.rsreu;

/**
 * 7. вычислить вероятность того, что броски кубика 10с10 со взрывами будет больше 80
 */
public class Runner {
    private static final double DEFAULT_EPS = 0.0025;
    private static final int DEFAULT_LOOPS_COUNT = 100;
    private static final int DEFAULT_BORDER_VALUE = 80;
    public static void main(String[] args) {
        Dice10d10RollCalculator diceCalculator = new Dice10d10RollCalculator(Runner.DEFAULT_EPS);
        System.out.println("EPS = " + DEFAULT_EPS);
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < Runner.DEFAULT_LOOPS_COUNT; i++) {
            double result = diceCalculator.getProbabilitySumBeMoreBorder(Runner.DEFAULT_BORDER_VALUE);
            System.out.printf("Result = %.5f\n", result);
        }
        long endTime = System.currentTimeMillis();
        System.out.printf("Execution time = %.3f ms\n", (endTime - startTime) / 1000.0);
    }
}
