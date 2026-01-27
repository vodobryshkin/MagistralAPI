package ru.rtkmagistral.magistralapi.validation.formats.uuid;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UUIDTest {
    private final UUIDValidator v = new UUIDValidator();

    @Tag("unit")
    @ParameterizedTest(name = "[{index}] value={0} => valid={1}")
    @CsvSource(
            value = {
                    "550e8400-e29b-41d4-a716-446655440000,true",
                    "550E8400-E29B-41D4-A716-446655440000,true",
                    "00000000-0000-0000-0000-000000000000,true",
                    "550e8400e29b41d4a716446655440000,false",
                    "550e8400-e29b-41d4-a716-44665544000,false",
                    "550e8400-e29b-41d4-a716-4466554400000,false",
                    "g50e8400-e29b-41d4-a716-446655440000,false",
                    "'',false"
            }
    )
    void directIsValidTest(String value, boolean expected) {
        assertEquals(expected, v.isValid(value, null));
    }
}

