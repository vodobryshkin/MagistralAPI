package ru.rtkmagistral.magistralapi.mapper;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mapstruct.factory.Mappers;
import ru.rtkmagistral.magistralapi.domain.jpa.Company;
import ru.rtkmagistral.magistralapi.dto.company.CreateCompanyRequest;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ICompanyMapperTest {
    private final ICompanyMapper mapper = Mappers.getMapper(ICompanyMapper.class);

    static Stream<Arguments> toEntityCases() {
        return Stream.of(
                Arguments.of(
                        new CreateCompanyRequest("MAGISTRAL", "1234567890", "123456789", "11.11", true),
                        new Company("MAGISTRAL", "1234567890", "123456789", "11.11"),
                        true
                ),
                Arguments.of(
                        new CreateCompanyRequest("magistral", "1234567890", "123456789", "11.11", true),
                        new Company("MAGISTRAL", "1234567890", "123456789", "11.11"),
                        true
                ),
                Arguments.of(
                        new CreateCompanyRequest("MAGISTRAL", "1234567890", "123456789", "11.11", true),
                        new Company("MAGISTRAL", "1234567890", "123456789", "1.1"),
                        false
                )
        );
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("toEntityCases")
    void toEntityTest(CreateCompanyRequest req,
                      Company expected,
                      boolean result) {

        Company company = mapper.toEntity(req);

        if (result) {
            assertAll(
                    () -> assertEquals(expected.getTitle(), company.getTitle()),
                    () -> assertEquals(expected.getInn(), company.getInn()),
                    () -> assertEquals(expected.getKpp(), company.getKpp()),
                    () -> assertEquals(expected.getOkved(), company.getOkved()),
                    () -> assertNull(company.getId()),
                    () -> assertNull(company.getUser())
            );
        } else {
            boolean anyMismatch =
                    !java.util.Objects.equals(expected.getTitle(), company.getTitle()) ||
                            !java.util.Objects.equals(expected.getInn(), company.getInn()) ||
                            !java.util.Objects.equals(expected.getKpp(), company.getKpp()) ||
                            !java.util.Objects.equals(expected.getOkved(), company.getOkved()) ||
                            !java.util.Objects.isNull(company.getId()) ||
                            !java.util.Objects.isNull(company.getUser());

            assertTrue(anyMismatch);
        }
    }
}