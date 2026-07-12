package ru.rtkmagistral.magistralapi.service.impl;

import org.springframework.stereotype.Service;
import ru.rtkmagistral.magistralapi.client.dadata.spec.IDadataAddressClient;
import ru.rtkmagistral.magistralapi.dto.pricing.CoefficientPolicy;
import ru.rtkmagistral.magistralapi.dto.pricing.DadataAddress;
import ru.rtkmagistral.magistralapi.dto.pricing.OfficeEntry;
import ru.rtkmagistral.magistralapi.dto.pricing.RemoteSurchargeEntry;
import ru.rtkmagistral.magistralapi.dto.pricing.ResolvedLocation;
import ru.rtkmagistral.magistralapi.dto.pricing.TariffRegionMapping;
import ru.rtkmagistral.magistralapi.exception.DadataClientException;
import ru.rtkmagistral.magistralapi.exception.PricingErrorCode;
import ru.rtkmagistral.magistralapi.exception.PricingException;
import ru.rtkmagistral.magistralapi.service.spec.ICityResolver;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Универсально сопоставляет любой российский населённый пункт с городом тарифной матрицы.
 *
 * <p>Основной путь:</p>
 * <ol>
 *     <li>Dadata разбирает адрес и возвращает {@code region_iso_code} и населённый пункт.</li>
 *     <li>Субъект РФ ищется в {@code pricing/tariff_regions.json}.</li>
 *     <li>Из записи берётся город-ключ таблицы {@code zones.json}.</li>
 *     <li>Коэффициент определяется политикой субъекта и тем, совпадает ли населённый пункт
 *     с тарифным городом.</li>
 * </ol>
 *
 * <p>Локальный поиск по строке оставлен только как безопасный резервный сценарий для адресов,
 * в которых прямо указан один из 87 городов тарифной матрицы. Он не пытается угадывать регион
 * произвольного населённого пункта без Dadata.</p>
 */
@Service
public class CityResolver implements ICityResolver {
    private static final double COEFFICIENT_CENTER = 1.0;
    private static final double COEFFICIENT_LENINGRAD_REGION = 1.15;
    private static final double COEFFICIENT_REGIONAL = 1.25;

    private final PricingReferenceData referenceData;
    private final IDadataAddressClient dadataAddressClient;
    private final Map<String, String> normalizedCities;
    private final Map<String, OfficeEntry> normalizedOffices;

    public CityResolver(PricingReferenceData referenceData,
                        IDadataAddressClient dadataAddressClient) {
        this.referenceData = referenceData;
        this.dadataAddressClient = dadataAddressClient;
        this.normalizedCities = buildNormalizedCities();
        this.normalizedOffices = buildNormalizedOffices();
    }

    @Override
    public ResolvedLocation resolve(String address) {
        OfficeEntry office = matchOffice(address);

        try {
            DadataAddress dadata = dadataAddressClient.resolveAddress(address).orElse(null);
            if (dadata != null) {
                return resolveFromDadata(dadata, office);
            }
        } catch (DadataClientException e) {
            ResolvedLocation fallback = resolveDirectMatrixCity(address, office);
            if (fallback != null) {
                return fallback;
            }
            throw new PricingException(PricingErrorCode.DADATA_UNAVAILABLE, e);
        }

        ResolvedLocation fallback = resolveDirectMatrixCity(address, office);
        if (fallback != null) {
            return fallback;
        }
        throw new PricingException(PricingErrorCode.PRICING_ADDRESS_NOT_RESOLVED);
    }

    private ResolvedLocation resolveFromDadata(DadataAddress address, OfficeEntry office) {
        if (address.countryIsoCode() != null
                && !address.countryIsoCode().isBlank()
                && !"RU".equalsIgnoreCase(address.countryIsoCode())) {
            throw new PricingException(PricingErrorCode.PRICING_COUNTRY_NOT_SUPPORTED);
        }

        TariffRegionMapping mapping = referenceData.tariffRegion(
                address.regionIsoCode(), address.region());
        if (mapping == null) {
            throw new PricingException(PricingErrorCode.PRICING_REGION_NOT_SUPPORTED);
        }

        String locality = address.localityName();
        if ((locality == null || locality.isBlank())
                && mapping.coefficientPolicy() != CoefficientPolicy.ALWAYS_CENTER) {
            throw new PricingException(PricingErrorCode.PRICING_LOCALITY_NOT_RESOLVED);
        }

        double coefficient = resolveCoefficient(mapping, locality);
        Integer remotePerKg = remotePerKg(address, mapping);

        return new ResolvedLocation(
                mapping.tariffCity(),
                coefficient,
                remotePerKg,
                office != null
        );
    }

