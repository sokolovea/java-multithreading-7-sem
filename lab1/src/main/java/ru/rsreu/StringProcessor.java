package ru.rsreu;

/**
 * Methods for string processing
 */
public class StringProcessor {

    /**
     * Private constructor for utility class
     */
    private StringProcessor() {
    }

    /**
     * Count upper-case letters in string
     *
     * @param processingString input string
     * @return upper-case letters count
     */
    public static int getUpperLetterCount(String processingString) {
        int counter = 0;
        if (processingString != null) {
            for (int i = 0; i < processingString.length(); i++) {
                if (Character.isUpperCase(processingString.charAt(i))) {
                    counter++;
                }
            }
        }
        return counter;
    }
}
