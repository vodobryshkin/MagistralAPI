package ru.rtkmagistral.magistralapi.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.rtkmagistral.magistralapi.client.dadata.spec.IDadataAddressClient;
import ru.rtkmagistral.magistralapi.dto.pricing.ResolvedLocation;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CityResolverTest {

    private CityResolver resolver;

    @BeforeEach
    void setUp() {
        IDadataAddressClient dadata = mock(IDadataAddressClient.class);
        when(dadata.resolveAddress(anyString())).thenReturn(Optional.empty());
        resolver = new CityResolver(new PricingReferenceData(new ObjectMapper()), dadata);
    }

    @Test
    @DisplayName("Адрес отделения спецсвязи распознаётся как «окно»")
    void officeAddress_detectedAsOffice() {
        ResolvedLocation location = resolver.resolve("129090, Россия, г Москва, пр-кт Мира, д 11");

        assertThat(location.office()).isTrue();
        assertThat(location.city()).isEqualTo("Москва");
    }

    @Test
    @DisplayName("Обычный адрес в том же городе не считается отделением")
    void ordinaryAddress_notOffice() {
        ResolvedLocation location = resolver.resolve("г Москва, ул Тверская, д 1");

        assertThat(location.office()).isFalse();
        assertThat(location.city()).isEqualTo("Москва");
    }
}
