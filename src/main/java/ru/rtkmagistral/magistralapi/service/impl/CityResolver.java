package ru.rtkmagistral.magistralapi.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.rtkmagistral.magistralapi.client.dadata.spec.IDadataAddressClient;
import ru.rtkmagistral.magistralapi.dto.pricing.DadataAddress;
import ru.rtkmagistral.magistralapi.dto.pricing.RemoteSurchargeEntry;
import ru.rtkmagistral.magistralapi.dto.pricing.ResolvedLocation;
import ru.rtkmagistral.magistralapi.exception.DadataClientException;
import ru.rtkmagistral.magistralapi.exception.OrderException;
import ru.rtkmagistral.magistralapi.service.spec.ICityResolver;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Резолвер города по адресу. Приоритетно обращается к Dadata, при недоступности сервиса
 * ищет город из таблицы зон непосредственно в строке адреса.
 * Региональный коэффициент: 1,0 для административного центра, 1,15 для Ленинградской области,
 * 1,25 для иного субъекта РФ. Доплата за отдалённость определяется по справочнику отдалённых пунктов.
 */
@Service
@RequiredArgsConstructor
public class CityResolver implements ICityResolver {
    private static final double COEFFICIENT_ADMIN_CENTER = 1.0;
    private static final double COEFFICIENT_LENINGRAD_REGION = 1.15;
    private static final double COEFFICIENT_OTHER_REGION = 1.25;

    private final PricingReferenceData referenceData;
    private final IDadataAddressClient dadataAddressClient;

    private Map<String, String> normalizedCities;

    @Override
    public ResolvedLocation resolve(String address) {
        DadataAddress dadata = callDadata(address);

        String matrixCity = null;
        boolean adminCenter = false;

        if (dadata != null && dadata.city() != null) {
            matrixCity = matchCity(dadata.city());
            adminCenter = matrixCity != null;
        }
        if (matrixCity == null) {
            matrixCity = scanAddressForCity(address);
        }
        if (matrixCity == null) {
            throw new OrderException("PRICING_CITY_NOT_RESOLVED");
        }

        double coefficient = coefficient(adminCenter, dadata);
        Integer remotePerKg = remotePerKg(dadata, address);

        return new ResolvedLocation(matrixCity, coefficient, remotePerKg);
    }

    private DadataAddress callDadata(String address) {
        try {
            return dadataAddressClient.resolveAddress(address).orElse(null);
        } catch (DadataClientException e) {
            return null;
        }
    }

    private double coefficient(boolean adminCenter, DadataAddress dadata) {
        if (adminCenter) {
            return COEFFICIENT_ADMIN_CENTER;
        }
        if (dadata != null && dadata.region() != null
                && normalize(dadata.region()).contains("ленинград")) {
            return COEFFICIENT_LENINGRAD_REGION;
        }
        return COEFFICIENT_OTHER_REGION;
    }

    private Integer remotePerKg(DadataAddress dadata, String address) {
        String city = dadata != null && dadata.city() != null ? normalize(dadata.city()) : null;
        if (city == null || city.length() < 3) {
            return null;
        }
        for (RemoteSurchargeEntry entry : referenceData.remoteSurcharge()) {
            if (normalize(entry.name()).contains(city)) {
                return entry.perKg();
            }
        }
        return null;
    }

    private String matchCity(String rawCity) {
        return normalizedCities().get(normalize(rawCity));
    }

    private String scanAddressForCity(String address) {
        String normalized = normalize(address);
        for (Map.Entry<String, String> entry : normalizedCities().entrySet()) {
            if (normalized.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    private Map<String, String> normalizedCities() {
        if (normalizedCities == null) {
            Map<String, String> map = new HashMap<>();
            for (String city : referenceData.cities()) {
                map.put(normalize(city), city);
            }
            normalizedCities = map;
        }
        return normalizedCities;
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase().replace('ё', 'е').trim();
    }
}
