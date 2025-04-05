package ru.rsreu.lock;

/**
 * Custom lock interface defining basic locking mechanisms.
 */
public interface MyLock {

    /**
     * Acquires the lock, blocking if necessary until it is available.
     */
    void lock();

    /**
     * Attempts to acquire the lock without blocking.
     *
     * @return true if the lock was successfully acquired, false otherwise
     */
    boolean tryLock();

    /**
     * Releases the lock held by the current thread.
     *
     * @throws IllegalMonitorStateException if the current thread does not hold the lock
     */
    void unlock();
}
