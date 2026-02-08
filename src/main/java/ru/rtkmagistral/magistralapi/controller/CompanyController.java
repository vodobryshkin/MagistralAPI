package ru.rtkmagistral.magistralapi.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.rtkmagistral.magistralapi.dto.company.CompanyDTO;
import ru.rtkmagistral.magistralapi.dto.company.CreateCompanyRequest;
import ru.rtkmagistral.magistralapi.dto.user.CreateUserRequest;
import ru.rtkmagistral.magistralapi.dto.user.UserResponse;
import ru.rtkmagistral.magistralapi.service.spec.IAuthenticationService;
import ru.rtkmagistral.magistralapi.service.spec.IJWTService;
import ru.rtkmagistral.magistralapi.service.spec.IUserService;

/**
 * Контроллер, принимающий запросы идущие на эндпойнт "/company"
 */
@RestController
@RequestMapping("/companies")
@RequiredArgsConstructor
public class CompanyController {
    private final IUserService userService;
    private final IJWTService jwtService;
    private final IAuthenticationService authenticationService;


    /**
     * Метод, принимающий POST-запросы идущие на эндпойнт "/company".
     * Логика метода заключается в добавлении пользователя и компании, которую он представляет, в систему.
     *
     * @param companyDTO запрос на создание пользователя и компании.
     * @return ответ на запрос с информацией о прошедшей операции.
     */
    @PostMapping
    public ResponseEntity<UserResponse> createCompany(@RequestBody @Valid CompanyDTO companyDTO) {
        CreateUserRequest createUserRequest = companyDTO.getCreateUserRequest();
        CreateCompanyRequest companyRequest = companyDTO.getCreateCompanyRequest();

        UserResponse userResponse = userService.createLegalUser(createUserRequest, companyRequest);

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
}
