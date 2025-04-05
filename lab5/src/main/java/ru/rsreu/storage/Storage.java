package ru.rsreu.storage;

/**
 * Storage that contains fields with the result and progress
 */
public class Storage {
    /**
     * Sum of calculation results
     */
    private volatile double resultSum;

    /**
     * Current summary iterations count for all of threads
     */
    private long currentIterationsCount;

    /**
     * Lock for the sum of calculation results
     */
    private final Object resultSumLock = new Object();

    /**
     * Lock for iterations count
     */
    private final Object currentIterationsLock = new Object();

    /**
     * Constructs object with printing the notification
     */
    public Storage() {
        System.out.println("Internal storage constructor");
    }

    /**
     * Gets the result of calculations
     *
     * @param threadsCount count of threads for calculations
     * @return the result of calculations
     */
    public double getResult(long threadsCount) {
        return resultSum / threadsCount;
    }

    /**
     * Appends the result of calculation to sum of results
     *
     * @param result the result of calculations for current thread
     */
    public void setResultSum(double result) {
        synchronized (resultSumLock) {
            this.resultSum += result;
        }
    }

    /**
     * Gets current progress in percentage
     *
     * @param maxIterationsCount equals 100% for progress
     * @return current progress in percentage
     */
    public double getCurrentProgress(long maxIterationsCount) {
        if (maxIterationsCount == 0) {
            return 0;
        }
        return (double) getCurrentIterationsCount() / maxIterationsCount * 100.0;
    }

    /**
     * Gets current summary iterations count
     *
     * @return current summary iterations count
     */
    public long getCurrentIterationsCount() {
        return currentIterationsCount;
    }

    /**
     * Updates current iterations count
     *
     * @param addCount additional iterations count
     */
    public void updateCurrentIterationsCount(long addCount) {
        synchronized (currentIterationsLock) {
            this.currentIterationsCount += addCount;
        }
    }
}
