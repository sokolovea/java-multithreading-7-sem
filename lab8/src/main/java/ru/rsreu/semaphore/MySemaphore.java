package ru.rsreu.semaphore;

/**
 * Interface defining basic semaphore operations.
 */
public interface MySemaphore {

    /**
     * Acquires a permit, blocking if no permits are currently available.
     *
     * @throws InterruptedException if the current thread is interrupted while waiting
     */
    void acquire() throws InterruptedException;

    /**
     * Releases a permit, increasing the number of available permits.
     */
    void release();

    /**
     * Attempts to acquire a permit without blocking.
     *
     * @return true if a permit was acquired, false otherwise
     */
    boolean tryAcquire();

    /**
     * Returns the current number of available permits.
     *
     * @return the number of available permits
     */
    int availablePermits();
}
