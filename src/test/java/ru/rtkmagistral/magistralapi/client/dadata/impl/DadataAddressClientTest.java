package ru.rtkmagistral.magistralapi.client.dadata.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import ru.rtkmagistral.magistralapi.dto.pricing.DadataAddress;
import ru.rtkmagistral.magistralapi.exception.DadataClientException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.http.HttpMethod.POST;

class DadataAddressClientTest {

    @Test
    @DisplayName("Клиент разбирает региональные, ФИАС- и населённые поля ответа Dadata")
    void resolveAddress_parsesStructuredFields() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://dadata.test/suggest/address");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        DadataAddressClient client = new DadataAddressClient(builder.build());

        server.expect(once(), requestTo("https://dadata.test/suggest/address"))
                .andExpect(method(POST))
                .andExpect(content().json("""
                        {"query":"Ленинградская обл, г Гатчина","count":1}
                        """))
                .andRespond(withSuccess("""
                        {
                          "suggestions": [
                            {
                              "value": "Ленинградская обл, г Гатчина",
                              "unrestricted_value": "188300, Ленинградская обл, г Гатчина",
                              "data": {
                                "country_iso_code": "RU",
                                "region": "Ленинградская",
                                "region_fias_id": "6d1ebb35-70c6-4129-bd55-da3969658f5d",
                                "region_iso_code": "RU-LEN",
                                "area": "Гатчинский",
                                "area_fias_id": "area-id",
                                "city": "Гатчина",
                                "city_fias_id": "city-id",
                                "settlement": null,
                                "settlement_fias_id": null,
                                "capital_marker": "0",
                                "fias_level": "4"
                              }
                            }
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));

        Optional<DadataAddress> result = client.resolveAddress("Ленинградская обл, г Гатчина");

        assertThat(result).isPresent();
        DadataAddress address = result.orElseThrow();
        assertThat(address.countryIsoCode()).isEqualTo("RU");
        assertThat(address.region()).isEqualTo("Ленинградская");
        assertThat(address.regionIsoCode()).isEqualTo("RU-LEN");
        assertThat(address.regionFiasId()).isEqualTo("6d1ebb35-70c6-4129-bd55-da3969658f5d");
        assertThat(address.city()).isEqualTo("Гатчина");
        assertThat(address.localityName()).isEqualTo("Гатчина");
        assertThat(address.capitalMarker()).isZero();
        assertThat(address.fiasLevel()).isEqualTo(4);
        assertThat(address.unrestrictedValue()).startsWith("188300");
        server.verify();
    }

    @Test
    @DisplayName("Пустой список подсказок превращается в Optional.empty")
    void resolveAddress_emptySuggestionsReturnsEmpty() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://dadata.test/suggest/address");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        DadataAddressClient client = new DadataAddressClient(builder.build());

        server.expect(requestTo("https://dadata.test/suggest/address"))
                .andRespond(withSuccess("{\"suggestions\":[]}", MediaType.APPLICATION_JSON));

        assertThat(client.resolveAddress("неизвестный адрес")).isEmpty();
        server.verify();
    }

    @Test
    @DisplayName("Сетевая/HTTP-ошибка Dadata оборачивается в DadataClientException")
    void resolveAddress_httpErrorIsWrapped() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://dadata.test/suggest/address");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        DadataAddressClient client = new DadataAddressClient(builder.build());

        server.expect(requestTo("https://dadata.test/suggest/address"))
                .andRespond(withServerError());

        assertThatThrownBy(() -> client.resolveAddress("г Гатчина"))
                .isInstanceOf(DadataClientException.class)
                .hasMessageContaining("DADATA_ERROR_WITH_CODE");
        server.verify();
    }
}
