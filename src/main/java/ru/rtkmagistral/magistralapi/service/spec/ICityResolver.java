package ru.rtkmagistral.magistralapi.service.spec;

import ru.rtkmagistral.magistralapi.dto.pricing.ResolvedLocation;

/**
 * Приводит свободный текст адреса к данным, необходимым для тарификации:
 * городу из таблицы зон, региональному коэффициенту и доплате за отдалённость.
 */
public interface ICityResolver {
    /**
     * Разбирает адрес: приоритетно через сервис Dadata, при недоступности — по локальному справочнику городов.
     *
     * @param address строка адреса.
     * @return найденный город таблицы зон с коэффициентом и доплатой за отдалённость.
     */
    ResolvedLocation resolve(String address);
}
