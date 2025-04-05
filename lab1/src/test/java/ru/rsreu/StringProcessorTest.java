package ru.rsreu;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.RepeatedTest;

class StringProcessorTest {

    @RepeatedTest(50)
    public void countUpperLettersInNullStringShouldBe0() {
        String testString = null;
        Assertions.assertEquals(0, StringProcessor.getUpperLetterCount(testString));
    }

    @RepeatedTest(50)
    public void countUpperLettersInLowerCaseStringShouldBe0() {
        String testString = "hello, my dear rsreu!";
        Assertions.assertEquals(0, StringProcessor.getUpperLetterCount(testString));
    }

    @RepeatedTest(50)
    public void countUpperLettersInUpperCaseStringShouldBeCorrect() {
        String testString = "RYAZAN, RUSSIA, 390000.";
        Assertions.assertEquals(12, StringProcessor.getUpperLetterCount(testString));
    }

    @RepeatedTest(50)
    public void countUpperLettersInMixedCaseStringShouldBeCorrect() {
        String testString = "Anton Antonovich, the Governor, Artemy Filippovich, the Superintendent of Charities.";
        Assertions.assertEquals(7, StringProcessor.getUpperLetterCount(testString));
    }

    @RepeatedTest(50)
    public void countUpperLettersInStringWithoutLettersShouldBe0() {
        String testString = "!#$%&()*+,-./:;<=>?@[]^_`{|}~\"'";
        Assertions.assertEquals(0, StringProcessor.getUpperLetterCount(testString));
    }

}