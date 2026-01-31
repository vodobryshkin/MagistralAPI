package ru.rtkmagistral.magistralapi.client.dadata.spec;

import ru.rtkmagistral.magistralapi.dto.company.CreateCompanyRequest;

/**
 * Интерфейс, который нужен для описания функциональности клиента для работы с сервисом dadata.ru
 */
public interface IDadataClient {
    /**
     * Метод для обращения к сервису Dadata за данными о компании по её ИНН.
     *
     * @param inn ИНН компании.
     * @return необходимые данные для компании.
     */
    CreateCompanyRequest findPartyByInn(String inn);
}
