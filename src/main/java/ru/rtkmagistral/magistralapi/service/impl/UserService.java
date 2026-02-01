package ru.rtkmagistral.magistralapi.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.rtkmagistral.magistralapi.domain.jpa.User;
import ru.rtkmagistral.magistralapi.dto.company.CreateCompanyRequest;
import ru.rtkmagistral.magistralapi.dto.user.CreateUserRequest;
import ru.rtkmagistral.magistralapi.dto.user.UserResponse;
import ru.rtkmagistral.magistralapi.dto.user.UserResponses;
import ru.rtkmagistral.magistralapi.exception.UserWithThisCreditsAlreadyExistsException;
import ru.rtkmagistral.magistralapi.mapper.IUserMapper;
import ru.rtkmagistral.magistralapi.repository.UserRepository;
import ru.rtkmagistral.magistralapi.service.spec.ICompanyService;
import ru.rtkmagistral.magistralapi.service.spec.IUserService;

/**
 * Сервис для процессов бизнес-логики, связанных с доменной сущностью "Пользователь".
 */
@Service
@RequiredArgsConstructor
public class UserService implements IUserService {
    private final IUserMapper userMapper;

    private final ICompanyService companyService;

    private final PasswordEncoder passwordEncoder;

    private final UserRepository userRepository;

    /**
     * Метод для создания пользователя (физическое лицо) в системе (вызывается при регистрации пользователя)
     *
     * @param createUserRequest данные, необходимые для создания пользователя.
     * @return ответ на создание пользователя.
     */
    @Override
    public UserResponse createUser(CreateUserRequest createUserRequest) {
        User user = validateUser(createUserRequest);
        user.setUserType(User.UserType.INDIVIDUAL);

        userRepository.save(user);

        return UserResponses.USER_CREATED;
    }

    /**
     * Метод для создания пользователя (юридическое лицо) в системе (вызывается при регистрации пользователя)
     *
     * @param createUserRequest    данные, необходимые для создания пользователя.
     * @param createCompanyRequest данные, необходимые для создания компании.
     * @return ответ на создание пользователя.
     */
    @Override
    @Transactional
    public UserResponse createLegalUser(CreateUserRequest createUserRequest, CreateCompanyRequest createCompanyRequest) {
        User user = validateUser(createUserRequest);
        user.setUserType(User.UserType.BUSINESS);
        userRepository.save(user);
        companyService.createCompany(createCompanyRequest, user);

        return UserResponses.USER_CREATED;
    }

    private User validateUser(CreateUserRequest createUserRequest) {
        if (userRepository.existsUserByEmail(createUserRequest.getEmail())) {
            throw new UserWithThisCreditsAlreadyExistsException("USER_WITH_THIS_EMAIL_ALREADY_EXISTS");
        }

        if (userRepository.existsUserByPhone(createUserRequest.getPhone())) {
            throw new UserWithThisCreditsAlreadyExistsException("USER_WITH_THIS_PHONE_ALREADY_EXISTS");
        }

        return userMapper.toEntity(createUserRequest, passwordEncoder);
    }
}
