package ru.rtkmagistral.magistralapi.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.rtkmagistral.magistralapi.dto.user.CreateUserRequest;
import ru.rtkmagistral.magistralapi.dto.user.UserResponse;
import ru.rtkmagistral.magistralapi.service.spec.IUserService;

/**
 * Контроллер, принимающий запросы идущие на эндпойнт "/users"
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final IUserService userService;

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
}
