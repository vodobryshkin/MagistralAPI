package ru.rtkmagistral.magistralapi.client.dadata.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import ru.rtkmagistral.magistralapi.client.dadata.spec.IDadataClient;
import ru.rtkmagistral.magistralapi.dto.company.CreateCompanyRequest;
import ru.rtkmagistral.magistralapi.exception.DadataClientException;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class DadataClient implements IDadataClient {
    private final RestClient rest;

    @Override
    public CreateCompanyRequest findPartyByInn(String inn) {
        var root = rest.post()
                .uri("")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("query", inn))
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, resp) -> {
                    throw new DadataClientException("DADATA_ERROR_WITH_CODE: " + resp.getStatusCode());
                })
                .body(new ParameterizedTypeReference<Map<String, Object>>() {});

        if (root == null) {
            throw new DadataClientException("EMPTY_REQUEST_FOR_DADATA");
        }

        List<?> suggestions = (List<?>) root.get("suggestions");
        if (suggestions == null || suggestions.isEmpty()) {
            throw new DadataClientException("CANT_FIND_DATA_IN_DADATA_FOR_CURRENT_INN");
        }

        var s0 = (Map<?, ?>) suggestions.getFirst();
        var data = (Map<?, ?>) s0.get("data");

        if (data == null) {
            throw new DadataClientException("EMPTY_REQUEST_FOR_DADATA");
        }

        String title = (String) s0.get("value");
        if (title == null || title.isBlank()) {
            throw new DadataClientException("DADATA_ERROR_WHILE_PARSING_TITLE");
        }

        String mappedInn = (String) data.get("inn");
        String mappedKpp = (String) data.get("kpp");

        String okved = (String) data.get("okved");
        if (okved == null) {
            var okveds = (List<?>) data.get("okveds");
            if (okveds != null && !okveds.isEmpty()) {
                var o0 = (Map<?, ?>) okveds.getFirst();
                okved = (String) o0.get("code");
            }
        }

        if (mappedInn == null || mappedInn.isBlank()
                || mappedKpp == null || mappedKpp.isBlank()
                || okved == null || okved.isBlank()) {
            throw new DadataClientException("DADATA_ERROR_WHILE_PARSING_INN_KPP_OKVED");
        }

        return new CreateCompanyRequest(title, mappedInn, mappedKpp, okved);
    }
}
