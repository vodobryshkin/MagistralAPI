package ru.rtkmagistral.magistralapi.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.rtkmagistral.magistralapi.dto.user.UserProfileDTO;
import ru.rtkmagistral.magistralapi.dto.user.UserResponse;
import ru.rtkmagistral.magistralapi.exception.AppExceptionHandler;
import ru.rtkmagistral.magistralapi.exception.CompanyException;
import ru.rtkmagistral.magistralapi.service.spec.IAuthenticationService;
import ru.rtkmagistral.magistralapi.service.spec.IJWTService;
import ru.rtkmagistral.magistralapi.service.spec.IUserService;
import ru.rtkmagistral.magistralapi.support.WebTestSupport;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CompanyController.class)
@Import({AppExceptionHandler.class, WebTestSupport.class})
class CompanyControllerIT {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    IUserService userService;

    @MockitoBean
    IJWTService jwtService;

    @MockitoBean
    IAuthenticationService authenticationService;

    private static final String VALID_JSON = """
        {
            "user": {
                "name": "Владимир",
                "surname": "Добрышкин",
                "fathers_name": "Александрович",
                "email": "vova@example.com",
                "phone": "+79614667210",
                "agree_to_the_processing_of_personal_data": true,
                "password": "12345678"
            },
            "company": {
                "title": "ООО \\"Магистраль\\"",
                "inn": "1234567890",
                "kpp": "123456789",
                "okved": "62.01",
                "agree_to_the_processing_of_courier_services": true
            }
        }
        """;

    @Test
    @DisplayName("POST /companies — корректный запрос возвращает 201 с Authorization и Set-Cookie")
    void validRequest_returns201() throws Exception {
        when(userService.createLegalUser(any(), any())).thenReturn(new UserResponse("CREATED"));
        when(userService.getUserProfile("vova@example.com"))
                .thenReturn(new UserProfileDTO("vova@example.com", "+79614667210", null, 0L, false));
        when(jwtService.generateAccessToken(any(), any(String.class))).thenReturn("Bearer access");
        when(jwtService.generateRefreshToken(any(), any(String.class))).thenReturn("refresh");

        mvc.perform(post("/api/v1/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_JSON))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.AUTHORIZATION, "Bearer access"))
                .andExpect(header().exists(HttpHeaders.SET_COOKIE));
    }

    @Test
    @DisplayName("POST /companies без OKVED — допустимо, должен вернуть 201")
    void requestWithoutOkved_returns201() throws Exception {
        when(userService.createLegalUser(any(), any())).thenReturn(new UserResponse("CREATED"));
        when(userService.getUserProfile(any()))
                .thenReturn(new UserProfileDTO("vova@example.com", "+79614667210", null, 0L, false));
        when(jwtService.generateAccessToken(any(), any(String.class))).thenReturn("Bearer access");
        when(jwtService.generateRefreshToken(any(), any(String.class))).thenReturn("refresh");

        String json = """
            {
                "user": {
                    "name": "Владимир",
                    "surname": "Добрышкин",
                    "fathers_name": "Александрович",
                    "email": "vova@example.com",
                    "phone": "+79614667210",
                    "agree_to_the_processing_of_personal_data": true,
                    "password": "12345678"
                },
                "company": {
                    "title": "ООО Магистраль",
                    "inn": "1234567890",
                    "kpp": "123456789",
                    "agree_to_the_processing_of_courier_services": true
                }
            }
            """;

        mvc.perform(post("/api/v1/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /companies с неверным форматом ИНН — 422")
    void invalidInn_returns422() throws Exception {
        String json = """
            {
                "user": {
                    "name": "Владимир",
                    "surname": "Добрышкин",
                    "email": "vova@example.com",
                    "phone": "+79614667210",
                    "agree_to_the_processing_of_personal_data": true,
                    "password": "12345678"
                },
                "company": {
                    "title": "ООО Магистраль",
                    "inn": "12abc",
                    "kpp": "123456789",
                    "okved": "62.01",
                    "agree_to_the_processing_of_courier_services": true
                }
            }
            """;
        mvc.perform(post("/api/v1/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    @DisplayName("POST /companies с неверным форматом КПП — 422")
    void invalidKpp_returns422() throws Exception {
        String json = """
            {
                "user": {
                    "name": "Владимир",
                    "surname": "Добрышкин",
                    "email": "vova@example.com",
                    "phone": "+79614667210",
                    "agree_to_the_processing_of_personal_data": true,
                    "password": "12345678"
                },
                "company": {
                    "title": "ООО Магистраль",
                    "inn": "1234567890",
                    "kpp": "1234567",
                    "okved": "62.01",
                    "agree_to_the_processing_of_courier_services": true
                }
            }
            """;
        mvc.perform(post("/api/v1/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    @DisplayName("POST /companies с несогласием на курьерские услуги — 422")
    void agreementMissing_returns422() throws Exception {
        String json = """
            {
                "user": {
                    "name": "Владимир",
                    "surname": "Добрышкин",
                    "email": "vova@example.com",
                    "phone": "+79614667210",
                    "agree_to_the_processing_of_personal_data": true,
                    "password": "12345678"
                },
                "company": {
                    "title": "ООО Магистраль",
                    "inn": "1234567890",
                    "kpp": "123456789",
                    "okved": "62.01",
                    "agree_to_the_processing_of_courier_services": false
                }
            }
            """;
        mvc.perform(post("/api/v1/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    @DisplayName("POST /companies — пользователь с таким email уже есть — 409")
    void userEmailAlreadyExists_returns409() throws Exception {
        when(userService.createLegalUser(any(), any()))
                .thenThrow(new ru.rtkmagistral.magistralapi.exception.UserException("USER_WITH_THIS_EMAIL_ALREADY_EXISTS"));

        mvc.perform(post("/api/v1/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_JSON))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /companies — компания c таким ИНН уже существует — 409")
    void companyAlreadyExists_returns409() throws Exception {
        when(userService.createLegalUser(any(), any()))
                .thenThrow(new CompanyException("COMPANY_ALREADY_EXISTS_IN_DATABASE"));

        mvc.perform(post("/api/v1/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_JSON))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /companies — компании с ИНН нет в Dadata — 422")
    void innNotInDadata_returns422() throws Exception {
        when(userService.createLegalUser(any(), any()))
                .thenThrow(new CompanyException("COMPANY_WITH_THIS_INN_NOT_EXISTS_IN_DADATA"));

        mvc.perform(post("/api/v1/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_JSON))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    @DisplayName("POST /companies — название не совпадает с Dadata — 422")
    void titleMismatch_returns422() throws Exception {
        when(userService.createLegalUser(any(), any()))
                .thenThrow(new CompanyException("TITLE_NOT_MATCHES_WITH_DADATA"));

        mvc.perform(post("/api/v1/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_JSON))
                .andExpect(status().isUnprocessableContent());
    }
}
