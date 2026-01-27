package ru.rtkmagistral.magistralapi.mapper;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mapstruct.factory.Mappers;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.rtkmagistral.magistralapi.domain.jpa.User;
import ru.rtkmagistral.magistralapi.dto.user.CreateUserRequest;

import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class IUserMapperTest {
    private final IUserMapper mapper = Mappers.getMapper(IUserMapper.class);

    static Stream<Arguments> toEntityCases() {
        return Stream.of(
                Arguments.of(
                        new CreateUserRequest("Ivan", "Ivanov", null, "a@b.ru", "+79999999999", "qwerty123", true),
                        new User("Ivan", "Ivanov", null, "a@b.ru", "+79999999999", "qwerty123".getBytes(StandardCharsets.UTF_8)),
                        true
                ),
                Arguments.of(
                        new CreateUserRequest("Ivan", "Ivanov", "null", "a@b.ru", "+79999999999", "qwerty123", true),
                        new User("Ivan", "Ivanov", "null", "a@b.ru", "+79999999999", "qwerty123".getBytes(StandardCharsets.UTF_8)),
                        true
                ),
                Arguments.of(
                        new CreateUserRequest("vova", "Ivanov", "null", "a@b.ru", "+79999999999", "qwerty123", true),
                        new User("Ivan", "Ivanov", "null", "a@b.ru", "+79999999999", "qwerty123".getBytes(StandardCharsets.UTF_8)),
                        false
                ),
                Arguments.of(
                        new CreateUserRequest("vova", "Ivanov", null, "a@b.ru", "+7999999999", "qwerty123", true),
                        new User("Ivan", "Ivanov", "null", "a@b.ru", "+79999999999", "qwerty123".getBytes(StandardCharsets.UTF_8)),
                        false
                ),
                Arguments.of(
                        new CreateUserRequest("Ivan", "Ivanov", null, "a@b.ru", "+79999999999", "qwerty1234", true),
                        new User("Ivan", "Ivanov", null, "a@b.ru", "+79999999999", "qwerty123".getBytes(StandardCharsets.UTF_8)),
                        false
                )
        );
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("toEntityCases")
    void toEntityTest(CreateUserRequest req,
                      User expected,
                      boolean result) {

        PasswordEncoder encoder = mock(PasswordEncoder.class);
        when(encoder.encode(req.getPassword())).thenReturn(req.getPassword());

        User user = mapper.toEntity(req, encoder);

        if (result) {
            assertAll(
                    () -> assertEquals(expected.getName(), user.getName()),
                    () -> assertEquals(expected.getSurname(), user.getSurname()),
                    () -> assertEquals(expected.getFathersName(), user.getFathersName()),
                    () -> assertEquals(expected.getEmail(), user.getEmail()),
                    () -> assertEquals(expected.getPhone(), user.getPhone()),
                    () -> assertArrayEquals(expected.getPasswordHash(), user.getPasswordHash())
            );
        } else {
            boolean anyMismatch =
                    !java.util.Objects.equals(expected.getName(), user.getName()) ||
                            !java.util.Objects.equals(expected.getSurname(), user.getSurname()) ||
                            !java.util.Objects.equals(expected.getFathersName(), user.getFathersName()) ||
                            !java.util.Objects.equals(expected.getEmail(), user.getEmail()) ||
                            !java.util.Objects.equals(expected.getPhone(), user.getPhone()) ||
                            !java.util.Arrays.equals(expected.getPasswordHash(), user.getPasswordHash());

            assertTrue(anyMismatch);
        }
    }
}