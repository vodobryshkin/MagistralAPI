package ru.rtkmagistral.magistralapi.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.rtkmagistral.magistralapi.domain.jpa.User;
import ru.rtkmagistral.magistralapi.dto.company.CreateCompanyRequest;
import ru.rtkmagistral.magistralapi.dto.mail.ConfirmAccountMailRequest;
import ru.rtkmagistral.magistralapi.dto.user.CreateUserRequest;
import ru.rtkmagistral.magistralapi.dto.user.UserProfileDTO;
import ru.rtkmagistral.magistralapi.dto.user.UserResponse;
import ru.rtkmagistral.magistralapi.exception.UserException;
import ru.rtkmagistral.magistralapi.mapper.IUserMapper;
import ru.rtkmagistral.magistralapi.repository.IOrderRepository;
import ru.rtkmagistral.magistralapi.repository.UserRepository;
import ru.rtkmagistral.magistralapi.service.spec.ICompanyService;
import ru.rtkmagistral.magistralapi.service.spec.IMessageService;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    IUserMapper userMapper;
    @Mock
    ICompanyService companyService;
    @Mock
    IMessageService messageService;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    UserRepository userRepository;
    @Mock
    IOrderRepository orderRepository;

    @InjectMocks
    UserService userService;

    private CreateUserRequest userRequest() {
        return new CreateUserRequest(
                "Владимир",
                "Добрышкин",
                "Александрович",
                "vovadobryshkin@gmail.com",
                "+79614667210",
                "12345678",
                true
        );
    }

    private User buildUser(CreateUserRequest req) {
        return new User(
                req.getName(),
                req.getSurname(),
                req.getFathersName(),
                req.getEmail(),
                req.getPhone(),
                "hash".getBytes(StandardCharsets.UTF_8)
        );
    }

    @Test
    @DisplayName("createUser сохраняет пользователя с типом INDIVIDUAL и публикует сообщение подтверждения")
    void createUser_savesIndividual_andSendsConfirmation() {
        CreateUserRequest req = userRequest();
        User mapped = buildUser(req);

        when(userRepository.existsUserByEmail(req.getEmail())).thenReturn(false);
        when(userRepository.existsUserByPhone(req.getPhone())).thenReturn(false);
        when(userMapper.toEntity(eq(req), any(PasswordEncoder.class))).thenReturn(mapped);

        UserResponse response = userService.createUser(req);

        assertThat(response).isNotNull();

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getUserType()).isEqualTo(User.UserType.INDIVIDUAL);

        ArgumentCaptor<ConfirmAccountMailRequest> mailCaptor = ArgumentCaptor.forClass(ConfirmAccountMailRequest.class);
        verify(messageService).sendConfirmAccountMessageToQueue(mailCaptor.capture());
        assertThat(mailCaptor.getValue().getTo()).isEqualTo(req.getEmail());
        assertThat(mailCaptor.getValue().getName()).isEqualTo(req.getName());
        assertThat(mailCaptor.getValue().getFathersName()).isEqualTo(req.getFathersName());
    }

    @Test
    @DisplayName("createUser передаёт пустую строку как отчество, если оно null")
    void createUser_nullFathersName_normalisedToEmpty() {
        CreateUserRequest req = userRequest();
        req.setFathersName(null);

        when(userRepository.existsUserByEmail(req.getEmail())).thenReturn(false);
        when(userRepository.existsUserByPhone(req.getPhone())).thenReturn(false);
        when(userMapper.toEntity(eq(req), any(PasswordEncoder.class))).thenReturn(buildUser(req));

        userService.createUser(req);

        ArgumentCaptor<ConfirmAccountMailRequest> mailCaptor = ArgumentCaptor.forClass(ConfirmAccountMailRequest.class);
        verify(messageService).sendConfirmAccountMessageToQueue(mailCaptor.capture());
        assertThat(mailCaptor.getValue().getFathersName()).isEqualTo("");
    }

    @Test
    @DisplayName("createUser кидает USER_WITH_THIS_EMAIL_ALREADY_EXISTS при дубликате email")
    void createUser_duplicateEmail_throws() {
        CreateUserRequest req = userRequest();
        when(userRepository.existsUserByEmail(req.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(req))
                .isInstanceOf(UserException.class)
                .hasMessage("USER_WITH_THIS_EMAIL_ALREADY_EXISTS");

        verify(userRepository, never()).save(any());
        verify(messageService, never()).sendConfirmAccountMessageToQueue(any());
    }

    @Test
    @DisplayName("createUser кидает USER_WITH_THIS_PHONE_ALREADY_EXISTS при дубликате телефона")
    void createUser_duplicatePhone_throws() {
        CreateUserRequest req = userRequest();
        when(userRepository.existsUserByEmail(req.getEmail())).thenReturn(false);
        when(userRepository.existsUserByPhone(req.getPhone())).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(req))
                .isInstanceOf(UserException.class)
                .hasMessage("USER_WITH_THIS_PHONE_ALREADY_EXISTS");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("createLegalUser сохраняет пользователя как BUSINESS и создаёт компанию")
    void createLegalUser_savesBusiness_andCreatesCompany() {
        CreateUserRequest userReq = userRequest();
        CreateCompanyRequest companyReq = new CreateCompanyRequest(
                "ООО Магистраль", "1234567890", "123456789", "62.01", true
        );
        User mapped = buildUser(userReq);

        when(userRepository.existsUserByEmail(userReq.getEmail())).thenReturn(false);
        when(userRepository.existsUserByPhone(userReq.getPhone())).thenReturn(false);
        when(userMapper.toEntity(eq(userReq), any(PasswordEncoder.class))).thenReturn(mapped);

        userService.createLegalUser(userReq, companyReq);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getUserType()).isEqualTo(User.UserType.BUSINESS);

        verify(companyService).createCompany(eq(companyReq), eq(mapped));
        verify(messageService).sendConfirmAccountMessageToQueue(any(ConfirmAccountMailRequest.class));
    }

    @Test
    @DisplayName("verifyUser помечает пользователя как verified и возвращает профиль")
    void verifyUser_existingUser_marksVerifiedAndReturnsProfile() {
        User user = buildUser(userRequest());
        user.setUserType(User.UserType.INDIVIDUAL);
        when(userRepository.findUserByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(orderRepository.countOrdersByUser(user)).thenReturn(3L);

        UserProfileDTO profile = userService.verifyUser(user.getEmail());

        assertThat(user.isVerified()).isTrue();
        assertThat(profile.getEmail()).isEqualTo(user.getEmail());
        assertThat(profile.getPhone()).isEqualTo(user.getPhone());
        assertThat(profile.getUserType()).isEqualTo(User.UserType.INDIVIDUAL);
        assertThat(profile.getAmountOfOrders()).isEqualTo(3L);
        assertThat(profile.isVerified()).isTrue();
    }

    @Test
    @DisplayName("verifyUser кидает USER_NOT_FOUND для несуществующего пользователя")
    void verifyUser_missing_throws() {
        when(userRepository.findUserByEmail("nope@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.verifyUser("nope@example.com"))
                .isInstanceOf(UserException.class)
                .hasMessage("USER_NOT_FOUND");
    }

    @Test
    @DisplayName("getUserProfile возвращает данные при существующем пользователе")
    void getUserProfile_existingUser_returnsProfile() {
        User user = buildUser(userRequest());
        user.setUserType(User.UserType.BUSINESS);
        user.setVerified(true);

        when(userRepository.findUserByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(orderRepository.countOrdersByUser(user)).thenReturn(7L);

        UserProfileDTO profile = userService.getUserProfile(user.getEmail());

        assertThat(profile.getEmail()).isEqualTo(user.getEmail());
        assertThat(profile.getAmountOfOrders()).isEqualTo(7L);
        assertThat(profile.isVerified()).isTrue();
        assertThat(profile.getUserType()).isEqualTo(User.UserType.BUSINESS);
    }

    @Test
    @DisplayName("getUserProfile кидает USER_NOT_FOUND если пользователя нет")
    void getUserProfile_missing_throws() {
        when(userRepository.findUserByEmail(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserProfile("nope@example.com"))
                .isInstanceOf(UserException.class)
                .hasMessage("USER_NOT_FOUND");
    }

    @Test
    @DisplayName("checkUserExists делегирует репозиторию")
    void checkUserExists_delegatesToRepo() {
        when(userRepository.existsUserByEmail("a@b.ru")).thenReturn(true);
        assertThat(userService.checkUserExists("a@b.ru")).isTrue();

        when(userRepository.existsUserByEmail("c@d.ru")).thenReturn(false);
        assertThat(userService.checkUserExists("c@d.ru")).isFalse();
    }
}
