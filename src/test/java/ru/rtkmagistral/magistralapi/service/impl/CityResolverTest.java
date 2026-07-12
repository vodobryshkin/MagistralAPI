package ru.rtkmagistral.magistralapi.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.rtkmagistral.magistralapi.client.dadata.spec.IDadataAddressClient;
import ru.rtkmagistral.magistralapi.dto.pricing.CoefficientPolicy;
import ru.rtkmagistral.magistralapi.dto.pricing.DadataAddress;
import ru.rtkmagistral.magistralapi.dto.pricing.ResolvedLocation;
import ru.rtkmagistral.magistralapi.dto.pricing.TariffRegionMapping;
import ru.rtkmagistral.magistralapi.exception.DadataClientException;
import ru.rtkmagistral.magistralapi.exception.PricingErrorCode;
import ru.rtkmagistral.magistralapi.exception.PricingException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CityResolverTest {

    private IDadataAddressClient dadata;
    private CityResolver resolver;

    @BeforeEach
    void setUp() {
        dadata = mock(IDadataAddressClient.class);
        when(dadata.resolveAddress(anyString())).thenReturn(Optional.empty());
        resolver = new CityResolver(new PricingReferenceData(new ObjectMapper()), dadata);
    }

    @Test
    @DisplayName("Адрес отделения спецсвязи распознаётся как «окно»")
    void officeAddress_detectedAsOffice() {
        ResolvedLocation location = resolver.resolve("129090, Россия, г Москва, пр-кт Мира, д 11");

        assertThat(location.office()).isTrue();
        assertThat(location.city()).isEqualTo("Москва");
        assertThat(location.coefficient()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("Без Dadata город тарифной матрицы распознаётся непосредственно из адреса")
    void directMatrixCity_usesSafeFallback() {
        ResolvedLocation location = resolver.resolve("г Москва, ул Тверская, д 1");

        assertThat(location.office()).isFalse();
        assertThat(location.city()).isEqualTo("Москва");
        assertThat(location.coefficient()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("Любой город Ленинградской области сопоставляется с Санкт-Петербургом и коэффициентом 1.15")
    void gatchina_resolvesThroughSaintPetersburg() {
        when(dadata.resolveAddress(anyString())).thenReturn(Optional.of(address(
                "Ленинградская", "RU-LEN", null,
                "Гатчина", null, 0
        )));

        ResolvedLocation location = resolver.resolve(
                "Ленинградская обл, г Гатчина, ул Дальняя, д 9");

        assertThat(location.city()).isEqualTo("Санкт-Петербург");
        assertThat(location.coefficient()).isEqualTo(1.15);
    }

    @Test
    @DisplayName("Необластной центр сопоставляется с тарифным центром своего субъекта")
    void tolyatti_resolvesThroughSamara() {
        when(dadata.resolveAddress(anyString())).thenReturn(Optional.of(address(
                "Самарская", "RU-SAM", null,
                "Тольятти", null, 0
        )));

        ResolvedLocation location = resolver.resolve("Самарская обл, г Тольятти");

        assertThat(location.city()).isEqualTo("Самара");
        assertThat(location.coefficient()).isEqualTo(1.25);
    }

    @Test
    @DisplayName("Адрес в самом тарифном центре получает коэффициент 1.0")
    void regionalCapital_getsCenterCoefficient() {
        when(dadata.resolveAddress(anyString())).thenReturn(Optional.of(address(
                "Самарская", "RU-SAM", null,
                "Самара", null, 2
        )));

        ResolvedLocation location = resolver.resolve("г Самара, ул Ленинградская, д 1");

        assertThat(location.city()).isEqualTo("Самара");
        assertThat(location.coefficient()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("Посёлок или село также сопоставляется по субъекту РФ")
    void settlement_resolvesThroughRegion() {
        when(dadata.resolveAddress(anyString())).thenReturn(Optional.of(address(
                "Иркутская", "RU-IRK", "Иркутский",
                null, "Хомутово", 0
        )));

        ResolvedLocation location = resolver.resolve("Иркутская обл, с Хомутово");

        assertThat(location.city()).isEqualTo("Иркутск");
        assertThat(location.coefficient()).isEqualTo(1.25);
    }

    @Test
    @DisplayName("Московская область сопоставляется с Москвой, но не считается тарифным центром")
    void moscowRegion_usesRegionalCoefficient() {
        when(dadata.resolveAddress(anyString())).thenReturn(Optional.of(address(
                "Московская", "RU-MOS", null,
                "Красногорск", null, 2
        )));

        ResolvedLocation location = resolver.resolve("Московская обл, г Красногорск");

        assertThat(location.city()).isEqualTo("Москва");
        assertThat(location.coefficient()).isEqualTo(1.25);
    }

    @Test
    @DisplayName("Федеральный город корректно работает даже когда Dadata не заполняет city")
    void federalCity_doesNotRequireCityField() {
        when(dadata.resolveAddress(anyString())).thenReturn(Optional.of(address(
                "Санкт-Петербург", "RU-SPE", null,
                null, null, 0
        )));

        ResolvedLocation location = resolver.resolve("г Санкт-Петербург, Невский проспект, д 1");

        assertThat(location.city()).isEqualTo("Санкт-Петербург");
        assertThat(location.coefficient()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("Доплата за отдалённость учитывает одновременно регион и населённый пункт")
    void remoteSurcharge_matchesRegionAndLocality() {
        when(dadata.resolveAddress(anyString())).thenReturn(Optional.of(address(
                "Амурская", "RU-AMU", null,
                "Зея", null, 0
        )));

        ResolvedLocation location = resolver.resolve("Амурская обл, г Зея");

        assertThat(location.city()).isEqualTo("Благовещенск");
        assertThat(location.remotePerKg()).isEqualTo(97);
    }

    @Test
    @DisplayName("Неизвестный субъект возвращает отдельную ошибку тарификации")
    void unknownRegion_throwsPricingRegionNotSupported() {
        when(dadata.resolveAddress(anyString())).thenReturn(Optional.of(address(
                "Неизвестная", "RU-XXX", null,
                "Неизвестноград", null, 0
        )));

        assertThatThrownBy(() -> resolver.resolve("неизвестный адрес"))
                .isInstanceOfSatisfying(PricingException.class,
                        ex -> assertThat(ex.getCode())
                                .isEqualTo(PricingErrorCode.PRICING_REGION_NOT_SUPPORTED));
    }

    @Test
    @DisplayName("При недоступной Dadata прямой город матрицы продолжает работать")
    void dadataUnavailable_directCityStillWorks() {
        when(dadata.resolveAddress(anyString()))
                .thenThrow(new DadataClientException("DADATA_ADDRESS_REQUEST_FAILED"));

        ResolvedLocation location = resolver.resolve("г Нижний Новгород, ул Абрикосовая, д 1");

        assertThat(location.city()).isEqualTo("Нижний Новгород");
        assertThat(location.coefficient()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("При недоступной Dadata произвольный населённый пункт не угадывается по строке")
    void dadataUnavailable_arbitraryLocalityReturns503Cause() {
        when(dadata.resolveAddress(anyString()))
                .thenThrow(new DadataClientException("DADATA_ADDRESS_REQUEST_FAILED"));

        assertThatThrownBy(() -> resolver.resolve("Ленинградская обл, г Гатчина"))
                .isInstanceOfSatisfying(PricingException.class,
                        ex -> assertThat(ex.getCode())
                                .isEqualTo(PricingErrorCode.DADATA_UNAVAILABLE));
    }

    @Test
    @DisplayName("Каждый настроенный субъект сопоставляется с существующим городом тарифной матрицы")
    void everyConfiguredRegion_resolvesToItsTariffCity() {
        PricingReferenceData referenceData = new PricingReferenceData(new ObjectMapper());

        for (TariffRegionMapping mapping : referenceData.tariffRegions()) {
            String locality = mapping.coefficientPolicy() == CoefficientPolicy.ALWAYS_CENTER
                    ? null
                    : "Тестовый населённый пункт";
            DadataAddress response = address(
                    mapping.regionNames().getFirst(),
                    mapping.regionIsoCode(),
                    null,
                    locality,
                    null,
                    0
            );
            CityResolver regionResolver = new CityResolver(
                    referenceData, ignored -> Optional.of(response));

            ResolvedLocation result = regionResolver.resolve("тестовый адрес");

            double expectedCoefficient = switch (mapping.coefficientPolicy()) {
                case ALWAYS_CENTER -> 1.0;
                case ALWAYS_LENINGRAD_REGION -> 1.15;
                case ALWAYS_REGIONAL, CENTER_OR_REGIONAL -> 1.25;
            };
            assertThat(result.city())
                    .as("тарифный город для %s", mapping.regionIsoCode())
                    .isEqualTo(mapping.tariffCity());
            assertThat(result.coefficient())
                    .as("коэффициент для %s", mapping.regionIsoCode())
                    .isEqualTo(expectedCoefficient);
        }
    }

    @Test
    @DisplayName("Все обычные административные центры получают коэффициент 1.0")
    void everyCenterOrRegionalTariffCity_getsCenterCoefficient() {
        PricingReferenceData referenceData = new PricingReferenceData(new ObjectMapper());

        referenceData.tariffRegions().stream()
                .filter(mapping -> mapping.coefficientPolicy() == CoefficientPolicy.CENTER_OR_REGIONAL)
                .forEach(mapping -> {
                    DadataAddress response = address(
                            mapping.regionNames().getFirst(),
                            mapping.regionIsoCode(),
                            null,
                            mapping.tariffCity(),
                            null,
                            2
                    );
                    CityResolver regionResolver = new CityResolver(
                            referenceData, ignored -> Optional.of(response));

                    ResolvedLocation result = regionResolver.resolve("тестовый адрес");

                    assertThat(result.city())
                            .as("тарифный центр для %s", mapping.regionIsoCode())
                            .isEqualTo(mapping.tariffCity());
                    assertThat(result.coefficient())
                            .as("коэффициент центра для %s", mapping.regionIsoCode())
                            .isEqualTo(1.0);
                });
    }

    @Test
    @DisplayName("При неизвестном ISO-коде используется точное название субъекта")
    void unknownIsoCode_fallsBackToRegionName() {
        when(dadata.resolveAddress(anyString())).thenReturn(Optional.of(address(
                "Нижегородская", "RU-UNKNOWN", null,
                "Арзамас", null, 0
        )));

        ResolvedLocation result = resolver.resolve("Нижегородская обл, г Арзамас");

        assertThat(result.city()).isEqualTo("Нижний Новгород");
        assertThat(result.coefficient()).isEqualTo(1.25);
    }

    @Test
    @DisplayName("Иностранный адрес не проходит как российский тариф")
    void foreignAddress_isRejected() {
        DadataAddress foreign = new DadataAddress(
                "KZ", "Алматы", null, "KZ-ALA",
                null, null, "Алматы", "city-fias",
                null, null, 2, 4, "Казахстан, Алматы"
        );
        when(dadata.resolveAddress(anyString())).thenReturn(Optional.of(foreign));

        assertThatThrownBy(() -> resolver.resolve("Казахстан, Алматы"))
                .isInstanceOfSatisfying(PricingException.class,
                        ex -> assertThat(ex.getCode())
                                .isEqualTo(PricingErrorCode.PRICING_COUNTRY_NOT_SUPPORTED));
    }

    private DadataAddress address(String region,
                                  String regionIsoCode,
                                  String area,
                                  String city,
                                  String settlement,
                                  int capitalMarker) {
        return new DadataAddress(
                "RU",
                region,
                "region-fias-id",
                regionIsoCode,
                area,
                area == null ? null : "area-fias-id",
                city,
                city == null ? null : "city-fias-id",
                settlement,
                settlement == null ? null : "settlement-fias-id",
                capitalMarker,
                city != null ? 4 : 6,
                "Полный адрес"
        );
    }
}
