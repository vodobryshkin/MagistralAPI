package ru.rtkmagistral.magistralapi.domain.jpa;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

/**
 * Доменная сущность "Пользователь". Описывает пользователей системы.
 */
@Entity
@Table(name="users")
@NoArgsConstructor
@Getter
@Setter
public class User {
    /**
     * Enum user_type, описанный в DDL.
     */
    public enum UserType {
        INDIVIDUAL,
        BUSINESS
    }

    /**
     * Уникальный идентификатор пользователя.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Имя пользователя. Не может быть null-значением.
     */
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * Фамилия пользователя. Не может быть null-значением.
     */
    @Column(name = "surname", nullable = false)
    private String surname;

    /**
     * Отчество пользователя.
     */
    @Column(name = "fathers_name")
    private String fathersName;

    /**
     * Адрес электронной почты пользователя. Не может быть null-значением и должен быть уникальным для каждого пользователя.
     */
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    /**
     * Телефон пользователя. Не может быть null-значением и должен быть уникальным для каждого пользователя.
     */
    @Column(name = "phone", nullable = false, unique = true)
    private String phone;

    /**
     * Хэш пароля пользователя. Не может быть null-значением.
     */
    @Column(name = "password_hash", nullable = false)
    private byte[] passwordHash;

    /**
     * Тип аккаунта пользователя. Может принимать только значения enum'а UserType. Не может быть null-значением.
     */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "acc_type", nullable = false)
    private UserType userType;
}

