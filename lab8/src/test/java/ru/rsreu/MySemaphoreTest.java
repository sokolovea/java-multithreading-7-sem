package ru.rsreu;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Timeout;
import ru.rsreu.semaphore.MySemaphore;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Unit test class for testing MySemaphore implementations.
 */
public class MySemaphoreTest {

    /**
     * Semaphore instances for testing with different permits.
     */
    private final MySemaphore semaphoreWithSinglePermit = SynchronizationObjectsFactory.getSemaphore(1);
    private final MySemaphore semaphoreWithTwoPermits = SynchronizationObjectsFactory.getSemaphore(2);

    /**
     * Repeated test to verify correct acquire and release behavior in a single thread.
     */
    @RepeatedTest(500)
    @Timeout(1)
    public void acquireAndReleaseShouldCorrectWorkInSingleThread() throws InterruptedException {
        Assertions.assertEquals(2, semaphoreWithTwoPermits.availablePermits());
        semaphoreWithTwoPermits.acquire();
        Assertions.assertEquals(1, semaphoreWithTwoPermits.availablePermits());
        semaphoreWithTwoPermits.acquire();
        Assertions.assertEquals(0, semaphoreWithTwoPermits.availablePermits());
        semaphoreWithTwoPermits.release();
        Assertions.assertEquals(1, semaphoreWithTwoPermits.availablePermits());
        semaphoreWithTwoPermits.release();
        Assertions.assertEquals(2, semaphoreWithTwoPermits.availablePermits());
    }

    /**
     * Repeated test to verify correct acquire and release behavior in multiple threads.
     */
    @RepeatedTest(500)
    @Timeout(1)
    public void acquireReleaseShouldCorrectWorkInMultiThread() throws InterruptedException {
        CountDownLatch countDownLatchFirst = new CountDownLatch(1);
        CountDownLatch countDownLatchSecond = new CountDownLatch(1);
        AtomicReference<Throwable> threadException = new AtomicReference<>();

        Thread first = new Thread(() -> {
            try {
                semaphoreWithSinglePermit.acquire();
                countDownLatchSecond.countDown();
                countDownLatchFirst.await(5, TimeUnit.SECONDS);
                Assertions.assertEquals(1, semaphoreWithSinglePermit.availablePermits());
            } catch (Throwable e) {
                threadException.set(e);
            }
        });
        first.start();

        Thread second = new Thread(() -> {
            try {
                countDownLatchSecond.await(5, TimeUnit.SECONDS);
                Assertions.assertEquals(0, semaphoreWithSinglePermit.availablePermits());
                semaphoreWithSinglePermit.release();
                Assertions.assertEquals(1, semaphoreWithSinglePermit.availablePermits());
            } catch (Throwable e) {
                threadException.set(e);
            } finally {
                countDownLatchFirst.countDown();
            }
        });
        second.start();
        first.join();
        second.join();
        Assertions.assertNull(threadException.get());
    }

    /**
     * Repeated test to verify tryAcquire behavior in a single thread.
     */
    @RepeatedTest(1000)
    @Timeout(1)
    public void tryAcquireShouldCorrectWorkInSingleThread() throws InterruptedException {
        Assertions.assertEquals(2, semaphoreWithTwoPermits.availablePermits());
        Assertions.assertTrue(semaphoreWithTwoPermits.tryAcquire());
        Assertions.assertEquals(1, semaphoreWithTwoPermits.availablePermits());
        Assertions.assertTrue(semaphoreWithTwoPermits.tryAcquire());
        Assertions.assertEquals(0, semaphoreWithTwoPermits.availablePermits());
        Assertions.assertFalse(semaphoreWithTwoPermits.tryAcquire());
        semaphoreWithTwoPermits.release();
        Assertions.assertEquals(1, semaphoreWithTwoPermits.availablePermits());
        Assertions.assertTrue(semaphoreWithTwoPermits.tryAcquire());
        Assertions.assertEquals(0, semaphoreWithTwoPermits.availablePermits());
        Assertions.assertFalse(semaphoreWithTwoPermits.tryAcquire());
        Assertions.assertFalse(semaphoreWithTwoPermits.tryAcquire());
        Assertions.assertFalse(semaphoreWithTwoPermits.tryAcquire());
        Assertions.assertFalse(semaphoreWithTwoPermits.tryAcquire());
        semaphoreWithTwoPermits.release();
        Assertions.assertEquals(1, semaphoreWithTwoPermits.availablePermits());
        semaphoreWithTwoPermits.release();
        Assertions.assertEquals(2, semaphoreWithTwoPermits.availablePermits());
    }

    /**
     * Repeated test to verify tryAcquire behavior in multiple threads.
     */
    @RepeatedTest(1000)
    @Timeout(1)
    public void tryAcquireShouldCorrectWorkInMultiThread() throws InterruptedException {
        CountDownLatch countDownLatchFirst = new CountDownLatch(1);
        CountDownLatch countDownLatchSecond = new CountDownLatch(1);
        AtomicReference<Throwable> threadException = new AtomicReference<>();

        Thread first = new Thread(() -> {
            try {
                Assertions.assertTrue(semaphoreWithSinglePermit.tryAcquire());
                Assertions.assertEquals(0, semaphoreWithSinglePermit.availablePermits());
                countDownLatchSecond.countDown();
                countDownLatchFirst.await(5, TimeUnit.SECONDS);
                Assertions.assertEquals(1, semaphoreWithSinglePermit.availablePermits());
            } catch (Throwable e) {
                threadException.set(e);
            }
        });
        first.start();

        Thread second = new Thread(() -> {
            try {
                countDownLatchSecond.await(5, TimeUnit.SECONDS);
                Assertions.assertEquals(0, semaphoreWithSinglePermit.availablePermits());
                Assertions.assertFalse(semaphoreWithSinglePermit.tryAcquire());
                semaphoreWithSinglePermit.release();
                Assertions.assertTrue(semaphoreWithSinglePermit.tryAcquire());
                Assertions.assertEquals(0, semaphoreWithSinglePermit.availablePermits());
                semaphoreWithSinglePermit.release();
                Assertions.assertEquals(1, semaphoreWithSinglePermit.availablePermits());
                countDownLatchFirst.countDown();
            } catch (Throwable e) {
                threadException.set(e);
            }
        });
        second.start();
        first.join();
        second.join();
        Assertions.assertNull(threadException.get());
    }

}
