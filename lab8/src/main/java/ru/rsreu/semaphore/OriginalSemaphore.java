package ru.rsreu.semaphore;

import java.util.concurrent.Semaphore;

/**
 * A wrapper for the Java standard Semaphore class implementing MySemaphore interface.
 */
public class OriginalSemaphore implements MySemaphore {

    /**
     * Internal Semaphore instance.
     */
    private final Semaphore semaphore;

    /**
     * Constructs a new OriginalSemaphore with the specified number of permits.
     *
     * @param permits the initial number of permits available
     */
    public OriginalSemaphore(int permits) {
        semaphore = new Semaphore(permits);
    }

    /**
     * Acquires a permit, blocking until one becomes available.
     *
     * @throws InterruptedException if the current thread is interrupted while waiting
     */
    @Override
    public void acquire() throws InterruptedException {
        semaphore.acquire();
    }

    /**
     * Releases a permit, increasing the number of available permits.
     */
    @Override
    public void release() {
        semaphore.release();
    }

    /**
     * Attempts to acquire a permit without blocking.
     *
     * @return true if a permit was acquired, false otherwise
     */
    @Override
    public boolean tryAcquire() {
        return semaphore.tryAcquire();
    }

    /**
     * Returns the current number of available permits.
     *
     * @return the number of available permits
     */
    @Override
    public int availablePermits() {
        return semaphore.availablePermits();
    }
}
