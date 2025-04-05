package ru.rsreu;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Timeout;
import ru.rsreu.lock.MyLock;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Unit test class for testing implementations of MyLock.
 */
public class MyLockTest {

    /**
     * Lock instance for testing.
     */
    private final MyLock lock = SynchronizationObjectsFactory.getLock();

    /**
     * Shared value for testing multithreaded access.
     */
    private int value = 0;

    /**
     * Repeated test to verify that lock/unlock works correctly in a multithreaded context.
     */
    @RepeatedTest(500)
    @Timeout(1)
    public void lockUnlockShouldCorrectWorkInMultiThread() throws InterruptedException {
        AtomicReference<Throwable> threadException = new AtomicReference<>();

        Thread first = new Thread(() -> {
            try {
                lock.lock();
                int oldValue = value;
                for (int i = 0; i < 10000; i++) {
                    value++;
                }
                Assertions.assertEquals(oldValue + 10000, value);
            } catch (Throwable e) {
                threadException.set(e);
            } finally {
                lock.unlock();
            }
        });

        Thread second = new Thread(() -> {
            try {
                lock.lock();
                int oldValue = value;
                for (int i = 0; i < 10000; i++) {
                    value++;
                }
                Assertions.assertEquals(oldValue + 10000, value);
            } catch (Throwable e) {
                threadException.set(e);
            } finally {
                lock.unlock();
            }
        });

        first.start();
        second.start();
        first.join();
        second.join();
        Assertions.assertEquals( 20000, value);
        Assertions.assertNull(threadException.get());
    }

    /**
     * Repeated test to verify that tryLock works correctly in a multithreaded context.
     */
    @RepeatedTest(500)
    public void tryLockShouldWorkInMultiThread() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        AtomicBoolean isLockedByFirstThread = new AtomicBoolean(false);
        AtomicBoolean isLockedBySecondThread = new AtomicBoolean(false);

        Thread first = new Thread(() -> {
            isLockedByFirstThread.set(lock.tryLock());
            countDownLatch.countDown();
        });

        Thread second = new Thread(() -> {
            try {
                countDownLatch.await();
                isLockedBySecondThread.set(lock.tryLock());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        first.start();
        second.start();
        first.join();
        second.join();

        Assertions.assertTrue(isLockedByFirstThread.get(), "First thread should acquire the lock.");
        Assertions.assertFalse(isLockedBySecondThread.get(), "Second thread should not acquire the lock.");
    }

    /**
     * Repeated test to verify that tryLock does not acquire the lock when it is already held.
     */
    @RepeatedTest(500)
    public void tryLockShouldNotAcquireLockWhenAlreadyHeld() throws InterruptedException {
        lock.lock();

        AtomicBoolean isLocked = new AtomicBoolean(false);
        Thread thread = new Thread(() -> {
            isLocked.set(lock.tryLock());
        });

        thread.start();
        thread.join();

        Assertions.assertFalse(isLocked.get(), "Thread should not acquire the lock since it's already held.");
        lock.unlock();
    }

}
