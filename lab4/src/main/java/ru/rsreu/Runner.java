package ru.rsreu;

import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.Arrays;
import java.util.ArrayList;

/**
 * 7. вычислить вероятность того, что броски кубика 10с10 со взрывами будет больше 80
 */
public class Runner {
    private static final double DEFAULT_EPS = 0.0003;
    private static final int DEFAULT_BORDER_VALUE = 80;
    private static final Set<String> AVAILABLE_COMMANDS = new HashSet<>(Arrays.asList("start", "stop", "await", "exit"));

    public static void main(String[] args) {
        Dice10d10RollCalculator diceCalculator =
                new Dice10d10RollCalculator(Runner.DEFAULT_EPS, Runner.DEFAULT_BORDER_VALUE);
        Scanner scanner = new Scanner(System.in);
        String[] input;
        String command;
        int availableNumber = 0;
        ArrayList<Thread> threads = new ArrayList<>();

        try {
            while (true) {
                try {
                    System.out.print("$ ");
                    input = scanner.nextLine().trim().split(" ");
                    command = input[0];
                    if (!AVAILABLE_COMMANDS.contains(command)) {
                        if (!command.isEmpty()) {
                            System.out.println("Wrong command!");
                        }
                        continue;
                    }

                    if ((!command.equals("exit") && input.length != 2) || (command.equals("exit") && input.length != 1)) {
                        System.out.println("Wrong number of arguments for command!");
                        continue;
                    }

                    if (Thread.interrupted()) {
                        throw new InterruptedException();
                    }
                    switch (command) {
                        case "exit":
                            System.exit(0);
                        case "start":
                            availableNumber = getFreeThreadNumber(threads);
                            threads.set(availableNumber,
                                    new Dice10d10RollCalculator(Double.parseDouble(input[1]), DEFAULT_BORDER_VALUE));
                            threads.get(availableNumber).setName(Integer.toString(availableNumber));
                            threads.get(availableNumber).start();
                            System.out.println("New task with number: " + availableNumber);
                            break;
                        case "stop":
                            availableNumber = Integer.parseInt(input[1]);
                            checkThreadExistence(threads.get(availableNumber));
                            threads.get(availableNumber).interrupt();
                            break;
                        case "await":
                            availableNumber = Integer.parseInt(input[1]);
                            checkThreadExistence(threads.get(availableNumber));
                            threads.get(availableNumber).join();
                            break;
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Can't parse command argument!");
                } catch (IndexOutOfBoundsException | IllegalStateException e) {
                    System.out.printf("Task[%d] does not exist!\n", availableNumber);
                }
            }
        } catch (InterruptedException e) {
            System.out.println("Main thread has been interrupted!");
            System.exit(0);
        }
    }

    /**
     * Gets free thread number for new task
     * @param threads array of threads
     * @return free thread number
     */
    public static int getFreeThreadNumber(ArrayList<Thread> threads) {
        int freeThreadNumber = 0;
        for (Thread thread : threads) {
            if (thread == null || !thread.isAlive()) {
                return freeThreadNumber;
            }
            freeThreadNumber++;
        }
        threads.add(null);
        return freeThreadNumber;
    }

    /**
     * Checks check existence of thread
     * @param thread
     * @throws IllegalStateException
     */
    public static void checkThreadExistence(Thread thread) throws IllegalStateException {
        if (!thread.isAlive()) {
            throw new IllegalStateException();
        }
    }
}
