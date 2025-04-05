package ru.rsreu.latch;

import java.util.concurrent.TimeUnit;

/**
 * Custom realization of CountDownLatch
 */
public class CustomCountDownLatch implements MyCountDownLatch {

    /**
     * Internal counter
     */
    private int count;

    /**
     * Lock for counter
     */
    private final Object count_lock = new Object();

    /**
     * Constructs latch with determined count
     *
     * @param count initial internal count value
     */
    public CustomCountDownLatch(int count) {
        this.count = count;
    }

    /**
     * Count down latch: decrements counter, if counter <= 0
     * then notifies all waiting threads
     */
    @Override
    public void countDown() {
        synchronized (count_lock) {
            count--;
            if (count <= 0) {
                count_lock.notifyAll();
            }
        }
    }

    /**
     * Awaits when latch will be "unlocked"
     *
     * @throws InterruptedException if current thread is interrupted
     */
    @Override
    public void await() throws InterruptedException {
        synchronized (count_lock) {
            while (count > 0) {
                count_lock.wait();
            }
        }
    }

    /**
     * Awaits when latch will be "unlocked" or timeout expires
     *
     * @param timeout timeout value
     * @param timeUnit unit of measurement for timeout
     * @throws InterruptedException if current thread is interrupted
     */
    @Override
    public void await(long timeout, TimeUnit timeUnit) throws InterruptedException {
        long remainingTime = timeUnit.toMillis(timeout);
        synchronized (count_lock) {
            long endTime = System.currentTimeMillis() + remainingTime;
            while (count > 0) {
                long waitTime = endTime - System.currentTimeMillis();
                if (waitTime <= 0) {
                    return;
                }
                count_lock.wait(waitTime);
            }
        }
    }

    /**
     * Gets internal count value
     *
     * @return internal count value
     */
    @Override
    public long getCount() {
        synchronized (count_lock) {
            return count;
        }
    }
}
