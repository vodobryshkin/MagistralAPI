package ru.rtkmagistral.magistralapi.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.rtkmagistral.magistralapi.domain.jpa.Order;

import java.util.UUID;

/**
 * Интерфейс репозитория для работы с сущностью "Заказ на доставку".
 */
@Repository
public interface IOrderRepository extends CrudRepository<Order, UUID> {
}
