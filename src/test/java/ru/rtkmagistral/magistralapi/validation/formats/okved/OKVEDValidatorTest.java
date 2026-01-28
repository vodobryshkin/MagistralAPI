package ru.rtkmagistral.magistralapi.validation.formats.okved;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OKVEDValidatorTest {
    private final OKVEDValidator v = new OKVEDValidator();

    @Tag("unit")
    @ParameterizedTest(name = "[{index}] value={0} => valid={1}")
    @CsvSource(value = {
            "01,true",
            "01.1,true",
            "01.11,true",
            "01.11.1,true",
            "01.11.11,true",
            "10,true",
            "10.8,true",
            "10.82,true",
            "10.82.1,true",
            "10.82.11,true",
            "47,true",
            "47.9,true",
            "47.91,true",
            "47.91.2,true",
            "47.91.21,true",
            "62,true",
            "62.0,true",
            "62.01,true",
            "62.01.1,true",
            "62.01.11,true",
            "1,false",
            "001,false",
            "01.,false",
            ".01,false",
            "01.111,false",
            "01.1.1,false",
            "01.11.111,false",
            "47.91.2.1,false",
            "47..91,false",
            "47-91,false",
            "47 91,false",
            "01.1a,false",
            "01.a1,false",
            "01.11.a,false",
            "01.11.1a,false",
    }, quoteCharacter = '\u0000')

    void directIsValidTest(String value, boolean expected) {
        assertEquals(expected, v.isValid(value, null));
    }
}

