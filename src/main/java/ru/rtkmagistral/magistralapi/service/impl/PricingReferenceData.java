package ru.rtkmagistral.magistralapi.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import ru.rtkmagistral.magistralapi.dto.pricing.CategorySurchargeBracket;
import ru.rtkmagistral.magistralapi.dto.pricing.DeliveryType;
import ru.rtkmagistral.magistralapi.dto.pricing.RemoteSurchargeEntry;
import ru.rtkmagistral.magistralapi.dto.pricing.TariffRow;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Загружает и хранит в памяти справочные данные для расчёта стоимости отправлений,
 * выгруженные из файла «Стоимость» в JSON-ресурсы каталога {@code classpath:pricing}.
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

    public PricingReferenceData(ObjectMapper objectMapper) {
        try {
            this.cities = new LinkedHashSet<>(read(objectMapper, "cities.json", new TypeReference<List<String>>() {}));
            this.zones = read(objectMapper, "zones.json", new TypeReference<Map<String, Map<String, Integer>>>() {});
            this.underHalfKg = read(objectMapper, "under_half_kg.json", new TypeReference<Map<String, Integer>>() {});
            this.categorySurcharge = read(objectMapper, "category_surcharge.json", new TypeReference<List<CategorySurchargeBracket>>() {});
            this.remoteSurcharge = read(objectMapper, "remote_surcharge.json", new TypeReference<List<RemoteSurchargeEntry>>() {});

            this.tariffs = new HashMap<>();
            this.tariffs.put(DeliveryType.DOOR_DOOR, readTariff(objectMapper, "tariffs/door_door.json"));
            this.tariffs.put(DeliveryType.WINDOW_DOOR, readTariff(objectMapper, "tariffs/window_door.json"));
            this.tariffs.put(DeliveryType.WINDOW_WINDOW, readTariff(objectMapper, "tariffs/window_window.json"));
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
        return byZone;
    }

    /**
     * @return неизменяемый набор городов, присутствующих в таблице зон.
     */
    public Set<String> cities() {
        return cities;
    }

    /**
     * Возвращает зону доставки между двумя городами таблицы зон.
     *
     * @return номер зоны или {@code null}, если пара городов в таблице отсутствует.
     */
    public Integer zone(String senderCity, String receiverCity) {
        Map<String, Integer> row = zones.get(senderCity);
        return row == null ? null : row.get(receiverCity);
    }

    /**
     * Возвращает тарифную строку для типа доставки и зоны.
     */
    public TariffRow tariff(DeliveryType deliveryType, int zone) {
        Map<Integer, TariffRow> byZone = tariffs.get(deliveryType);
        return byZone == null ? null : byZone.get(zone);
    }

    /**
     * Возвращает тариф для веса до 0,5 кг по административному центру или {@code null}, если он не задан.
     */
    public Integer underHalfKgTariff(String city) {
        return underHalfKg.get(city);
    }

    public List<CategorySurchargeBracket> categorySurcharge() {
        return categorySurcharge;
    }

    public List<RemoteSurchargeEntry> remoteSurcharge() {
        return remoteSurcharge;
    }
}
