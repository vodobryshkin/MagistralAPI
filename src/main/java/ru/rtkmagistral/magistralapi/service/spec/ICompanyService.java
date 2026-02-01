package ru.rtkmagistral.magistralapi.service.spec;

import ru.rtkmagistral.magistralapi.domain.jpa.User;
import ru.rtkmagistral.magistralapi.dto.company.CreateCompanyRequest;

/**
 * Интерфейс, который описывает функциональность класса, отвечающего за бизнес-логику, связанную с работой с компаниями.
 */
public interface ICompanyService {
    /**
     * Метод для проверки корректности переданных с фронтенда данных путём получения информации из источников Dadata API.
     *
     * @param createCompanyRequest запрос на создание компании.
     * @return результат проверки на существование компании.
     */
    boolean verifyData(CreateCompanyRequest createCompanyRequest);

    /**
     * Метод для создания компании в системе.
     *
     * @param createCompanyRequest DTO с данными для создания компании.
     * @param user пользователь, который привязан к компании.
     */
    void createCompany(CreateCompanyRequest createCompanyRequest, User user);
}
