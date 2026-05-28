package ru.rtkmagistral.magistralapi.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.rtkmagistral.magistralapi.client.dadata.spec.IDadataClient;
import ru.rtkmagistral.magistralapi.domain.jpa.Company;
import ru.rtkmagistral.magistralapi.domain.jpa.User;
import ru.rtkmagistral.magistralapi.dto.company.CreateCompanyRequest;
import ru.rtkmagistral.magistralapi.exception.CompanyException;
import ru.rtkmagistral.magistralapi.mapper.ICompanyMapper;
import ru.rtkmagistral.magistralapi.repository.CompanyRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {

    @Mock
    IDadataClient dadataClient;
    @Mock
    ICompanyMapper companyMapper;
    @Mock
    CompanyRepository companyRepository;

    @InjectMocks
    CompanyService companyService;

    private CreateCompanyRequest req(String title, String inn, String kpp, String okved) {
        return new CreateCompanyRequest(title, inn, kpp, okved, true);
    }

    @Test
    @DisplayName("verifyData возвращает true когда все поля совпадают с Dadata")
    void verifyData_allMatches_returnsTrue() {
        CreateCompanyRequest req = req("Магистраль", "1234567890", "123456789", "62.01");
        CreateCompanyRequest fromDadata = req("Магистраль", "1234567890", "123456789", "62.01");

        when(dadataClient.findPartyByInn("1234567890")).thenReturn(fromDadata);

        assertThat(companyService.verifyData(req)).isTrue();
    }

    @Test
    @DisplayName("verifyData допускает null OKVED в запросе")
    void verifyData_nullOkved_skipsOkvedCheck() {
        CreateCompanyRequest req = req("Магистраль", "1234567890", "123456789", null);
        CreateCompanyRequest fromDadata = req("Магистраль", "1234567890", "123456789", "62.01");

        when(dadataClient.findPartyByInn("1234567890")).thenReturn(fromDadata);

        assertThat(companyService.verifyData(req)).isTrue();
    }

    @Test
    @DisplayName("verifyData кидает COMPANY_WITH_THIS_INN_NOT_EXISTS_IN_DADATA когда Dadata вернула null")
    void verifyData_dadataNull_throws() {
        CreateCompanyRequest req = req("Магистраль", "1234567890", "123456789", "62.01");
        when(dadataClient.findPartyByInn("1234567890")).thenReturn(null);

        assertThatThrownBy(() -> companyService.verifyData(req))
                .isInstanceOf(CompanyException.class)
                .hasMessage("COMPANY_WITH_THIS_INN_NOT_EXISTS_IN_DADATA");
    }

    @Test
    @DisplayName("verifyData кидает TITLE_NOT_MATCHES_WITH_DADATA при несовпадении названия")
    void verifyData_titleMismatch_throws() {
        CreateCompanyRequest req = req("Магистраль", "1234567890", "123456789", "62.01");
        CreateCompanyRequest fromDadata = req("Другая Компания", "1234567890", "123456789", "62.01");
        when(dadataClient.findPartyByInn("1234567890")).thenReturn(fromDadata);

        assertThatThrownBy(() -> companyService.verifyData(req))
                .isInstanceOf(CompanyException.class)
                .hasMessage("TITLE_NOT_MATCHES_WITH_DADATA");
    }

    @Test
    @DisplayName("verifyData кидает INN_NOT_MATCHES_WITH_DADATA при несовпадении ИНН")
    void verifyData_innMismatch_throws() {
        CreateCompanyRequest req = req("Магистраль", "1234567890", "123456789", "62.01");
        CreateCompanyRequest fromDadata = req("Магистраль", "0987654321", "123456789", "62.01");
        when(dadataClient.findPartyByInn("1234567890")).thenReturn(fromDadata);

        assertThatThrownBy(() -> companyService.verifyData(req))
                .isInstanceOf(CompanyException.class)
                .hasMessage("INN_NOT_MATCHES_WITH_DADATA");
    }

    @Test
    @DisplayName("verifyData кидает KPP_NOT_MATCHES_WITH_DADATA при несовпадении КПП")
    void verifyData_kppMismatch_throws() {
        CreateCompanyRequest req = req("Магистраль", "1234567890", "123456789", "62.01");
        CreateCompanyRequest fromDadata = req("Магистраль", "1234567890", "987654321", "62.01");
        when(dadataClient.findPartyByInn("1234567890")).thenReturn(fromDadata);

        assertThatThrownBy(() -> companyService.verifyData(req))
                .isInstanceOf(CompanyException.class)
                .hasMessage("KPP_NOT_MATCHES_WITH_DADATA");
    }

    @Test
    @DisplayName("verifyData кидает OKVED_NOT_MATCHES_WITH_DADATA при несовпадении ОКВЭД")
    void verifyData_okvedMismatch_throws() {
        CreateCompanyRequest req = req("Магистраль", "1234567890", "123456789", "01.01");
        CreateCompanyRequest fromDadata = req("Магистраль", "1234567890", "123456789", "62.01");
        when(dadataClient.findPartyByInn("1234567890")).thenReturn(fromDadata);

        assertThatThrownBy(() -> companyService.verifyData(req))
                .isInstanceOf(CompanyException.class)
                .hasMessage("OKVED_NOT_MATCHES_WITH_DADATA");
    }

    @Test
    @DisplayName("verifyData обрезает пробелы внутри ОКВЭД перед сравнением")
    void verifyData_okvedWithSpaces_stripped() {
        CreateCompanyRequest req = req("Магистраль", "1234567890", "123456789", " 62. 01 ");
        CreateCompanyRequest fromDadata = req("Магистраль", "1234567890", "123456789", "62.01");
        when(dadataClient.findPartyByInn("1234567890")).thenReturn(fromDadata);

        assertThat(companyService.verifyData(req)).isTrue();
    }

    @Test
    @DisplayName("createCompany сохраняет компанию и устанавливает пользователя")
    void createCompany_validData_savesCompanyWithUser() {
        CreateCompanyRequest req = req("Магистраль", "1234567890", "123456789", "62.01");
        when(dadataClient.findPartyByInn("1234567890")).thenReturn(req);
        when(companyRepository.existsCompanyByInn("1234567890")).thenReturn(false);

        Company company = new Company("МАГИСТРАЛЬ", "1234567890", "123456789", "62.01");
        when(companyMapper.toEntity(req)).thenReturn(company);

        User user = new User();
        companyService.createCompany(req, user);

        ArgumentCaptor<Company> companyCaptor = ArgumentCaptor.forClass(Company.class);
        verify(companyRepository).save(companyCaptor.capture());
        assertThat(companyCaptor.getValue().getUser()).isSameAs(user);
    }

    @Test
    @DisplayName("createCompany кидает COMPANY_ALREADY_EXISTS_IN_DATABASE если ИНН уже есть в БД")
    void createCompany_duplicateInn_throws() {
        CreateCompanyRequest req = req("Магистраль", "1234567890", "123456789", "62.01");
        when(dadataClient.findPartyByInn("1234567890")).thenReturn(req);
        when(companyRepository.existsCompanyByInn("1234567890")).thenReturn(true);

        assertThatThrownBy(() -> companyService.createCompany(req, new User()))
                .isInstanceOf(CompanyException.class)
                .hasMessage("COMPANY_ALREADY_EXISTS_IN_DATABASE");

        verify(companyRepository, never()).save(any());
    }
}
