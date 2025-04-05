package ru.rsreu;

/**
 * Calculator for rolls for 10d10 dice
 */
public class Dice10d10RollCalculator extends Thread {
    /**
     * Default rolls count
     */
    public static final int ROLLS_COUNT = 10;

    /**
     * Sided count for d10 dice
     */
    public static final int SIDES_COUNT = 10;

    /**
     * Dice for calculations
     */
    private final Dice dice = new Dice(Dice10d10RollCalculator.SIDES_COUNT);

    /**
     * Error rate for calculations
     */
    private final double eps;

    /**
     * Lower border value for sum of points
     */
    private final int borderValue;

    /**
     * Constructs calculator with preferred eps
     *
     * @param eps preferred maximal error rate for calculations
     */
    public Dice10d10RollCalculator(double eps, int borderValue) {
        this.eps = checkAndCorrectEps(eps);
        this.borderValue = borderValue;
    }

    /**
     * Gets probability that sum after all rolls will be more than borderValue
     *
     * @return probability that sum after all rolls will be more than borderValue
     */
    public double calculateProbabilitySumBeMoreBorder() throws InterruptedException {
        long counter = 0;
        long n = (long) Math.ceil(Math.pow((1.96 * 0.95) / eps, 2.0)); // confidence interval 95%
        for (long i = 0; i < n; i++) {
            if (getSumAfterNRollsWithExplosions(Dice10d10RollCalculator.ROLLS_COUNT) > this.borderValue) {
                counter++;
            }
            if (interrupted()) {
                throw new InterruptedException();
            }
        }
        return counter / (double) n;
    }

    /**
     * Gets sum value after several rolls (can be explosions additionally)
     *
     * @param rollsCount count of rolls for die
     * @return sum value after several rolls of die
     */
    private double getSumAfterNRollsWithExplosions(int rollsCount) {
        long sum = 0;
        for (int i = 0; i < rollsCount; i++) {
            sum += getRollSumWithExplosion();
        }
        return sum;
    }

    /**
     * Gets sum value after one roll (can be explosions additionally)
     *
     * @return sum value after one roll with explosions
     */
    private long getRollSumWithExplosion() {
        long resultValue = 0;
        int currentValue = 0;
        do {
            currentValue = dice.getValueAfterRoll();
            resultValue += currentValue;
        } while (currentValue == dice.getSidesNumber());
        return resultValue;
    }

    /**
     * Checks and correct eps if is needed
     * @param eps maximal error rate
     * @return checked eps
     */
    private static double checkAndCorrectEps(double eps) {
        if (eps <= 0) {
            eps = 1e-5;
        }
        return eps;
    }

    @Override
    public void run() {
        try {
            long startTime = System.currentTimeMillis();
            double result = calculateProbabilitySumBeMoreBorder();
            long endTime = System.currentTimeMillis();
            System.out.printf("Task[%s]: result = %.5f; execution time = %.3f ms\n",
                    getName(), result, (endTime - startTime) / 1000.0);
        } catch (InterruptedException e) {
            System.out.printf("Task[%s] is interrupted!\n", getName());
        }
    }

}
