package ru.rtkmagistral.magistralapi.validation.formats.inn;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class INNValidatorTest {
    private final INNValidator v = new INNValidator();

    @Tag("unit")
    @ParameterizedTest(name = "[{index}] value={0} => valid={1}")
    @CsvSource(value = {
            "1234567890,true",
            "0124214124,true",
            "0000000000,true",
            "a111111111,false",
            "а111111111,false",
    }, quoteCharacter = '\u0000')

    void directIsValidTest(String value, boolean expected) {
        assertEquals(expected, v.isValid(value, null));
    }
}

