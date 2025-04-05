package ru.rsreu.latch;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Wrapper on internal CountDownLatch
 */
public class OriginalCountDownLatch implements MyCountDownLatch {

    /**
     * Original CountDownLatch
     */
    private final CountDownLatch latch;

    /**
     * Constructs latch with determined count
     *
     * @param count initial internal count value
     */
    public OriginalCountDownLatch(int count) {
        latch = new CountDownLatch(count);
    }

    /**
     * Count down latch: decrements counter, if counter <= 0
     * then notifies all waiting threads
     */
    @Override
    public void countDown() {
        latch.countDown();
    }

    /**
     * Awaits when latch will be "unlocked"
     *
     * @throws InterruptedException if current thread is interrupted
     */
    @Override
    public void await() throws InterruptedException {
        latch.await();
    }

    /**
     * Awaits when latch will be "unlocked" or timeout expires
     *
     * @param timeout  timeout value
     * @param timeUnit unit of measurement for timeout
     * @throws InterruptedException if current thread is interrupted
     */
    @Override
    public void await(long timeout, TimeUnit timeUnit) throws InterruptedException {
        latch.await(timeout, timeUnit);
    }

    /**
     * Gets internal count value
     *
     * @return internal count value
     */
    @Override
    public long getCount() {
        return latch.getCount();
    }
}
