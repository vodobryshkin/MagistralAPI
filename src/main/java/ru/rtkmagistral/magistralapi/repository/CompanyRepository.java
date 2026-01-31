package ru.rtkmagistral.magistralapi.repository;

import org.springframework.data.repository.CrudRepository;
import ru.rtkmagistral.magistralapi.domain.jpa.Company;

import java.util.UUID;

/**
 * Интерфейс репозитория для работы с сущностью "Компания".
 */
public interface CompanyRepository extends CrudRepository<Company, UUID> {
    /**
     * Метод для проверки на то, существует ли компания с переданным ИНН в базе или нет.
     *
     * @param inn ИНН компании.
     * @return результат проверки на существование компании.
     */
    boolean existsCompanyByInn(String inn);
}
