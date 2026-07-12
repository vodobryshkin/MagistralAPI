package ru.rtkmagistral.magistralapi.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import ru.rtkmagistral.magistralapi.dto.pricing.CategorySurchargeBracket;
import ru.rtkmagistral.magistralapi.dto.pricing.DeliveryType;
import ru.rtkmagistral.magistralapi.dto.pricing.OfficeEntry;
import ru.rtkmagistral.magistralapi.dto.pricing.RemoteSurchargeEntry;
import ru.rtkmagistral.magistralapi.dto.pricing.TariffRegionMapping;
import ru.rtkmagistral.magistralapi.dto.pricing.TariffRow;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Загружает и проверяет справочные данные тарификации из {@code classpath:pricing}.
 */
@Component
public class PricingReferenceData {
    private static final String BASE = "pricing/";

    private final Set<String> cities;
    private final Map<String, Map<String, Integer>> zones;
    private final Map<DeliveryType, Map<Integer, TariffRow>> tariffs;
    private final Map<String, Integer> underHalfKg;
    private final List<CategorySurchargeBracket> categorySurcharge;
    private final List<RemoteSurchargeEntry> remoteSurcharge;
    private final List<OfficeEntry> offices;
    private final List<TariffRegionMapping> tariffRegions;
    private final Map<String, TariffRegionMapping> tariffRegionByIso;
    private final Map<String, TariffRegionMapping> tariffRegionByName;

    public PricingReferenceData(ObjectMapper objectMapper) {
        try {
            this.cities = Set.copyOf(new LinkedHashSet<>(
                    read(objectMapper, "cities.json", new TypeReference<List<String>>() {})));
            this.zones = read(objectMapper, "zones.json",
                    new TypeReference<Map<String, Map<String, Integer>>>() {});
            this.underHalfKg = read(objectMapper, "under_half_kg.json",
                    new TypeReference<Map<String, Integer>>() {});
            this.categorySurcharge = List.copyOf(read(objectMapper, "category_surcharge.json",
                    new TypeReference<List<CategorySurchargeBracket>>() {}));
            this.remoteSurcharge = List.copyOf(read(objectMapper, "remote_surcharge.json",
                    new TypeReference<List<RemoteSurchargeEntry>>() {}));
            this.offices = List.copyOf(read(objectMapper, "offices.json",
                    new TypeReference<List<OfficeEntry>>() {}));
            this.tariffRegions = List.copyOf(read(objectMapper, "tariff_regions.json",
                    new TypeReference<List<TariffRegionMapping>>() {}));

            this.tariffs = new HashMap<>();
            this.tariffs.put(DeliveryType.DOOR_DOOR, readTariff(objectMapper, "tariffs/door_door.json"));
            this.tariffs.put(DeliveryType.WINDOW_DOOR, readTariff(objectMapper, "tariffs/window_door.json"));
            this.tariffs.put(DeliveryType.WINDOW_WINDOW, readTariff(objectMapper, "tariffs/window_window.json"));

            this.tariffRegionByIso = new LinkedHashMap<>();
            this.tariffRegionByName = new LinkedHashMap<>();
            indexAndValidateTariffRegions();
            validateZoneMatrix();
        } catch (IOException e) {
            throw new UncheckedIOException("Не удалось загрузить справочники расчёта стоимости", e);
        }
    }

    private <T> T read(ObjectMapper objectMapper, String name, TypeReference<T> type) throws IOException {
        try (InputStream in = new ClassPathResource(BASE + name).getInputStream()) {
            return objectMapper.readValue(in, type);
        }
    }

    private Map<Integer, TariffRow> readTariff(ObjectMapper objectMapper, String name) throws IOException {
        Map<String, TariffRow> raw = read(objectMapper, name, new TypeReference<Map<String, TariffRow>>() {});
        Map<Integer, TariffRow> byZone = new HashMap<>();
        raw.forEach((zone, row) -> byZone.put(Integer.parseInt(zone), row));
        return Map.copyOf(byZone);
    }

