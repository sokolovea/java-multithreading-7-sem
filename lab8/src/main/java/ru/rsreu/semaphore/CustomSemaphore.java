package ru.rsreu.semaphore;

/**
 * Custom implementation of a semaphore with basic acquire and release functionality.
 */
public class CustomSemaphore implements MySemaphore {

    /**
     * Number of permits available.
     */
    private int permits;

    /**
     * Lock object for synchronizing access to permits.
     */
    private final Object permits_lock = new Object();

    /**
     * Creates a semaphore with the given number of initial permits.
     *
     * @param permits initial number of permits
     */
    public CustomSemaphore(int permits) {
        this.permits = permits;
    }

    /**
     * Acquires a permit, blocking if none are available until one is released.
     *
     * @throws InterruptedException if the current thread is interrupted while waiting
     */
    @Override
    public void acquire() throws InterruptedException {
        synchronized (permits_lock) {
            while (permits <= 0) {
                permits_lock.wait();
            }
            permits--;
        }
    }

    /**
     * Releases a permit, increasing the number of available permits and notifying waiting threads.
     */
    @Override
    public void release() {
        synchronized (permits_lock) {
            permits++;
            permits_lock.notify();
        }
    }

    /**
     * Attempts to acquire a permit without blocking.
     *
     * @return true if a permit was successfully acquired, false otherwise
     */
    @Override
    public boolean tryAcquire() {
        synchronized (permits_lock) {
            if (permits <= 0) {
                return false;
            }
            permits--;
            return true;
        }
    }

    /**
     * Returns the current number of available permits.
     *
     * @return the number of available permits
     */
    @Override
    public int availablePermits() {
        synchronized (permits_lock) {
            return permits;
        }
    }
}
