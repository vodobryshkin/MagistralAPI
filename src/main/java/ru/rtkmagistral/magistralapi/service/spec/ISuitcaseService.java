package ru.rtkmagistral.magistralapi.service.spec;

import ru.rtkmagistral.magistralapi.dto.order.OrderResponseDTO;
import ru.rtkmagistral.magistralapi.dto.suitcase.CreateSuitcaseRequest;

import java.util.UUID;

/**
 * Интерфейс для определения функциональности SuitcaseService.
 */
public interface ISuitcaseService {
    /**
     * Идемпотентно оформляет заявку на доставку чемодана. Данные о чемодане берутся из формы,
     * получателем подставляется Магистраль (реквизиты из конфигурации), после чего заявка
     * обрабатывается общим потоком создания заказа.
     *
     * @param id ключ идемпотентности.
     * @param email адрес электронной почты отправителя, к которому привязывается заявка.
     * @param createSuitcaseRequest данные формы «Чемоданы».
     * @param method http-метод запроса.
     * @param path URI запроса.
     * @return результат оформления заявки на доставку чемодана.
     */
    OrderResponseDTO createIdempotentSuitcase(UUID id,
                                              String email,
                                              CreateSuitcaseRequest createSuitcaseRequest,
                                              String method,
                                              String path);
}
