package ru.rtkmagistral.magistralapi.client.dadata.spec;

import ru.rtkmagistral.magistralapi.dto.pricing.DadataAddress;

import java.util.Optional;

/**
 * Клиент Dadata для структурирования почтового адреса.
 */
public interface IDadataAddressClient {
    /**
     * Разбирает адрес и возвращает субъект РФ, населённый пункт и ФИАС/ISO-идентификаторы.
     *
     * @param address строка адреса.
     * @return разобранный адрес или {@link Optional#empty()}, если Dadata не нашла подходящий вариант.
     */
    Optional<DadataAddress> resolveAddress(String address);
}