    private double resolveCoefficient(TariffRegionMapping mapping, String locality) {
        return switch (mapping.coefficientPolicy()) {
            case ALWAYS_CENTER -> COEFFICIENT_CENTER;
            case ALWAYS_LENINGRAD_REGION -> COEFFICIENT_LENINGRAD_REGION;
            case ALWAYS_REGIONAL -> COEFFICIENT_REGIONAL;
            case CENTER_OR_REGIONAL -> isSameLocality(locality, mapping.tariffCity())
                    ? COEFFICIENT_CENTER
                    : COEFFICIENT_REGIONAL;
        };
    }

    private boolean isSameLocality(String left, String right) {
        return !normalizeName(left).isBlank()
                && normalizeName(left).equals(normalizeName(right));
    }

    /**
     * Доплата за отдалённость ищется одновременно по субъекту и населённому пункту.
     * Это устраняет ложные совпадения одноимённых городов и посёлков в разных регионах.
     */
    private Integer remotePerKg(DadataAddress address, TariffRegionMapping mapping) {
        String locality = normalizeName(address.localityName());
        String area = normalizeName(address.area());
        if (locality.length() < 2) {
            return null;
        }

        RemoteSurchargeEntry localityAndRegionMatch = null;
        for (RemoteSurchargeEntry entry : referenceData.remoteSurcharge()) {
            String normalizedEntry = normalizeName(entry.name());
            boolean regionMatches = mapping.regionNames().stream()
                    .map(this::normalizeName)
                    .filter(alias -> alias.length() >= 2)
                    .anyMatch(alias -> containsPhrase(normalizedEntry, alias));
            if (!regionMatches || !containsPhrase(normalizedEntry, locality)) {
                continue;
            }

            if (!area.isBlank() && containsPhrase(normalizedEntry, area)) {
                return entry.perKg();
            }
            if (localityAndRegionMatch == null) {
                localityAndRegionMatch = entry;
            }
        }
        return localityAndRegionMatch == null ? null : localityAndRegionMatch.perKg();
    }

    private boolean containsPhrase(String text, String phrase) {
        if (text.isBlank() || phrase.isBlank()) {
            return false;
        }
        return (" " + text + " ").contains(" " + phrase + " ")
                || text.startsWith(phrase + " ")
                || text.endsWith(" " + phrase)
                || text.equals(phrase);
    }

    /**
     * Резервный путь без Dadata. Корректен только если строка содержит сам город тарифной матрицы.
     */
    private ResolvedLocation resolveDirectMatrixCity(String address, OfficeEntry office) {
        String matrixCity = null;
        if (office != null) {
            matrixCity = matchCity(office.city());
        }
        if (matrixCity == null) {
            matrixCity = scanAddressForCity(address);
        }
        if (matrixCity == null) {
            return null;
        }
        return new ResolvedLocation(matrixCity, COEFFICIENT_CENTER, null, office != null);
    }

    private String matchCity(String rawCity) {
        return normalizedCities.get(normalizeName(rawCity));
    }

    private String scanAddressForCity(String address) {
        String normalized = normalizeName(address);
        for (Map.Entry<String, String> entry : normalizedCities.entrySet()) {
            if (containsPhrase(normalized, entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    private Map<String, String> buildNormalizedCities() {
        Map<String, String> map = new LinkedHashMap<>();
        referenceData.cities().stream()
                .sorted((left, right) -> Integer.compare(right.length(), left.length()))
                .forEach(city -> map.put(normalizeName(city), city));
        return java.util.Collections.unmodifiableMap(new LinkedHashMap<>(map));
    }

    private OfficeEntry matchOffice(String address) {
        String normalized = normalizeAddress(address);
        if (normalized.isBlank()) {
            return null;
        }
        for (Map.Entry<String, OfficeEntry> entry : normalizedOffices.entrySet()) {
            if (normalized.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    private Map<String, OfficeEntry> buildNormalizedOffices() {
        Map<String, OfficeEntry> map = new LinkedHashMap<>();
        for (OfficeEntry office : referenceData.offices()) {
            String key = normalizeAddress(office.address());
            if (!key.isBlank()) {
                map.put(key, office);
            }
        }
        return java.util.Collections.unmodifiableMap(new LinkedHashMap<>(map));
    }

    private String normalizeName(String value) {
        if (value == null) {
            return "";
        }
        return value.toLowerCase(Locale.ROOT)
                .replace('ё', 'е')
                .replace('—', '-')
                .replaceAll("[^а-яa-z0-9]+", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String normalizeAddress(String value) {
        if (value == null) {
            return "";
        }
        return value.toLowerCase(Locale.ROOT)
                .replace('ё', 'е')
                .replaceAll("\\b\\d{6}\\b", " ")
                .replace("россия", " ")
                .replaceAll("[^а-я0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}
