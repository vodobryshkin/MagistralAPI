package ru.rtkmagistral.magistralapi.validation.rules.proper_noun;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ProperNounValidationTest {
    private final ProperNounValidator v = new ProperNounValidator();

    @Tag("unit")
    @ParameterizedTest(name = "[{index}] value={0} => valid={1}")
    @CsvSource(value = {
            "Иван,true",
            "Vladimir,true",
            "Бестужев-Рюмин,true",
            "-Добрый,false",
            "O'Keffy,false",
            "O',false",
            "1234,false",
            "asfasf,false",
            "ПетРОВ,false"
    })
    void directIsValidTest(String value, boolean expected) {
        assertEquals(expected, v.isValid(value, null));
    }
}
