package ru.rsreu;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.RepeatedTest;
import ru.rsreu.lock.MyLock;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Unit test class for testing reentrant behavior of MyLock implementations.
 */
public class MyReentrantLockTest {

    /**
     * Lock instance for testing.
     */
    private final MyLock lock = SynchronizationObjectsFactory.getLock();

    /**
     * Verifies that the lock is reentrant.
     */
    @RepeatedTest(500)
    public void lockShouldBeReentrantInSingleThread() {
        lock.lock();
        AtomicBoolean isLocked = new AtomicBoolean(false);
        isLocked.set(lock.tryLock());
        Assertions.assertTrue(isLocked.get());
        lock.unlock();
    }

    /**
     * Verifies that the locked lock can't be unlocked by another thread.
     */
    @RepeatedTest(500)
    public void lockShouldNotBeUnlockedByAnotherThread() {
        CountDownLatch latch = new CountDownLatch(1);
        new Thread(new Runnable() {
            @Override
            public void run() {
                lock.lock();
                latch.countDown();
            }
        }).start();
        try {
            latch.await(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Assertions.fail(e);
        }
        Assertions.assertThrows(IllegalMonitorStateException.class, () -> {lock.unlock();});
    }

}
