package ru.rtkmagistral.magistralapi.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.rtkmagistral.magistralapi.client.dadata.spec.IDadataAddressClient;
import ru.rtkmagistral.magistralapi.domain.jpa.NatureOfInvestment;
import ru.rtkmagistral.magistralapi.dto.pricing.DadataAddress;
import ru.rtkmagistral.magistralapi.dto.pricing.PriceCalculationResult;
import ru.rtkmagistral.magistralapi.dto.pricing.PriceQuoteRequest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PriceQuoteIntegrationTest {

    @Test
    @DisplayName("Полный расчёт Нижний Новгород → Гатчина использует Санкт-Петербург как тарифный центр")
    void nizhnyNovgorodToGatchina_calculatesPrice() {
        IDadataAddressClient dadata = mock(IDadataAddressClient.class);
        when(dadata.resolveAddress(contains("Нижний Новгород")))
                .thenReturn(Optional.of(address("Нижегородская", "RU-NIZ", "Нижний Новгород", 2)));
        when(dadata.resolveAddress(contains("Гатчина")))
                .thenReturn(Optional.of(address("Ленинградская", "RU-LEN", "Гатчина", 0)));

        PricingReferenceData referenceData = new PricingReferenceData(new ObjectMapper());
        CityResolver resolver = new CityResolver(referenceData, dadata);
        PriceCalculationService calculationService = new PriceCalculationService(referenceData);
        PriceQuoteService quoteService = new PriceQuoteService(resolver, calculationService);

        PriceQuoteRequest request = new PriceQuoteRequest(
                "г Нижний Новгород, ул Абрикосовая, д 1",
                false,
                "Ленинградская обл, г Гатчина, ул Дальняя, д 9",
                false,
                1000,
                1000,
                1000,
                100_000,
                12_000L,
                NatureOfInvestment.OTHER
        );

        PriceCalculationResult result = quoteService.quote(request);

        assertThat(result.zone()).isEqualTo(3);
        assertThat(result.chargeableWeightKg()).isEqualTo(100.0);
        assertThat(result.priceInKopeika()).isEqualTo(1_602_668L);
    }

    private DadataAddress address(String region, String regionIsoCode, String city, int capitalMarker) {
        return new DadataAddress(
                "RU", region, "region-fias", regionIsoCode,
                null, null,
                city, "city-fias",
                null, null,
                capitalMarker, 8,
                city
        );
    }
}
