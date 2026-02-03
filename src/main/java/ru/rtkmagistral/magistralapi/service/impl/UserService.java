package ru.rtkmagistral.magistralapi.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.rtkmagistral.magistralapi.domain.jpa.User;
import ru.rtkmagistral.magistralapi.domain.redis.ConfirmationLink;
import ru.rtkmagistral.magistralapi.dto.company.CreateCompanyRequest;
import ru.rtkmagistral.magistralapi.dto.mail.ConfirmAccountMailRequest;
import ru.rtkmagistral.magistralapi.dto.user.CreateUserRequest;
import ru.rtkmagistral.magistralapi.dto.user.UserResponse;
import ru.rtkmagistral.magistralapi.dto.user.UserResponses;
import ru.rtkmagistral.magistralapi.exception.UserException;
import ru.rtkmagistral.magistralapi.mapper.IUserMapper;
import ru.rtkmagistral.magistralapi.repository.UserRepository;
import ru.rtkmagistral.magistralapi.service.spec.ICompanyService;
import ru.rtkmagistral.magistralapi.service.spec.IConfirmationLinkService;
import ru.rtkmagistral.magistralapi.service.spec.IMessageService;
import ru.rtkmagistral.magistralapi.service.spec.IUserService;

import java.util.Optional;

/**
 * Сервис для процессов бизнес-логики, связанных с доменной сущностью "Пользователь".
 */
@Service
@RequiredArgsConstructor
public class UserService implements IUserService {
    private final IUserMapper userMapper;

    private final ICompanyService companyService;
    private final IMessageService messageService;
    private final IConfirmationLinkService confirmationLinkService;

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

        String id = createConfirmationLink(createUserRequest.getEmail());
        sendConfirmationMessageForUser(user, id);

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

        String id = createConfirmationLink(createUserRequest.getEmail());
        sendConfirmationMessageForUser(user, id);

        return UserResponses.USER_CREATED;
    }

    /**
     * Метод для подтверждения аккаунта пользователя.
     *
     * @param email электронная почта пользователя, аккаунт которого нужно подтвердить.
     */
    @Override
    @Transactional
    public void verifyUser(String email) {
        Optional<User> userOptional = userRepository.findUserByEmail(email);

        if (userOptional.isEmpty()) {
            throw new UserException("USER_NOT_FOUND");
        }

        User user = userOptional.get();
        user.setVerified(true);
    }

    private User validateUser(CreateUserRequest createUserRequest) {
        if (userRepository.existsUserByEmail(createUserRequest.getEmail())) {
            throw new UserException("USER_WITH_THIS_EMAIL_ALREADY_EXISTS");
        }

        if (userRepository.existsUserByPhone(createUserRequest.getPhone())) {
            throw new UserException("USER_WITH_THIS_PHONE_ALREADY_EXISTS");
        }

        return userMapper.toEntity(createUserRequest, passwordEncoder);
    }

    private void sendConfirmationMessageForUser(User user, String id) {
        ConfirmAccountMailRequest request = new ConfirmAccountMailRequest(
                user.getName(),
                user.getFathersName() == null? "": user.getFathersName(),
                user.getEmail(),
                "Подтверждение аккаунта",
                id
        );

        messageService.sendConfirmAccountMessageToQueue(request);
    }

    private String createConfirmationLink(String email) {
        ConfirmationLink confirmationLink = confirmationLinkService.generateConfirmationLink(email);
        confirmationLinkService.saveConfirmationLink(confirmationLink);

        return confirmationLink.getId().toString();
    }

}
