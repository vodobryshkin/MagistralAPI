package ru.rtkmagistral.magistralapi.client.dadata.impl;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import ru.rtkmagistral.magistralapi.client.dadata.spec.IDadataAddressClient;
import ru.rtkmagistral.magistralapi.dto.pricing.DadataAddress;
import ru.rtkmagistral.magistralapi.exception.DadataClientException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Реализация клиента Dadata для разбора адреса через эндпойнт «suggest/address».
 */
@Component
public class DadataAddressClient implements IDadataAddressClient {
    private final RestClient rest;

    public DadataAddressClient(@Qualifier("dadataAddressRestClient") RestClient rest) {
        this.rest = rest;
    }

    @Override
    public Optional<DadataAddress> resolveAddress(String address) {
        var root = rest.post()
                .uri("")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("query", address, "count", 1))
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, resp) -> {
                    throw new DadataClientException("DADATA_ERROR_WITH_CODE: " + resp.getStatusCode());
                })
                .body(new ParameterizedTypeReference<Map<String, Object>>() {});

        if (root == null) {
            return Optional.empty();
        }

        List<?> suggestions = (List<?>) root.get("suggestions");
        if (suggestions == null || suggestions.isEmpty()) {
            return Optional.empty();
        }

        var data = (Map<?, ?>) ((Map<?, ?>) suggestions.getFirst()).get("data");
        if (data == null) {
            return Optional.empty();
        }

        String city = firstNonBlank(
                (String) data.get("city"),
                (String) data.get("settlement"),
                (String) data.get("area")
        );
        String region = (String) data.get("region");

        return Optional.of(new DadataAddress(city, region));
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}
