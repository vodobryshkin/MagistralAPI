package ru.rtkmagistral.magistralapi.validation.formats.kpp;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class KPPValidatorTest {
    private final KPPValidator v = new KPPValidator();

    @Tag("unit")
    @ParameterizedTest(name = "[{index}] value={0} => valid={1}")
    @CsvSource(value = {
            "123456789,true",
            "012421412,true",
            "000000000,true",
            "a11111111,false",
            "а11111111,false",
    }, quoteCharacter = '\u0000')

    void directIsValidTest(String value, boolean expected) {
        assertEquals(expected, v.isValid(value, null));
    }
}

