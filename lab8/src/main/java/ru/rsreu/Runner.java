package ru.rsreu;

import ru.rsreu.storage.Storage;
import ru.rsreu.storage.StorageFactory;
import ru.rsreu.storage.StorageSingleton;

import java.util.*;
import java.util.concurrent.*;

/**
 * 7. вычислить вероятность того, что броски кубика 10с10 со взрывами будет больше 80
 */
public class Runner {
    private static final double DEFAULT_EPS = 0.0003;
    private static final int DEFAULT_BORDER_VALUE = 80;

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        long n = 0;
        int threadsCount = 0;
        int calculationsLimit = 0;

        try {
            System.out.println("Input count of iterations");
            n = Long.parseLong(scanner.nextLine());
            System.out.println("Input count of threads");
            threadsCount = Integer.parseInt(scanner.nextLine());
            System.out.println("Input limits of max parallel calculation tasks");
            calculationsLimit = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid parameter!");
            System.exit(1);
        }

        if (threadsCount <= 0) {
            System.out.println("Invalid number of threads!");
            System.exit(2);
        }

        if (calculationsLimit <= 0) {
            System.out.println("Invalid limits of max parallel calculation tasks!");
            System.exit(3);
        }
        StorageSingleton storageSingleton = StorageFactory.getStorageSingleton();
        Storage storage = storageSingleton.getStorage();
        Storage.setTotalIterationsCount(n);
        storage.setSemaphoreCalc(calculationsLimit);
        storage.setCountDownLatch(threadsCount);
        ExecutorService executorService = Executors.newFixedThreadPool(threadsCount);


        List<Future<Double>> futureTasks = new ArrayList<>();
        for (int i = 0; i < threadsCount; i++) {
            Dice10d10RollCalculator task = new Dice10d10RollCalculator(n / threadsCount, Runner.DEFAULT_BORDER_VALUE);
            futureTasks.add(executorService.submit(task));
        }

        try {
            System.out.println("Awaiting calculations...");
            storage.getCountDownLatch().await(15, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            System.out.println("Interrupping main thread...");
            shutdownProgram(executorService, 1);
        }

        long referenceTime = System.currentTimeMillis();
        System.out.println("---");

        for (String eachKey: (storage.getTimesArray().keySet())) {
            System.out.printf("Time for %s = %.3f s\n", eachKey, (referenceTime - storage.getTimesArray().get(eachKey)) / 1000.0);
        }

        System.out.println("---");
        double averageResult = 0;
        for (Future<Double> future : futureTasks) {
            try {
                averageResult += future.get(2, TimeUnit.MINUTES);
            } catch (InterruptedException | ExecutionException e) {
                System.out.println("Interrupping main thread...");
                shutdownProgram(executorService, 1);
            } catch (TimeoutException e) {
                System.out.println("Maybe a problem with calculations (too long)!");
                shutdownProgram(executorService, 2);
            }
        }

        averageResult /= threadsCount;
        System.out.printf("Result = %.5f\n", averageResult);
        shutdownProgram(executorService, 0);
    }

    /**
     * Stops calculations and shutdowns program
     *
     * @param executorService Executor for tasks
     * @param code Code to be returned to operating system
     */
    private static void shutdownProgram(ExecutorService executorService, int code) {
        executorService.shutdownNow();
        try {
            boolean isTerminated = executorService.awaitTermination(1, TimeUnit.SECONDS);
            if (!isTerminated) {
                System.out.println("Await termination timed out!");
            }
        } catch (InterruptedException e) {
            System.out.println("Interrupted while shutting down program...");
        }
        System.exit(code);
    }

}