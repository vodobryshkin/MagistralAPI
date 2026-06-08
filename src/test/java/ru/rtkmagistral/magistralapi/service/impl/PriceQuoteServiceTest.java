package ru.rtkmagistral.magistralapi.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.rtkmagistral.magistralapi.domain.jpa.NatureOfInvestment;
import ru.rtkmagistral.magistralapi.dto.pricing.DeliveryType;
import ru.rtkmagistral.magistralapi.dto.pricing.PriceCalculationInput;
import ru.rtkmagistral.magistralapi.dto.pricing.PriceCalculationResult;
import ru.rtkmagistral.magistralapi.dto.pricing.PriceQuoteRequest;
import ru.rtkmagistral.magistralapi.dto.pricing.ResolvedLocation;
import ru.rtkmagistral.magistralapi.service.spec.ICityResolver;
import ru.rtkmagistral.magistralapi.service.spec.IPriceCalculationService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PriceQuoteServiceTest {

    @Mock
    ICityResolver cityResolver;
    @Mock
    IPriceCalculationService priceCalculationService;

    @InjectMocks
    PriceQuoteService priceQuoteService;

    private PriceQuoteRequest request(NatureOfInvestment nature) {
        return new PriceQuoteRequest(
                "г. Москва, ул. Тверская, д. 1",
                true,
                "г. Санкт-Петербург, Невский пр., д. 1",
                false,
                100, 200, 300,
                2000,
                500_000L,
                nature
        );
    }

    @Test
    @DisplayName("quote резолвит оба города и собирает корректный PriceCalculationInput")
    void quote_buildsInput() {
        when(cityResolver.resolve("г. Москва, ул. Тверская, д. 1"))
                .thenReturn(new ResolvedLocation("Москва", 1.0, 10, false));
        when(cityResolver.resolve("г. Санкт-Петербург, Невский пр., д. 1"))
                .thenReturn(new ResolvedLocation("Санкт-Петербург", 1.15, null, false));
        when(priceCalculationService.calculate(any()))
                .thenReturn(new PriceCalculationResult(99_000L, 2, DeliveryType.WINDOW_DOOR, 2.0));

        PriceCalculationResult result = priceQuoteService.quote(request(NatureOfInvestment.JEWELRY));

        assertThat(result.priceInKopeika()).isEqualTo(99_000L);

        ArgumentCaptor<PriceCalculationInput> captor = ArgumentCaptor.forClass(PriceCalculationInput.class);
        verify(priceCalculationService).calculate(captor.capture());
        PriceCalculationInput input = captor.getValue();

        assertThat(input.senderCity()).isEqualTo("Москва");
        assertThat(input.receiverCity()).isEqualTo("Санкт-Петербург");
        assertThat(input.senderIsOffice()).isTrue();
        assertThat(input.receiverIsOffice()).isFalse();
        assertThat(input.weightGr()).isEqualTo(2000);
        assertThat(input.natureOfInvestment()).isEqualTo(NatureOfInvestment.JEWELRY);
        assertThat(input.declaredValueKopeika()).isEqualTo(500_000L);
        assertThat(input.senderCoefficient()).isEqualTo(1.0);
        assertThat(input.receiverCoefficient()).isEqualTo(1.15);
        assertThat(input.senderRemotePerKg()).isEqualTo(10);
        assertThat(input.receiverRemotePerKg()).isNull();
    }

    @Test
    @DisplayName("quote подставляет категорию OTHER, если характер вложения не передан")
    void quote_defaultsNatureToOther() {
        when(cityResolver.resolve(any()))
                .thenReturn(new ResolvedLocation("Москва", 1.0, null, false));
        when(priceCalculationService.calculate(any()))
                .thenReturn(new PriceCalculationResult(1L, 0, DeliveryType.DOOR_DOOR, 0.5));

        priceQuoteService.quote(request(null));

        ArgumentCaptor<PriceCalculationInput> captor = ArgumentCaptor.forClass(PriceCalculationInput.class);
        verify(priceCalculationService).calculate(captor.capture());
        assertThat(captor.getValue().natureOfInvestment()).isEqualTo(NatureOfInvestment.OTHER);
    }

    @Test
    @DisplayName("calculatePriceInKopeika возвращает цену из расчёта")
    void calculatePriceInKopeika_returnsPrice() {
        when(cityResolver.resolve(any()))
                .thenReturn(new ResolvedLocation("Москва", 1.0, null, false));
        when(priceCalculationService.calculate(any()))
                .thenReturn(new PriceCalculationResult(42_000L, 1, DeliveryType.DOOR_DOOR, 1.0));

        long price = priceQuoteService.calculatePriceInKopeika(
                "Москва", "Москва", false, false,
                1000, 100, 100, 100,
                NatureOfInvestment.HOUSEHOLD_CHEMICALS, 100_000L);

        assertThat(price).isEqualTo(42_000L);
    }

    @Test
    @DisplayName("quote с коэффициентом домножает итоговую цену в самом конце")
    void quote_withMultiplier_appliesCoefficient() {
        when(cityResolver.resolve(any()))
                .thenReturn(new ResolvedLocation("Москва", 1.0, null, false));
        when(priceCalculationService.calculate(any()))
                .thenReturn(new PriceCalculationResult(100_000L, 1, DeliveryType.DOOR_DOOR, 1.0));

        // 100000 * 0.95 = 95000
        assertThat(priceQuoteService.quote(request(NatureOfInvestment.OTHER), 0.95).priceInKopeika())
                .isEqualTo(95_000L);
    }

    @Test
    @DisplayName("calculatePriceInKopeika с коэффициентом 0.95 округляет результат до копейки")
    void calculatePriceInKopeika_withMultiplier_rounds() {
        when(cityResolver.resolve(any()))
                .thenReturn(new ResolvedLocation("Москва", 1.0, null, false));
        when(priceCalculationService.calculate(any()))
                .thenReturn(new PriceCalculationResult(12_345L, 1, DeliveryType.DOOR_DOOR, 1.0));

        // 12345 * 0.95 = 11727.75 -> HALF_UP -> 11728
        long price = priceQuoteService.calculatePriceInKopeika(
                "Москва", "Москва", false, false,
                1000, 100, 100, 100,
                NatureOfInvestment.HOUSEHOLD_CHEMICALS, 100_000L, 0.95);

        assertThat(price).isEqualTo(11_728L);
    }
}
