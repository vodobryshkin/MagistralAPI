package ru.rtkmagistral.magistralapi.validation.rules.cyrillic_word;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CyrillicWordValidationTest {
    private final CyrillicWordValidator v = new CyrillicWordValidator();

    @Tag("unit")
    @ParameterizedTest(name = "[{index}] value={0} => valid={1}")
    @CsvSource(value = {
            "Иван,true",
            "Vladimir,false",
            "Бестужев-Рюмин,false",
            "-Добрый,false",
            "O'Keffy,false",
            "O',false",
            "1234,false",
            "asfasf,false",
            "ПетРОВ,true"
    })
    void directIsValidTest(String value, boolean expected) {
        assertEquals(expected, v.isValid(value, null));
    }
}