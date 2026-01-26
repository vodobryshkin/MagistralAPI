package ru.rtkmagistral.magistralapi.validation.formats.password;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PasswordTest {

    private final PasswordValidator v = new PasswordValidator();

    @Tag("unit")
    @ParameterizedTest(name = "[{index}] value={0} => valid={1}")
    @CsvSource(value = {
            "abcdef,true",
            "Abcdef,true",
            "abc123,true",
            "passWORD123,true",
            "qwerty_123,true",
            "HelloWorld!,true",
            "123456,true",
            "'abc def',false",
            "'   ',false",
            "пароль,false",
            "Passпароль,false"
    }, quoteCharacter = '\u0000')
    void directIsValidTest(String value, boolean expected) {
        assertEquals(expected, v.isValid(value, null));
    }
}
