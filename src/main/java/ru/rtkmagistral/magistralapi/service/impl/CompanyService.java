package ru.rtkmagistral.magistralapi.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.rtkmagistral.magistralapi.client.dadata.spec.IDadataClient;
import ru.rtkmagistral.magistralapi.domain.jpa.Company;
import ru.rtkmagistral.magistralapi.domain.jpa.User;
import ru.rtkmagistral.magistralapi.dto.company.CreateCompanyRequest;
import ru.rtkmagistral.magistralapi.exception.CompanyException;
import ru.rtkmagistral.magistralapi.mapper.ICompanyMapper;
import ru.rtkmagistral.magistralapi.repository.CompanyRepository;
import ru.rtkmagistral.magistralapi.service.spec.ICompanyService;

/**
 * Сервис для процессов бизнес-логики, связанных с доменной сущностью "Компания".
 */
@Service
@RequiredArgsConstructor
public class CompanyService implements ICompanyService {
    private final IDadataClient dadataClient;

    private final ICompanyMapper companyMapper;

    private final CompanyRepository companyRepository;

    /**
     * Метод для проверки корректности переданных с фронтенда данных путём получения информации из источников Dadata API.
     *
     * @param req запрос на создание компании.
     * @return результат проверки на существование компании.
     */
    @Override
    public boolean verifyData(CreateCompanyRequest req) {
        String innReq = req.getInn().trim();
        String kppReq = req.getKpp().trim();
        String okvedReq = req.getOkved().trim().replace(" ", "");
        String titleReq = req.getTitle();

        CreateCompanyRequest companyRequestFormClient = dadataClient.findPartyByInn(innReq);

        if (companyRequestFormClient == null) {
            throw new CompanyException("COMPANY_WITH_THIS_INN_NOT_EXISTS_IN_DADATA");
        }

        if (!titleReq.equals(companyRequestFormClient.getTitle())) {
            throw new CompanyException("TITLE_NOT_MATCHES_WITH_DADATA");
        }

        if (!innReq.equals(companyRequestFormClient.getInn())) {
            throw new CompanyException("INN_NOT_MATCHES_WITH_DADATA");
        }

        if (!kppReq.equals(companyRequestFormClient.getKpp())) {
            throw new CompanyException("KPP_NOT_MATCHES_WITH_DADATA");
        }

        if (!okvedReq.equals(companyRequestFormClient.getOkved())) {
            throw new CompanyException("OKVED_NOT_MATCHES_WITH_DADATA");
        }

        return true;
    }

    /**
     * Метод для создания компании в системе.
     *
     * @param createCompanyRequest DTO с данными для создания компании.
     * @param user                 пользователь, который привязан к компании.
     */
    @Override
    public void createCompany(CreateCompanyRequest createCompanyRequest, User user) {
        if (verifyData(createCompanyRequest)) {
            if (companyRepository.existsCompanyByInn(createCompanyRequest.getInn())) {
                throw new CompanyException("COMPANY_ALREADY_EXISTS_IN_DATABASE");
            }

            Company company = companyMapper.toEntity(createCompanyRequest);
            company.setUser(user);
            companyRepository.save(company);
        } else {
            throw new CompanyException("PROBLEMS_WHILE_ADDING_A_COMPANY");
        }
    }
}
