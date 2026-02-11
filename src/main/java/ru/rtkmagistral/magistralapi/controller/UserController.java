package ru.rtkmagistral.magistralapi.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.rtkmagistral.magistralapi.dto.user.CreateUserRequest;
import ru.rtkmagistral.magistralapi.dto.user.UserProfileDTO;
import ru.rtkmagistral.magistralapi.dto.user.UserResponse;
import ru.rtkmagistral.magistralapi.security.authorization.ForAuthenticatedUsers;
import ru.rtkmagistral.magistralapi.service.spec.IAuthenticationService;
import ru.rtkmagistral.magistralapi.service.spec.IJWTService;
import ru.rtkmagistral.magistralapi.service.spec.IUserService;

/**
 * Контроллер, принимающий запросы идущие на эндпойнт "/users"
 */
@RestController
@RequestMapping(
        value = "/users",
        produces = MediaType.APPLICATION_JSON_VALUE
)
@RequiredArgsConstructor
public class UserController {
    private final IUserService userService;
    private final IJWTService jwtService;
    private final IAuthenticationService authenticationService;

    /**
     * Метод, принимающий POST-запросы идущие на эндпойнт "/users".
     * Логика метода заключается в добавлении пользователя в систему.
     *
     * @param createUserRequest запрос на создание пользователя.
     * @return ответ на запрос с информацией о прошедшей операции.
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserResponse> createUser(@RequestBody @Valid CreateUserRequest createUserRequest) {
        UserResponse userResponse = userService.createUser(createUserRequest);
        UserProfileDTO userProfileDTO = userService.getUserProfile(createUserRequest.getEmail());
        userResponse.setUserProfileDTO(userProfileDTO);

        String accessToken = jwtService.generateAccessToken(createUserRequest.getEmail(), "ROLE_UNVERIFIED_USER");
        String refreshToken = jwtService.generateRefreshToken(createUserRequest.getEmail(), "ROLE_UNVERIFIED_USER");

        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/")
                .build();

        authenticationService.createResendToken(createUserRequest.getEmail());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(userResponse);
    }

    @GetMapping("/me")
    @ForAuthenticatedUsers
    public ResponseEntity<UserProfileDTO> readUserProfile(Authentication authentication) {
        return ResponseEntity
                .ok()
                .body(userService.getUserProfile(authentication.getName()));
    }
}
