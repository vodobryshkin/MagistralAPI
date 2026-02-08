package ru.rtkmagistral.magistralapi.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.rtkmagistral.magistralapi.domain.jpa.Order;
import ru.rtkmagistral.magistralapi.domain.jpa.User;

/**
 * Интерфейс репозитория для работы с сущностью "Заказ на доставку".
 */
@Repository
public interface IOrderRepository extends CrudRepository<Order, Long> {
    long countOrdersByUser(User user);
}
