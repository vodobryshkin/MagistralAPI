package ru.rtkmagistral.magistralapi.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.rtkmagistral.magistralapi.domain.jpa.NatureOfInvestment;
import ru.rtkmagistral.magistralapi.dto.pricing.DeliveryType;
import ru.rtkmagistral.magistralapi.dto.pricing.PriceCalculationInput;
import ru.rtkmagistral.magistralapi.dto.pricing.PriceCalculationResult;
import ru.rtkmagistral.magistralapi.exception.OrderException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PriceCalculationServiceTest {

    private PriceCalculationService service;

    @BeforeEach
    void setUp() {
        service = new PriceCalculationService(new PricingReferenceData(new ObjectMapper()));
    }

    private PriceCalculationInput input(
            String senderCity, String receiverCity,
            boolean senderOffice, boolean receiverOffice,
            int weightGr, int lengthCentiCm, int widthCentiCm, int heightCentiCm,
            NatureOfInvestment nature, long declaredValueKopeika
    ) {
        return new PriceCalculationInput(
                senderCity, receiverCity, senderOffice, receiverOffice,
                weightGr, lengthCentiCm, widthCentiCm, heightCentiCm,
                nature, declaredValueKopeika, 1.0, 1.0, null, null
        );
    }

    @Test
    @DisplayName("Дверь-дверь, зона 0, 1 кг: 560 × 1,22 × 0,85 = 580,72 ₽")
    void doorDoor_zone0_oneKg() {
        PriceCalculationResult result = service.calculate(
                input("Москва", "Москва", false, false,
                        1000, 10, 10, 10,
                        NatureOfInvestment.HOUSEHOLD_CHEMICALS, 0));

        assertThat(result.zone()).isZero();
        assertThat(result.deliveryType()).isEqualTo(DeliveryType.DOOR_DOOR);
        assertThat(result.chargeableWeightKg()).isEqualTo(1.0);
        assertThat(result.priceInKopeika()).isEqualTo(58_072L);
    }

    @Test
    @DisplayName("Объёмный вес больше фактического: считаем по объёму")
    void volumetricWeightWins() {
        PriceCalculationResult result = service.calculate(
                input("Москва", "Москва", false, false,
                        1000, 20000, 20000, 20000,
                        NatureOfInvestment.HOUSEHOLD_CHEMICALS, 0));

        assertThat(result.chargeableWeightKg()).isEqualTo(1600.0);
        assertThat(result.priceInKopeika()).isPositive();
    }

    @Test
    @DisplayName("Сбор за категорию: ювелирные изделия добавляют сбор к тарифу")
    void jewelryAddsCategorySurcharge() {
        PriceCalculationResult result = service.calculate(
                input("Москва", "Москва", false, false,
                        1000, 10, 10, 10,
                        NatureOfInvestment.JEWELRY, 100_000));

        assertThat(result.priceInKopeika()).isEqualTo(83_582L);
    }

    @Test
    @DisplayName("Неизвестный город отправления — PRICING_ZONE_NOT_FOUND")
    void unknownCity_throws() {
        assertThatThrownBy(() -> service.calculate(
                input("Неизвестноград", "Москва", false, false,
                        1000, 10, 10, 10,
                        NatureOfInvestment.HOUSEHOLD_CHEMICALS, 0)))
                .isInstanceOf(OrderException.class)
                .hasMessage("PRICING_ZONE_NOT_FOUND");
    }

    @Test
    @DisplayName("Окно-окно для зоны 0 недоступно — PRICING_TARIFF_NOT_AVAILABLE")
    void windowWindowZone0_throws() {
        assertThatThrownBy(() -> service.calculate(
                input("Москва", "Москва", true, true,
                        1000, 10, 10, 10,
                        NatureOfInvestment.HOUSEHOLD_CHEMICALS, 0)))
                .isInstanceOf(OrderException.class)
                .hasMessage("PRICING_TARIFF_NOT_AVAILABLE");
    }
}
