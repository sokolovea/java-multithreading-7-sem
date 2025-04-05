package ru.rsreu.lock;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Custom implementation of MyLock using ReentrantLock.
 */
public class OriginalReentrantLock implements MyLock {

    /**
     * Internal lock instance for managing thread synchronization.
     */
    private final Lock lock = new ReentrantLock();

    /**
     * Acquires the lock, blocking if necessary until it is available.
     */
    @Override
    public void lock() {
        lock.lock();
    }

    /**
     * Attempts to acquire the lock without blocking.
     *
     * @return true if the lock was successfully acquired, false otherwise
     */
    @Override
    public boolean tryLock() {
        return lock.tryLock();
    }

    /**
     * Releases the lock held by the current thread.
     */
    @Override
    public void unlock() {
        lock.unlock();
    }
}
