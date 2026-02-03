package ru.rtkmagistral.magistralapi.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.rtkmagistral.magistralapi.domain.redis.ConfirmationLink;
import ru.rtkmagistral.magistralapi.dto.token.VerifyResponse;
import ru.rtkmagistral.magistralapi.exception.ConfirmationLinkException;
import ru.rtkmagistral.magistralapi.repository.IConfirmationLinkRepository;
import ru.rtkmagistral.magistralapi.service.spec.IConfirmationLinkService;

import java.util.Optional;
import java.util.UUID;

/**
 * Сервис для работы с кодами для подтверждения регистрации.
 */
@RequiredArgsConstructor
@Service
public class ConfirmationLinkService implements IConfirmationLinkService {
    @Value("${confirmationlink.ttl}")
    private Long ttl;

    private final IConfirmationLinkRepository confirmationLinkRepository;


    /**
     * Метод для подтверждения корректности ссылки на подтверждение аккаунта
     *
     * @param id id ссылки
     */
    @Override
    public VerifyResponse verifyConfirmationLink(String id) {
        UUID uuid = UUID.fromString(id);
        Optional<ConfirmationLink> confirmationLinkOptional = confirmationLinkRepository.findById(uuid);

        if (confirmationLinkOptional.isPresent()) {
            String username = confirmationLinkOptional.get().getEmail();
            confirmationLinkRepository.deleteById(uuid);

            return new VerifyResponse(true, username);
        }

        throw new ConfirmationLinkException("CONFIRMATION_LINK_HAS_EXPIRED_OR_INVALID");
    }

    /**
     * Метод для генерации ссылки на подтверждение аккаунта
     *
     * @param username имя пользователя, с аккаунтом которого будет связана ссылка
     * @return сгенерированную ссылку.
     */
    @Override
    public ConfirmationLink generateConfirmationLink(String username) {
        return new ConfirmationLink(UUID.randomUUID(), username, ttl);
    }

    /**
     * Метод для сохранения в Redis токена на подтверждение аккаунта
     *
     * @param confirmationLink токен для подтверждения регистрации
     */
    @Override
    public void saveConfirmationLink(ConfirmationLink confirmationLink) {
        confirmationLinkRepository.save(confirmationLink);
    }
}
