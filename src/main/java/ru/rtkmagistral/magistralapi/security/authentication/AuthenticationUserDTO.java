package ru.rtkmagistral.magistralapi.security.authentication;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

/**
 * DTO для получения необходимых данных о пользователе с помощью DAO.
 */
@Data
@AllArgsConstructor
public class AuthenticationUserDTO {
    private UUID uuid;
    private String email;
    private byte[] passwordHash;
}
