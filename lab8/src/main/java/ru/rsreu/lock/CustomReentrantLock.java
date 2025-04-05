package ru.rsreu.lock;

/**
 * Custom implementation of a reentrant lock, allowing the same thread
 * to acquire the lock multiple times without causing a deadlock.
 */
public class CustomReentrantLock implements MyLock {

    /**
     * The thread currently holding the lock.
     */
    private Thread owner = null;

    /**
     * Counter for the number of times the lock has been acquired by the owner thread.
     */
    private int holdCounter = 0;

    /**
     * Acquires the lock, blocking if it is held by another thread.
     * If the current thread already owns the lock, increments the hold counter.
     */
    public synchronized void lock() {
        Thread currentThread = Thread.currentThread();

        if (currentThread.equals(owner)) {
            holdCounter++;
            return;
        }

        while (holdCounter > 0) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        owner = currentThread;
        holdCounter = 1;
    }

    /**
     * Releases the lock held by the current thread.
     * If the hold counter reaches zero, the lock is fully released.
     *
     * @throws IllegalMonitorStateException if the current thread does not hold the lock
     */
    public synchronized void unlock() {
        if (!Thread.currentThread().equals(owner)) {
            throw new IllegalMonitorStateException();
        }

        holdCounter--;

        if (holdCounter == 0) {
            owner = null;
            notifyAll();
        }
    }

    /**
     * Attempts to acquire the lock without blocking.
     * If the lock is available or already held by the current thread, acquires it.
     *
     * @return true if the lock was successfully acquired; false otherwise
     */
    public synchronized boolean tryLock() {
        Thread currentThread = Thread.currentThread();

        if (holdCounter == 0 || owner.equals(currentThread)) {
            lock();
            return true;
        }

        return false;
    }
}
