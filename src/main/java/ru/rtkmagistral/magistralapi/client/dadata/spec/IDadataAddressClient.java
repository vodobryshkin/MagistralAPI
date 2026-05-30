package ru.rtkmagistral.magistralapi.client.dadata.spec;

import ru.rtkmagistral.magistralapi.dto.pricing.DadataAddress;

import java.util.Optional;

/**
 * Клиент сервиса Dadata для разбора почтового адреса (определение города и субъекта РФ).
 */
public interface IDadataAddressClient {
    /**
     * Разбирает свободный текст адреса и возвращает город и субъект РФ.
     *
     * @param address строка адреса.
     * @return разобранный адрес или {@link Optional#empty()}, если сервис не вернул данных.
     */
    Optional<DadataAddress> resolveAddress(String address);
}
