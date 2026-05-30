package ru.rtkmagistral.magistralapi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
public class DadataClientConfig {

    @Bean
    @Primary
    public RestClient dadataRestClient(
            @Value("${dadata.client.base-url}") String baseUrl,
            @Value("${dadata.client.token}") String token,
            @Value("${dadata.client.timeout:5s}") Duration timeout
    ) {
        return buildClient(baseUrl, token, timeout);
    }

    @Bean
    public RestClient dadataAddressRestClient(
            @Value("${dadata.address.base-url}") String baseUrl,
            @Value("${dadata.client.token}") String token,
            @Value("${dadata.client.timeout:5s}") Duration timeout
    ) {
        return buildClient(baseUrl, token, timeout);
    }

    private RestClient buildClient(String baseUrl, String token, Duration timeout) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(timeout)
                .build();

        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(timeout);

        return RestClient.builder()
                .requestFactory(factory)
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Token " + token)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}

