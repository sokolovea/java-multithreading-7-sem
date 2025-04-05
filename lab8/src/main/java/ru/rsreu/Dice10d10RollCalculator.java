package ru.rsreu;

import ru.rsreu.storage.StorageFactory;
import ru.rsreu.storage.StorageSingleton;
import java.util.concurrent.Callable;

import static java.lang.Thread.interrupted;

/**
 * Calculator for rolls for 10d10 dice
 */
public class Dice10d10RollCalculator implements Callable<Double> {
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
    private final StorageSingleton storageInitOnDemand;

    /**
     * Constructs calculator with preferred eps
     *
     * @param n preferred maximal error rate for calculations
     */
    public Dice10d10RollCalculator(long n, int borderValue) {
        this.n = n;
        this.borderValue = borderValue;
        this.storageInitOnDemand = StorageFactory.getStorageSingleton();
    }

    /**
     * Gets probability that sum after all rolls will be more than borderValue
     *
     * @return probability that sum after all rolls will be more than borderValue
     */
    public double calculateProbabilitySumBeMoreBorder() throws InterruptedException {
        long counter = 0;
        if (this.n <= 0) {
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
            if (i % step == 0) {
                storageInitOnDemand.getStorage().updateCurrentIterationsCount(step);
            }
        }
        storageInitOnDemand.getStorage().updateCurrentIterationsCount(this.n % step);
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
    public Double call() throws Exception {
        try {
            storageInitOnDemand.getStorage().getSemaphoreCalc().acquire();
            System.out.printf("Task %s has started calculations!\n", Thread.currentThread().getName());
            double result = calculateProbabilitySumBeMoreBorder();

            System.out.printf("Task[%s]: completed!\n", Thread.currentThread().getName());
            storageInitOnDemand.getStorage().setTimeForFinishingTask(Thread.currentThread().getName());
            return result;
        } catch (InterruptedException e) {
            System.out.printf("Task[%s] is interrupted!\n", Thread.currentThread().getName());
        } finally {
            storageInitOnDemand.getStorage().getSemaphoreCalc().release();
            storageInitOnDemand.getStorage().getCountDownLatch().countDown();
        }
        return 0.0;
    }
}
