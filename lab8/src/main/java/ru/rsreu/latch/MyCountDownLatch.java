package ru.rsreu.latch;

import java.util.concurrent.TimeUnit;

/**
 * Public interface for CountDownLatch
 */
public interface MyCountDownLatch {
    /**
     * Count down latch: decrements counter, if counter <= 0
     * then notifies all waiting threads
     */
    public void countDown();

    /**
     * Awaits when latch will be "unlocked"
     *
     * @throws InterruptedException if current thread is interrupted
     */
    public void await() throws InterruptedException;

    /**
     * Awaits when latch will be "unlocked" or timeout expires
     *
     * @param timeout  timeout value
     * @param timeUnit unit of measurement for timeout
     * @throws InterruptedException if current thread is interrupted
     */
    public void await(long timeout, TimeUnit timeUnit) throws InterruptedException;

    /**
     * Gets internal count value
     *
     * @return internal count value
     */
    public long getCount();
}
