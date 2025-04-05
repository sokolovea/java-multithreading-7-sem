package ru.rsreu;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Timeout;
import ru.rsreu.latch.MyCountDownLatch;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Unit test class for testing implementations of MyCountDownLatch.
 */
public class MyCountDownLatchTest {

    /**
     * Single count latch instance for testing single-thread scenarios.
     */
    private final MyCountDownLatch latch = SynchronizationObjectsFactory.getCountDownLatch(1);

    /**
     * Number of threads used in multi-threaded tests.
     */
    private final int numberOfThreadsForTests = 4;

    /**
     * Multi-count latch instance for testing multi-thread scenarios.
     */
    private final MyCountDownLatch latchWithSeveralCount =
            SynchronizationObjectsFactory.getCountDownLatch(numberOfThreadsForTests);

    /**
     * Repeated test to verify that a single-threaded latch unlocks correctly.
     * Ensures that only one thread proceeds after countdown.
     */
    @RepeatedTest(500)
    @Timeout(1)
    public void testCountDownLatchSingleThread() throws InterruptedException {
        AtomicReference<Throwable> threadException = new AtomicReference<>();
        AtomicBoolean isThreadLast = new AtomicBoolean(false);
        final MyCountDownLatch internalLatch = SynchronizationObjectsFactory.getCountDownLatch(1);

        Thread thread = new Thread(() -> {
            try {
                latch.await();
                isThreadLast.set(true);
            } catch (Throwable e) {
                threadException.set(e);
            } finally {
                internalLatch.countDown();
            }
        });
        thread.start();
        isThreadLast.set(false);
        latch.countDown();
        internalLatch.await();
        Assertions.assertNull(threadException.get());
        Assertions.assertTrue(isThreadLast.get());
    }

    /**
     * Repeated test to verify that multiple threads are correctly synchronized
     * using a latch with multiple counts. Ensures no exceptions and all threads
     * reach the critical section.
     */
    @RepeatedTest(500)
    public void testCountDownLatchMultipleThreads() throws InterruptedException {
        Object threadLock = new Object();
        ArrayList<AtomicReference<Throwable>> threadExceptions = new ArrayList<>();
        ArrayList<Boolean> isThreadLast = new ArrayList<>();
        AtomicInteger currentIndex = new AtomicInteger(0);

        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreadsForTests);

        for (int i = 0; i < numberOfThreadsForTests; i++) {
            executor.submit(() -> {
                try {
                    latchWithSeveralCount.await();
                } catch (Throwable e) {
                    synchronized (threadLock) {
                        threadExceptions.add(new AtomicReference<>(e));
                    }
                } finally {
                    synchronized (threadLock) {
                        isThreadLast.set(currentIndex.get(), true);
                        currentIndex.getAndIncrement();
                    }
                }
            });
        }

        for (int i = 0; i < numberOfThreadsForTests; i++) {
            isThreadLast.add(false);
        }
        for (int i = 0; i < numberOfThreadsForTests; i++) {
            latchWithSeveralCount.countDown();
        }
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.SECONDS);
        for (AtomicReference<Throwable> exception : threadExceptions) {
            Assertions.assertNull(exception.get());
        }
        for (Boolean eachBool : isThreadLast) {
            Assertions.assertTrue(eachBool);
        }
    }

    /**
     * Test to ensure that a latch with a zero count does not block the awaiting
     * thread. Checks that no exception is thrown in the thread.
     */
    @RepeatedTest(500)
    public void testCountDownNotBlocked() throws InterruptedException {
        latch.countDown();

        AtomicReference<Throwable> threadException = new AtomicReference<>();

        new Thread(() -> {
            try {
                latch.await();
            } catch (Throwable e) {
                threadException.set(e);
            }
        }).start();

        Assertions.assertNull(threadException.get(), "Exception should not be thrown in thread.");
    }
}
