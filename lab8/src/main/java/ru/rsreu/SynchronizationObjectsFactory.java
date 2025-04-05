package ru.rsreu;

import ru.rsreu.latch.CustomCountDownLatch;
import ru.rsreu.latch.MyCountDownLatch;
import ru.rsreu.latch.OriginalCountDownLatch;
import ru.rsreu.lock.CustomReentrantLock;
import ru.rsreu.lock.MyLock;
import ru.rsreu.lock.OriginalReentrantLock;
import ru.rsreu.semaphore.CustomSemaphore;
import ru.rsreu.semaphore.MySemaphore;
import ru.rsreu.semaphore.OriginalSemaphore;


/**
 * Factory for semaphores, locks, countdown latches
 */
public class SynchronizationObjectsFactory {
    private SynchronizationObjectsFactory() {
    }

    public static MySemaphore getSemaphore(int permits) {
//        return new OriginalSemaphore(permits);
        return new CustomSemaphore(permits);
    }

    public static MyLock getLock() {
//        return new OriginalReentrantLock();
        return new CustomReentrantLock();
    }

    public static MyCountDownLatch getCountDownLatch(int count) {
//        return new OriginalCountDownLatch(count);
        return new CustomCountDownLatch(count);
    }
}
