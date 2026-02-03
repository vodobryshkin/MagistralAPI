package ru.rtkmagistral.magistralapi.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.rtkmagistral.magistralapi.dto.token.VerifyResponse;
import ru.rtkmagistral.magistralapi.dto.user.CreateUserRequest;
import ru.rtkmagistral.magistralapi.dto.user.UserResponse;
import ru.rtkmagistral.magistralapi.service.spec.IConfirmationLinkService;
import ru.rtkmagistral.magistralapi.service.spec.IUserService;
import ru.rtkmagistral.magistralapi.validation.formats.uuid.UUID;

/**
 * Контроллер, принимающий запросы идущие на эндпойнт "/users"
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final IUserService userService;
    private final IConfirmationLinkService confirmationLinkService;

    /**
     * Метод, принимающий POST-запросы идущие на эндпойнт "/users".
     * Логика метода заключается в добавлении пользователя в систему.
     *
     * @param createUserRequest запрос на создание пользователя.
     * @return ответ на запрос с информацией о прошедшей операции.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createUser(@RequestBody @Valid CreateUserRequest createUserRequest) {
        return userService.createUser(createUserRequest);
    }

    /**
     * Метод, принимающий PATCH-запросы идущие на эндпойнт "/users/{id}".
     * Логика метода заключается в подтверждении пользователя в системе.
     *
     * @param id ссылки на подтверждение.
     * @return ответ на запрос с информацией о прошедшей операции.
     */
    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public VerifyResponse verifyUser(@PathVariable @Valid @UUID String id) {
        VerifyResponse verifyResponse = confirmationLinkService.verifyConfirmationLink(id);
        String email = verifyResponse.getMessage();

        userService.verifyUser(email);
        verifyResponse.setMessage(null);

        return verifyResponse;
    }
}
