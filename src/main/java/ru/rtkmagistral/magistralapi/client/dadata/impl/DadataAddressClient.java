package ru.rtkmagistral.magistralapi.client.dadata.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import ru.rtkmagistral.magistralapi.client.dadata.spec.IDadataAddressClient;
import ru.rtkmagistral.magistralapi.dto.pricing.DadataAddress;
import ru.rtkmagistral.magistralapi.exception.DadataClientException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Клиент Dadata для структурирования адреса через {@code suggest/address}.
 * Использует не только название города, но и стабильные поля субъекта РФ:
 * {@code region_iso_code}, {@code region_fias_id}, а также признак административного центра.
 */
@Component
public class DadataAddressClient implements IDadataAddressClient {
    private final RestClient rest;

    public DadataAddressClient(@Qualifier("dadataAddressRestClient") RestClient rest) {
        this.rest = rest;
    }

    @Override
    public Optional<DadataAddress> resolveAddress(String address) {
        if (address == null || address.isBlank()) {
            return Optional.empty();
        }

        try {
            DadataResponse root = rest.post()
                    .uri("")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("query", address, "count", 1))
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, response) -> {
                        throw new DadataClientException(
                                "DADATA_ERROR_WITH_CODE: " + response.getStatusCode());
                    })
                    .body(DadataResponse.class);

            if (root == null || root.suggestions() == null || root.suggestions().isEmpty()) {
                return Optional.empty();
            }

            Suggestion suggestion = root.suggestions().getFirst();
            if (suggestion == null || suggestion.data() == null) {
                return Optional.empty();
            }

            AddressData data = suggestion.data();
            return Optional.of(new DadataAddress(
                    data.countryIsoCode(),
                    data.region(),
                    data.regionFiasId(),
                    data.regionIsoCode(),
                    data.area(),
                    data.areaFiasId(),
                    data.city(),
                    data.cityFiasId(),
                    data.settlement(),
                    data.settlementFiasId(),
                    parseInteger(data.capitalMarker()),
                    parseInteger(data.fiasLevel()),
                    suggestion.unrestrictedValue()
            ));
        } catch (DadataClientException e) {
            throw e;
        } catch (RestClientException | IllegalArgumentException e) {
            throw new DadataClientException("DADATA_ADDRESS_REQUEST_FAILED", e);
        }
    }

    private Integer parseInteger(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record DadataResponse(List<Suggestion> suggestions) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Suggestion(
            String value,
            @JsonProperty("unrestricted_value") String unrestrictedValue,
            AddressData data
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record AddressData(
            @JsonProperty("country_iso_code") String countryIsoCode,
            String region,
            @JsonProperty("region_fias_id") String regionFiasId,
            @JsonProperty("region_iso_code") String regionIsoCode,
            String area,
            @JsonProperty("area_fias_id") String areaFiasId,
            String city,
            @JsonProperty("city_fias_id") String cityFiasId,
            String settlement,
            @JsonProperty("settlement_fias_id") String settlementFiasId,
            @JsonProperty("capital_marker") String capitalMarker,
            @JsonProperty("fias_level") String fiasLevel
    ) {
    }
}
