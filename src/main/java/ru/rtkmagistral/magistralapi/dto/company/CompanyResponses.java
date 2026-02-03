package ru.rtkmagistral.magistralapi.dto.company;


/**
 * Класс для введения констант для ответов на запрос, связанный с доменной сущностью "Компания".
 */
public final class CompanyResponses {
    private CompanyResponses() {}

    /**
     * Ответ со статусом 422 Unprocessable Content, вызванный тем, что организации с таким ИНН (согласно Dadata) не существует.
     */
    public static final CompanyResponse COMPANY_WITH_THIS_INN_NOT_EXISTS_IN_DADATA =
            new CompanyResponse("COMPANY_WITH_THIS_INN_NOT_EXISTS_IN_DADATA", null);

    /**
     * Ответ со статусом 422 Unprocessable Content, вызванный тем, что организации переданный ИНН и ИНН с Dadata не совпадают.
     */
    public static final CompanyResponse INN_NOT_MATCHES_WITH_DADATA =
            new CompanyResponse("INN_NOT_MATCHES_WITH_DADATA", null);

    /**
     * Ответ со статусом 422 Unprocessable Content, вызванный тем, что организации переданный КПП и КПП с Dadata не совпадают.
     */
    public static final CompanyResponse KPP_NOT_MATCHES_WITH_DADATA =
            new CompanyResponse("KPP_NOT_MATCHES_WITH_DADATA", null);

    /**
     * Ответ со статусом 422 Unprocessable Content, вызванный тем, что организации переданный ОКВЭД и ОКВЭД с Dadata не совпадают.
     */
    public static final CompanyResponse OKVED_NOT_MATCHES_WITH_DADATA =
            new CompanyResponse("OKVED_NOT_MATCHES_WITH_DADATA", null);

    /**
     * Ответ со статусом 422 Unprocessable Content, вызванный тем, что организации переданное название компании и название компании с Dadata не совпадают.
     */
    public static final CompanyResponse TITLE_NOT_MATCHES_WITH_DADATA =
            new CompanyResponse("TITLE_NOT_MATCHES_WITH_DADATA", null);

    /**
     * Ответ со статусом 502 Bad Gateway, вызванный ошибкой соединения.
     */
    public static final CompanyResponse PROBLEMS_WHILE_ADDING_A_COMPANY =
            new CompanyResponse("PROBLEMS_WHILE_ADDING_A_COMPANY", null);

    /**
     * Ответ со статусом 409 Conflict, вызванный тем, что компания уже существует в системе.
     */
    public static final CompanyResponse COMPANY_ALREADY_EXISTS_IN_DATABASE =
            new CompanyResponse("COMPANY_ALREADY_EXISTS_IN_DATABASE", null);

    /**
     * Ответ со статусом 400 Bad Request, вызванный тем, что были переданы пустые данные о компании.
     */
    public static final CompanyResponse EMPTY_REQUEST_FOR_DADATA =
            new CompanyResponse("EMPTY_REQUEST_FOR_DADATA", null);

    /**
     * Ответ со статусом 404 Not Found, вызванный тем, что по переданному ИНН dadata не нашла никакой информации.
     */
    public static final CompanyResponse CANT_FIND_DATA_IN_DADATA_FOR_CURRENT_INN =
            new CompanyResponse("CANT_FIND_DATA_IN_DADATA_FOR_CURRENT_INN", null);

    /**
     * Ответ со статусом 422 Unprocessable Content, вызванный тем, что произошла ошибка при парсинге ИНН/КПП/ОКВЭД из полученных данных.
     */
    public static final CompanyResponse DADATA_ERROR_WHILE_PARSING_INN_KPP_OKVED =
            new CompanyResponse("DADATA_ERROR_WHILE_PARSING_INN_KPP_OKVED", null);
}

