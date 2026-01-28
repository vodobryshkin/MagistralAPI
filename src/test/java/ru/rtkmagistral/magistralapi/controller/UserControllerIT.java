package ru.rtkmagistral.magistralapi.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.rtkmagistral.magistralapi.config.SecurityConfig;
import ru.rtkmagistral.magistralapi.exception.AppExceptionHandler;
import ru.rtkmagistral.magistralapi.service.spec.IUserService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, AppExceptionHandler.class})
class UserControllerWebTest {

    @Autowired MockMvc mvc;

    @MockitoBean
    IUserService userService;

    @Test
    @DisplayName("Корректный запрос с отчеством. Должен вернуть 201.")
    void validRequest_withFathersName_shouldReturn201() throws Exception {
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
    @DisplayName("Некорректный запрос с пустым телом. Должен вернуть 400.")
    void invalidRequest_emptyBody_shouldReturn422() throws Exception {
        String json = """
            {
            }
        """;
        mvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Некорректный запрос с неправильным форматом JSON. Должен вернуть 400.")
    void invalidRequest_invalidJSONStructure_shouldReturn201() throws Exception {
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
}

