package ru.rtkmagistral.magistralapi.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import ru.rtkmagistral.magistralapi.domain.redis.ConfirmationLink;
import ru.rtkmagistral.magistralapi.dto.token.VerifyResponse;
import ru.rtkmagistral.magistralapi.exception.ConfirmationLinkException;
import ru.rtkmagistral.magistralapi.repository.IConfirmationLinkRepository;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfirmationLinkServiceTest {

    @Mock
    IConfirmationLinkRepository confirmationLinkRepository;

    @InjectMocks
    ConfirmationLinkService confirmationLinkService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(confirmationLinkService, "ttl", 86400L);
    }

    @Test
    @DisplayName("verifyConfirmationLink возвращает email и удаляет токен при удачной валидации")
    void verifyConfirmationLink_success_returnsEmailAndDeletes() {
        UUID id = UUID.randomUUID();
        ConfirmationLink link = new ConfirmationLink(id, "vova@example.com", 86400L);
        when(confirmationLinkRepository.findById(id)).thenReturn(Optional.of(link));

        VerifyResponse response = confirmationLinkService.verifyConfirmationLink(id.toString());

        assertThat(response.isStatus()).isTrue();
        assertThat(response.getMessage()).isEqualTo("vova@example.com");
        verify(confirmationLinkRepository).deleteById(id);
    }

    @Test
    @DisplayName("verifyConfirmationLink бросает исключение если ссылка не найдена")
    void verifyConfirmationLink_missing_throws() {
        UUID id = UUID.randomUUID();
        when(confirmationLinkRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> confirmationLinkService.verifyConfirmationLink(id.toString()))
                .isInstanceOf(ConfirmationLinkException.class)
                .hasMessage("CONFIRMATION_LINK_HAS_EXPIRED_OR_INVALID");
    }

    @Test
    @DisplayName("verifyConfirmationLink кидает IllegalArgumentException для некорректного UUID")
    void verifyConfirmationLink_invalidUuid_throwsIAE() {
        assertThatThrownBy(() -> confirmationLinkService.verifyConfirmationLink("not-a-uuid"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("generateConfirmationLink создаёт уникальный ID и проставляет TTL")
    void generateConfirmationLink_buildsLinkWithTtl() {
        ConfirmationLink first = confirmationLinkService.generateConfirmationLink("vova@example.com");
        ConfirmationLink second = confirmationLinkService.generateConfirmationLink("vova@example.com");

        assertThat(first.getEmail()).isEqualTo("vova@example.com");
        assertThat(first.getTtlSeconds()).isEqualTo(86400L);
        assertThat(first.getId()).isNotNull();
        assertThat(first.getId()).isNotEqualTo(second.getId());
    }

    @Test
    @DisplayName("saveConfirmationLink делегирует репозиторию")
    void saveConfirmationLink_delegatesToRepo() {
        ConfirmationLink link = new ConfirmationLink(UUID.randomUUID(), "vova@example.com", 86400L);
        confirmationLinkService.saveConfirmationLink(link);

        verify(confirmationLinkRepository).save(link);
    }
}