    private void indexAndValidateTariffRegions() {
        if (tariffRegions.isEmpty()) {
            throw new IllegalStateException("Справочник tariff_regions.json пуст");
        }

        Set<String> mappedCities = new LinkedHashSet<>();
        for (TariffRegionMapping mapping : tariffRegions) {
            if (mapping == null
                    || isBlank(mapping.regionIsoCode())
                    || isBlank(mapping.tariffCity())
                    || mapping.coefficientPolicy() == null) {
                throw new IllegalStateException("Некорректная запись в tariff_regions.json: " + mapping);
            }
            if (!cities.contains(mapping.tariffCity())) {
                throw new IllegalStateException(
                        "Тарифный город отсутствует в cities.json: " + mapping.tariffCity());
            }

            registerUnique(tariffRegionByIso, normalizeIso(mapping.regionIsoCode()), mapping,
                    "ISO-код субъекта");

            if (mapping.regionNames() == null || mapping.regionNames().isEmpty()) {
                throw new IllegalStateException(
                        "Для субъекта не заданы варианты названия: " + mapping.regionIsoCode());
            }
            for (String regionName : mapping.regionNames()) {
                if (!isBlank(regionName)) {
                    registerUnique(tariffRegionByName, normalizeRegionName(regionName), mapping,
                            "название субъекта");
                }
            }
            mappedCities.add(mapping.tariffCity());
        }

        Set<String> missingCities = new LinkedHashSet<>(cities);
        missingCities.removeAll(mappedCities);
        if (!missingCities.isEmpty()) {
            throw new IllegalStateException(
                    "Для городов тарифной матрицы не настроены субъекты РФ: " + missingCities);
        }
    }

    private void validateZoneMatrix() {
        for (String city : cities) {
            Map<String, Integer> row = zones.get(city);
            if (row == null) {
                throw new IllegalStateException("В zones.json отсутствует строка города: " + city);
            }
            Set<String> missingColumns = new LinkedHashSet<>(cities);
            missingColumns.removeAll(row.keySet());
            if (!missingColumns.isEmpty()) {
                throw new IllegalStateException(
                        "В строке zones.json для " + city + " отсутствуют города: " + missingColumns);
            }
        }
    }

    private void registerUnique(Map<String, TariffRegionMapping> index,
                                String key,
                                TariffRegionMapping mapping,
                                String fieldName) {
        TariffRegionMapping previous = index.putIfAbsent(key, mapping);
        if (previous != null && previous != mapping) {
            throw new IllegalStateException(
                    "Дублируется " + fieldName + " '" + key + "' для "
                            + previous.tariffCity() + " и " + mapping.tariffCity());
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String normalizeIso(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeRegionName(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT)
                .replace('ё', 'е')
                .replace('—', '-')
                .replaceAll("[^а-яa-z0-9]+", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    public Set<String> cities() {
        return cities;
    }

    public Integer zone(String senderCity, String receiverCity) {
        Map<String, Integer> row = zones.get(senderCity);
        return row == null ? null : row.get(receiverCity);
    }

    public TariffRow tariff(DeliveryType deliveryType, int zone) {
        Map<Integer, TariffRow> byZone = tariffs.get(deliveryType);
        return byZone == null ? null : byZone.get(zone);
    }

    public Integer underHalfKgTariff(String city) {
        return underHalfKg.get(city);
    }

    public List<CategorySurchargeBracket> categorySurcharge() {
        return categorySurcharge;
    }

    public List<RemoteSurchargeEntry> remoteSurcharge() {
        return remoteSurcharge;
    }

    public List<OfficeEntry> offices() {
        return offices;
    }

    /**
     * Ищет тарифную настройку субъекта. Сначала используется ISO-код Dadata,
     * затем — название региона как резервный вариант.
     */
    public TariffRegionMapping tariffRegion(String regionIsoCode, String regionName) {
        if (!isBlank(regionIsoCode)) {
            TariffRegionMapping byIso = tariffRegionByIso.get(normalizeIso(regionIsoCode));
            if (byIso != null) {
                return byIso;
            }
        }
        if (!isBlank(regionName)) {
            return tariffRegionByName.get(normalizeRegionName(regionName));
        }
        return null;
    }

    public List<TariffRegionMapping> tariffRegions() {
        return tariffRegions;
    }
}
