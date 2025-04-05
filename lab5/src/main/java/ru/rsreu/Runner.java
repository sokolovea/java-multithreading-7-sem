package ru.rsreu;

import ru.rsreu.storage.StorageFactory;
import ru.rsreu.storage.StorageSingleton;

import java.util.*;

/**
 * 7. вычислить вероятность того, что броски кубика 10с10 со взрывами будет больше 80
 */
public class Runner {
    private static final double DEFAULT_EPS = 0.0003;
    private static final int DEFAULT_BORDER_VALUE = 80;
    private static final List<Thread> threadList = new ArrayList<>();

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        long n = 0;
        int threadsCount = 0;

        try {
            System.out.println("Input count of iterations");
            n = Long.parseLong(scanner.nextLine());
            System.out.println("Input count of threads");
            threadsCount = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid parameter!");
            System.exit(1);
        }

        if (threadsCount <= 0) {
            System.out.println("Invalid number of threads!");
            System.exit(2);
        }

        long startTime = System.currentTimeMillis();

        startCalculationThreads(n, threadsCount);

        Thread printingThread = getPrintingThread(n);
        printingThread.start();

        try {
            for (Thread thread : threadList) {
                thread.join();
            }
            printingThread.interrupt();
        } catch (InterruptedException e) {
            System.out.println("Main has been interrupted!");
            System.exit(3);
        }

        long endTime = System.currentTimeMillis();

        StorageSingleton storage = StorageFactory.getStorageSingleton();
        System.out.printf("Result = %.5f, execution time = %.3f ms.\n", storage.getStorage().getResult(threadsCount),
                (endTime - startTime) / 1000.0);
        System.out.printf("Summary iterations count = %d\n", storage.getStorage().getCurrentIterationsCount());
    }


    /**
     * Gets thread for progress printing
     *
     * @param n max iterations count
     * @return printing thread
     */
    private static Thread getPrintingThread(long n) {
        Thread printDaemon = new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        StorageSingleton storage = StorageFactory.getStorageSingleton();
                        try {
                            while (true) {
                                if (Thread.interrupted()) {
                                    return;
                                }
                                System.out.printf("Progress: %.1f%%\n", storage.getStorage().getCurrentProgress(n));
                                Thread.sleep(50);
                            }
                        } catch (InterruptedException ignored) {
                        }
                    }
                });
        printDaemon.setDaemon(true);
        return printDaemon;
    }


    /**
     * Start calculations to find probability
     *
     * @param n            number of rolls
     * @param threadsCount number of threads
     */
    private static void startCalculationThreads(long n, long threadsCount) {
        for (int i = 0; i < threadsCount; i++) {
            long processedN = n / threadsCount;
            if (i == threadsCount - 1) {
                processedN += (n % threadsCount); //process all tasks
            }
            Thread thread = new Dice10d10RollCalculator(processedN, Runner.DEFAULT_BORDER_VALUE);
            thread.setName(Integer.toString(i + 1));
            threadList.add(thread);
            thread.start();
        }
    }
}
