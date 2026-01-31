package ru.rtkmagistral.magistralapi.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.rtkmagistral.magistralapi.dto.company.CompanyDTO;
import ru.rtkmagistral.magistralapi.dto.company.CreateCompanyRequest;
import ru.rtkmagistral.magistralapi.dto.user.CreateUserRequest;
import ru.rtkmagistral.magistralapi.dto.user.UserResponse;
import ru.rtkmagistral.magistralapi.service.spec.IUserService;

/**
 * Контроллер, принимающий запросы идущие на эндпойнт "/company"
 */
@RestController
@RequestMapping("/company")
@RequiredArgsConstructor
public class CompanyController {
    private final IUserService userService;

    /**
     * Метод, принимающий POST-запросы идущие на эндпойнт "/company".
     * Логика метода заключается в добавлении пользователя и компании, которую он представляет, в систему.
     *
     * @param companyDTO запрос на создание пользователя и компании.
     * @return ответ на запрос с информацией о прошедшей операции.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createCompany(@RequestBody @Valid CompanyDTO companyDTO) {
        CreateUserRequest createUserRequest = companyDTO.getCreateUserRequest();
        CreateCompanyRequest companyRequest = companyDTO.getCreateCompanyRequest();

        return userService.createLegalUser(createUserRequest, companyRequest);
    }
}
