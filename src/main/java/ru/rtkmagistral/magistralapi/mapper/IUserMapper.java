package ru.rtkmagistral.magistralapi.mapper;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.rtkmagistral.magistralapi.domain.jpa.User;
import ru.rtkmagistral.magistralapi.dto.user.CreateUserRequest;
import ru.rtkmagistral.magistralapi.dto.user.UserBlock;

/**
 * Маппер для перевода между DTO, которое приходит на добавление пользователя в системе, и сущностью "Пользователь".
 */
@Mapper(componentModel = "spring")
public interface IUserMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userType", ignore = true)
    @Mapping(target = "passwordHash",
            expression = "java(passwordEncoder.encode(createUserRequest.getPassword())" +
            ".getBytes(java.nio.charset.StandardCharsets.UTF_8))")
    User toEntity(CreateUserRequest createUserRequest, @Context PasswordEncoder passwordEncoder);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "verified", ignore = true)
    @Mapping(target = "userType", ignore = true)
    @Mapping(target = "companyName",
            expression = "java(companyName.isEmpty() ? null ? companyName.getTitle())")
    UserBlock toDto(User user);
}
