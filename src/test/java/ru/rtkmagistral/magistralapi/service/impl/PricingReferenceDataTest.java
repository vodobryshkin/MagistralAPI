package ru.rtkmagistral.magistralapi.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class PricingReferenceDataTest {

    private PricingReferenceData referenceData;

    @BeforeEach
    void setUp() {
        referenceData = new PricingReferenceData(new ObjectMapper());
    }

    @Test
    @DisplayName("Справочник покрывает все 89 субъектов и все 87 городов тарифной матрицы")
    void tariffRegions_coverAllSubjectsAndMatrixCities() {
        assertThat(referenceData.tariffRegions()).hasSize(89);

        Set<String> mappedCities = referenceData.tariffRegions().stream()
                .map(mapping -> mapping.tariffCity())
                .collect(Collectors.toSet());

        assertThat(mappedCities).containsExactlyInAnyOrderElementsOf(referenceData.cities());
    }

    @Test
    @DisplayName("Субъект ищется по ISO-коду Dadata")
    void tariffRegion_foundByIsoCode() {
        assertThat(referenceData.tariffRegion("ru-len", null).tariffCity())
                .isEqualTo("Санкт-Петербург");
        assertThat(referenceData.tariffRegion("RU-SAM", null).tariffCity())
                .isEqualTo("Самара");
    }

    @Test
    @DisplayName("При отсутствии ISO-кода используется нормализованное название региона")
    void tariffRegion_foundByRegionNameFallback() {
        assertThat(referenceData.tariffRegion(null, "Республика Саха (Якутия)").tariffCity())
                .isEqualTo("Якутск");
        assertThat(referenceData.tariffRegion(null, "Ханты-Мансийский Автономный округ - Югра").tariffCity())
                .isEqualTo("Ханты-Мансийск");
    }

    @Test
    @DisplayName("Москва и Санкт-Петербург покрывают также соседние области отдельными правилами")
    void federalCitiesAndNeighbouringRegions_haveSeparateMappings() {
        assertThat(referenceData.tariffRegion("RU-MOW", null).tariffCity()).isEqualTo("Москва");
        assertThat(referenceData.tariffRegion("RU-MOS", null).tariffCity()).isEqualTo("Москва");
        assertThat(referenceData.tariffRegion("RU-SPE", null).tariffCity()).isEqualTo("Санкт-Петербург");
        assertThat(referenceData.tariffRegion("RU-LEN", null).tariffCity()).isEqualTo("Санкт-Петербург");
    }
}
