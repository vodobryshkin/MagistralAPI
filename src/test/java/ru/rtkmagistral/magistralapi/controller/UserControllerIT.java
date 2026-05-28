package ru.rtkmagistral.magistralapi.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.rtkmagistral.magistralapi.dto.user.UserProfileDTO;
import ru.rtkmagistral.magistralapi.dto.user.UserResponse;
import ru.rtkmagistral.magistralapi.exception.AppExceptionHandler;
import ru.rtkmagistral.magistralapi.exception.UserException;
import ru.rtkmagistral.magistralapi.service.spec.IAuthenticationService;
import ru.rtkmagistral.magistralapi.service.spec.IJWTService;
import ru.rtkmagistral.magistralapi.service.spec.IUserService;
import ru.rtkmagistral.magistralapi.support.WebTestSupport;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import({AppExceptionHandler.class, WebTestSupport.class})
class UserControllerIT {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    IUserService userService;

    @MockitoBean
    IJWTService jwtService;

    @MockitoBean
    IAuthenticationService authenticationService;

    @Test
    @DisplayName("Корректный запрос с отчеством. Должен вернуть 201.")
    void validRequest_withFathersName_shouldReturn201() throws Exception {
        when(userService.createUser(any())).thenReturn(new UserResponse("CREATED"));
        when(userService.getUserProfile(any())).thenReturn(
                new UserProfileDTO("vovadobryshkin@gmail.com", "+79614667210", null, 0L, false));
        when(jwtService.generateAccessToken(any(), any(String.class))).thenReturn("Bearer access");
        when(jwtService.generateRefreshToken(any(), any(String.class))).thenReturn("refresh");

        String json = """
            {
                "name": "Владимир",
                "surname": "Добрышкин",
                "fathers_name": "Александрович",
                "email": "vovadobryshkin@gmail.com",
                "phone": "+79614667210",
                "agree_to_the_processing_of_personal_data": true,
                "password": "12345678"
            }
        """;
        mvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Корректный запрос без отчества. Должен вернуть 201.")
    void validRequest_withoutFathersName_shouldReturn201() throws Exception {
        when(userService.createUser(any())).thenReturn(new UserResponse("CREATED"));
        when(userService.getUserProfile(any())).thenReturn(
                new UserProfileDTO("vovadobryshkin@gmail.com", "+79614667210", null, 0L, false));
        when(jwtService.generateAccessToken(any(), any(String.class))).thenReturn("Bearer access");
        when(jwtService.generateRefreshToken(any(), any(String.class))).thenReturn("refresh");

        String json = """
            {
                "name": "Владимир",
                "surname": "Добрышкин",
                "email": "vovadobryshkin@gmail.com",
                "phone": "+79614667210",
                "agree_to_the_processing_of_personal_data": true,
                "password": "12345678"
            }
        """;
        mvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Некорректный запрос с неправильным именем. Должен вернуть 422.")
    void invalidRequest_incorrectName_shouldReturn422() throws Exception {
        String json = """
            {
                "name": "Vladimir",
                "surname": "Добрышкин",
                "email": "vovadobryshkin@gmail.com",
                "phone": "+79614667210",
                "agree_to_the_processing_of_personal_data": true,
                "password": "12345678"
            }
        """;
        mvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    @DisplayName("Некорректный запрос с неправильной фамилией. Должен вернуть 422.")
    void invalidRequest_incorrectSurname_shouldReturn422() throws Exception {
        String json = """
            {
                "name": "Владимир",
                "surname": "Dobryshkin",
                "email": "vovadobryshkin@gmail.com",
                "phone": "+79614667210",
                "agree_to_the_processing_of_personal_data": true,
                "password": "12345678"
            }
        """;
        mvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    @DisplayName("Некорректный запрос с неправильным отчеством. Должен вернуть 422.")
    void invalidRequest_incorrectFathersName_shouldReturn422() throws Exception {
        String json = """
            {
                "name": "Владимир",
                "surname": "Добрышкин",
                "fathers_name": "Aleksandrovich",
                "email": "vovadobryshkin@gmail.com",
                "phone": "+79614667210",
                "agree_to_the_processing_of_personal_data": true,
                "password": "12345678"
            }
        """;
        mvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    @DisplayName("Некорректный запрос с неправильным адресом электронной почты. Должен вернуть 422.")
    void invalidRequest_incorrectEmail_shouldReturn422() throws Exception {
        String json = """
            {
                "name": "Владимир",
                "surname": "Добрышкин",
                "email": "vovadobryshkingmail.com",
                "phone": "+79614667210",
                "agree_to_the_processing_of_personal_data": true,
                "password": "12345678"
            }
        """;
        mvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    @DisplayName("Некорректный запрос с неправильным номером телефона. Должен вернуть 422.")
    void invalidRequest_incorrectPhone_shouldReturn422() throws Exception {
        String json = """
            {
                "name": "Владимир",
                "surname": "Добрышкин",
                "email": "vovadobryshkin@gmail.com",
                "phone": "7961466721",
                "agree_to_the_processing_of_personal_data": true,
                "password": "12345678"
            }
        """;
        mvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    @DisplayName("Некорректный запрос с неправильным согласием на обработку данных. Должен вернуть 422.")
    void invalidRequest_incorrectAgreementToTheProcessingOfPersonalData_shouldReturn422() throws Exception {
        String json = """
            {
                "name": "Владимир",
                "surname": "Добрышкин",
                "email": "vovadobryshkin@gmail.com",
                "phone": "+79614667210",
                "agree_to_the_processing_of_personal_data": false,
                "password": "12345678"
            }
        """;
        mvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    @DisplayName("Некорректный запрос с неправильным форматом пароля. Должен вернуть 422.")
    void invalidRequest_incorrectPassword_shouldReturn422() throws Exception {
        String json = """
            {
                "name": "Владимир",
                "surname": "Добрышкин",
                "email": "vovadobryshkin@gmail.com",
                "phone": "+79614667210",
                "agree_to_the_processing_of_personal_data": true,
                "password": "аолаолфдпокупдоулфдцао"
            }
        """;
        mvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    @DisplayName("Некорректный запрос с пустым телом. Должен вернуть 422.")
    void invalidRequest_emptyBody_shouldReturn422() throws Exception {
        String json = """
            {
            }
        """;
        mvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    @DisplayName("Некорректный запрос с неправильным форматом JSON. Должен вернуть 400.")
    void invalidRequest_invalidJSONStructure_shouldReturn400() throws Exception {
        String json = """
            {
                "name": "Владимир",
                "surname": "Добрышкин"
                "fathers_name": "Александрович",
                "email": "vovadobryshkin@gmail.com",
                "phone": "+79614667210",
                "agree_to_the_processing_of_personal_data": true,
                "password": "12345678"
        """;
        mvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Сервис кидает USER_WITH_THIS_EMAIL_ALREADY_EXISTS — должен вернуть 409.")
    void serviceThrowsEmailConflict_shouldReturn409() throws Exception {
        when(userService.createUser(any())).thenThrow(new UserException("USER_WITH_THIS_EMAIL_ALREADY_EXISTS"));

        String json = """
            {
                "name": "Владимир",
                "surname": "Добрышкин",
                "email": "vovadobryshkin@gmail.com",
                "phone": "+79614667210",
                "agree_to_the_processing_of_personal_data": true,
                "password": "12345678"
            }
        """;
        mvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Сервис кидает USER_WITH_THIS_PHONE_ALREADY_EXISTS — должен вернуть 409.")
    void serviceThrowsPhoneConflict_shouldReturn409() throws Exception {
        when(userService.createUser(any())).thenThrow(new UserException("USER_WITH_THIS_PHONE_ALREADY_EXISTS"));

        String json = """
            {
                "name": "Владимир",
                "surname": "Добрышкин",
                "email": "vovadobryshkin@gmail.com",
                "phone": "+79614667210",
                "agree_to_the_processing_of_personal_data": true,
                "password": "12345678"
            }
        """;
        mvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isConflict());
    }
}
