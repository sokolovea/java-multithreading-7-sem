package ru.rsreu.storage;

/**
 * Storage that contains fields with the result and progress
 */
public class Storage {

    /**
     * Total iterations count for task solving
     */
    private static long totalIterationsCount = 0;

    /**
     * Step for printing progress to console
     */
    private static long stepForPrinting = 1;

    /**
     * Current value of printing counter (is compared with stepForPrinting)
     */
    private long printingCounter = 0;

    /**
     * Current summary iterations count for all of threads
     */
    private long currentIterationsCount;

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
     * Gets current progress in percentage
     *
     * @return current progress in percentage
     */
    public double getCurrentProgress() {
        if (totalIterationsCount == 0) {
            return 0;
        }
        return ((double) getCurrentIterationsCount()) / totalIterationsCount * 100.0;
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
     * Sets total target iterations count
     */
    public static synchronized void setTotalIterationsCount(long totalIterationsCount) {
        Storage.totalIterationsCount = totalIterationsCount;
        Storage.stepForPrinting = totalIterationsCount / 10000;
        if (Storage.stepForPrinting == 0) {
            Storage.stepForPrinting = 1;
        }
    }

    /**
     * Updates current iterations count
     *
     * @param addCount additional iterations count
     */
    public void updateCurrentIterationsCount(long addCount) {
        synchronized (currentIterationsLock) {
            this.currentIterationsCount += addCount;
            printingCounter += 1;
        }
        if (printingCounter % stepForPrinting == 0) {
            System.out.printf("Current progress: %.1f%%\n", getCurrentProgress());
        }
    }
}
