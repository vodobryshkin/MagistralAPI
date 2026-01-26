package ru.rtkmagistral.magistralapi.validation.formats.phone_number;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import ru.rtkmagistral.magistralapi.validation.formats.phone.PhoneNumberValidator;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PhoneNumberTest {
    private final PhoneNumberValidator v = new PhoneNumberValidator();

    @Tag("unit")
    @ParameterizedTest(name = "[{index}] value={0} => valid={1}")
    @CsvSource(
            value = {
                    "+79999999999,true",
                    "+179999999999,false",
                    "89999999999,false",
                    "+99999999999,false",
                    "+7123456789,false",
            }
    )
    void directIsValidTest(String value, boolean expected) {
        assertEquals(expected, v.isValid(value, null));
    }
}
