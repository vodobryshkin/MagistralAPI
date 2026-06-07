package ru.rtkmagistral.magistralapi.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.rtkmagistral.magistralapi.dto.pricing.DeliveryType;
import ru.rtkmagistral.magistralapi.dto.pricing.PriceCalculationResult;
import ru.rtkmagistral.magistralapi.exception.AppExceptionHandler;
import ru.rtkmagistral.magistralapi.service.spec.IJWTService;
import ru.rtkmagistral.magistralapi.service.spec.IPriceQuoteService;
import ru.rtkmagistral.magistralapi.support.WebTestSupport;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PriceController.class)
@Import({AppExceptionHandler.class, WebTestSupport.class})
@org.springframework.test.context.TestPropertySource(properties = "suitcase.price-coefficient=0.95")
class PriceControllerIT {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    IPriceQuoteService priceQuoteService;

    @MockitoBean
    IJWTService jwtService;

    private static final String VALID_JSON = """
        {
            "shipping_address": "г. Москва, ул. Тверская, д. 1",
            "arrival_address": "г. Москва, ул. Арбат, д. 10",
            "length": 100,
            "width": 100,
            "height": 100,
            "weight": 1000,
            "cost_of_investment": 100000,
            "nature_of_investment": "HOUSEHOLD_CHEMICALS"
        }
        """;

    @Test
    @DisplayName("POST /orders/price — валидный запрос возвращает 200 и цену")
    void orderPrice_returns200() throws Exception {
        when(priceQuoteService.quote(any()))
                .thenReturn(new PriceCalculationResult(123456L, 3, DeliveryType.DOOR_DOOR, 1.5));

        mvc.perform(post("/api/v1/orders/price")
                        .with(user("vova@example.com").roles("VERIFIED_USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price_kopeika").value(123456))
                .andExpect(jsonPath("$.chargeable_weight_kg").value(1.5));
    }

    @Test
    @DisplayName("POST /suitcases/price — валидный запрос возвращает 200 и цену c коэффициентом 0.95")
    void suitcasePrice_returns200() throws Exception {
        when(priceQuoteService.quote(any(), org.mockito.ArgumentMatchers.eq(0.95)))
                .thenReturn(new PriceCalculationResult(777L, 1, DeliveryType.WINDOW_WINDOW, 0.5));

        mvc.perform(post("/api/v1/suitcases/price")
                        .with(user("vova@example.com").roles("VERIFIED_USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price_kopeika").value(777));
    }

    @Test
    @DisplayName("POST /orders/price с пустым адресом — 422")
    void orderPrice_blankAddress_returns422() throws Exception {
        String json = VALID_JSON.replace("г. Москва, ул. Тверская, д. 1", "");

        mvc.perform(post("/api/v1/orders/price")
                        .with(user("vova@example.com").roles("VERIFIED_USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    @DisplayName("POST /orders/price с нулевым весом — 422")
    void orderPrice_zeroWeight_returns422() throws Exception {
        String json = VALID_JSON.replace("\"weight\": 1000", "\"weight\": 0");

        mvc.perform(post("/api/v1/orders/price")
                        .with(user("vova@example.com").roles("VERIFIED_USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnprocessableContent());
    }
}
