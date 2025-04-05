package ru.rsreu.storage;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
    private final Lock currentIterationsLock = new ReentrantLock();

    /**
     * Latch for time counting
     */
    private CountDownLatch countDownLatch = new CountDownLatch(1);

    /**
     * Array of times
     */
    private final Map<String, Long> timesArray = new ConcurrentHashMap<>();

    /**
     * Semaphore to limit count of parallel tasks
     */
    private Semaphore semaphoreCalc = new Semaphore(Runtime.getRuntime().availableProcessors());

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
        try {
            currentIterationsLock.lock();
            this.currentIterationsCount += addCount;
            printingCounter += 1;
            if (printingCounter % stepForPrinting == 0) {
                System.out.printf("Current progress: %.1f%%\n", getCurrentProgress());
            }
        } finally {
            currentIterationsLock.unlock();
        }
    }

    /**
     * Set name and time of finishing task to map
     *
     * @param taskName name of the task
     */
    public void setTimeForFinishingTask(String taskName) {
        timesArray.put(taskName, System.currentTimeMillis());
    }

    /**
     * Gets arrays of times for threads
     *
     * @return arrays of times for threads
     */
    public Map<String, Long> getTimesArray() {
        return timesArray;
    }

    /**
     * Gets semaphore for calculations
     *
     * @return semaphore for calculations
     */
    public Semaphore getSemaphoreCalc() {
        return semaphoreCalc;
    }

    /**
     * Sets semaphore for calculations
     *
     * @param maxParallelTasksCount value for semaphore for calculations
     */
    public synchronized void setSemaphoreCalc(int maxParallelTasksCount) {
        if (maxParallelTasksCount > 0) {
            this.semaphoreCalc = new Semaphore(maxParallelTasksCount);
        }
    }

    /**
     * Gets latch for time counting
     *
     * @return latch for time counting
     */
    public synchronized CountDownLatch getCountDownLatch() {
        return this.countDownLatch;
    }

    /**
     * Sets latch for time counting
     *
     * @param threadsCount initial internal value of latch
     */
    public synchronized void setCountDownLatch(int threadsCount) {
        if (threadsCount > 0) {
            this.countDownLatch = new CountDownLatch(threadsCount);
        }
    }
}
