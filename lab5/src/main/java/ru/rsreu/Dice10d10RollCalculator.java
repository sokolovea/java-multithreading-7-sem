package ru.rsreu;

import ru.rsreu.storage.StorageFactory;
import ru.rsreu.storage.StorageSingleton;

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
     * Rolls count for calculations
     */
    private final long n;

    /**
     * Lower border value for sum of points
     */
    private final int borderValue;

    /**
     * Storage for setting progress and result calculation
     */
    private final StorageSingleton storageInitOnDemand = StorageFactory.getStorageSingleton();

    /**
     * Lock for printing some information
     */
    private static final Object PRINT_LOCK = new Object();

    /**
     * Constructs calculator with preferred eps
     *
     * @param n preferred maximal error rate for calculations
     */
    public Dice10d10RollCalculator(long n, int borderValue) {
        this.n = n;
        this.borderValue = borderValue;
    }

    /**
     * Gets probability that sum after all rolls will be more than borderValue
     *
     * @return probability that sum after all rolls will be more than borderValue
     */
    public double calculateProbabilitySumBeMoreBorder() throws InterruptedException {
        long counter = 0;
        if (n <= 0) {
            return 0.0;
        }
        long step = this.n <= 10?  1 : this.n <= 10000 ? 10 : 100;
        for (long i = 1; i <= this.n; i++) {
            if (getSumAfterNRollsWithExplosions(Dice10d10RollCalculator.ROLLS_COUNT) > this.borderValue) {
                counter++;
            }
            if (interrupted()) {
                throw new InterruptedException();
            }
        }
        storageInitOnDemand.getStorage().updateCurrentIterationsCount(-(step - this.n % step));
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
