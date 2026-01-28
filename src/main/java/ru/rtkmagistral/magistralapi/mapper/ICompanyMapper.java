package ru.rtkmagistral.magistralapi.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.rtkmagistral.magistralapi.domain.jpa.Company;
import ru.rtkmagistral.magistralapi.dto.company.CreateCompanyRequest;

/**
 * Маппер для перевода между DTO, которое приходит на добавление компании в системе, и сущностью "Компания".
 */
@Mapper(componentModel = "spring")
public interface ICompanyMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "title", expression = "java(createCompanyRequest.getTitle().toUpperCase())")
    Company toEntity(CreateCompanyRequest createCompanyRequest);
}
