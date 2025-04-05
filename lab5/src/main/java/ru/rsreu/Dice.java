package ru.rsreu;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Dice with n sides
 */
public class Dice {

    /**
     * sides (>= 4)
     */
    private int sidesNumber;

    /**
     * Constructs dice with n sides
     */
    public Dice(int sidesNumber) {
        setSidesNumber(sidesNumber);
    }

    /**
     * Gets sides number for dice
     *
     * @return sides number for dice
     */
    public int getSidesNumber() {
        return sidesNumber;
    }

    /**
     * Sets sides number for dice. Number should be more than 3
     *
     * @param sidesNumber sides number for dice
     */
    public void setSidesNumber(int sidesNumber) {
        if (sidesNumber < 4) {
            this.sidesNumber = 4;
        }
        this.sidesNumber = sidesNumber;
    }

    /**
     * Gets random dice value after roll
     *
     * @return dice value after roll
     */
    public int getValueAfterRoll() {
        return ThreadLocalRandom.current().nextInt(1, sidesNumber + 1);
    }
}
