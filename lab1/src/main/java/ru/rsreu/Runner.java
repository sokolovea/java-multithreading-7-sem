package ru.rsreu;

import java.util.Scanner;

/**
 * d. определение количества заглавных букв в строке
 */
public class Runner {
    public static void main(String[] args) {
        System.out.println("Please input string to process");
        Scanner scanner = new Scanner(System.in);
        String string = scanner.nextLine();
        int countUpperLetters = StringProcessor.getUpperLetterCount(string);
        System.out.println("Count upper-case letters = " + countUpperLetters);
    }
}
