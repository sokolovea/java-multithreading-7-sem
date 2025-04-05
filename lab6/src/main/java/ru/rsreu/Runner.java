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

        ExecutorService executorService = Executors.newFixedThreadPool(threadsCount);
        long startTime = System.currentTimeMillis();

        Storage.setTotalIterationsCount(n);
        List<Future<Double>> futureTasks = new ArrayList<>();
        for (int i = 0; i < threadsCount; i++) {
            Dice10d10RollCalculator task = new Dice10d10RollCalculator(n / threadsCount, Runner.DEFAULT_BORDER_VALUE);
            futureTasks.add(executorService.submit(task));
        }


        double averageResult = futureTasks.stream()
                .mapToDouble(future -> {
                    try {
                        return future.get(100, TimeUnit.SECONDS);
                    } catch (InterruptedException | ExecutionException e) {
                        System.out.println("Interrupping main thread...");
                        shutdownProgram(executorService, 1);
                    } catch (TimeoutException e) {
                        System.out.println("Maybe a problem with calculations (too long)!");
                        shutdownProgram(executorService, 2);
                    }
                    return 0;
                })
                .average()
                .orElse(-1);

        long endTime = System.currentTimeMillis();

        System.out.printf("Result = %.5f, execution time = %.3f s.\n", averageResult,
                (endTime - startTime) / 1000.0);
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