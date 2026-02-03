package ru.rtkmagistral.magistralapi.domain.jpa;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
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
@ToString
public class User {
    /**
     * Enum user_type, описанный в DDL.
     */
    public enum UserType {
        INDIVIDUAL,
        BUSINESS
    }

    /**
     * Конструктор для сущности "Пользователь". В него передаются все поля, которые можно считать с DTO CreateUserRequest.
     *
     * @param name имя пользователя.
     * @param surname фамилия пользователя.
     * @param fathersName отчество пользователя.
     * @param email адрес электронной почты пользователя.
     * @param phone номер телефона пользователя.
     * @param passwordHash пароль пользователя, к которому было применено хэширование.
     */
    public User(String name, String surname, String fathersName, String email, String phone, byte[] passwordHash) {
        this.name = name;
        this.surname = surname;
        this.fathersName = fathersName;
        this.email = email;
        this.phone = phone;
        this.passwordHash = passwordHash;
    }

    /**
     * Уникальный идентификатор пользователя.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Компания, к которой прикреплён пользователь.
     */
    @OneToOne(mappedBy = "user")
    private Company company;

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
     * Статус аккаунта пользователя (подтверждён/не подтверждён). Не может быть null-значением.
     */
    @Column(name = "verified", nullable = false)
    private boolean verified;

    /**
     * Тип аккаунта пользователя. Может принимать только значения enum'а UserType. Не может быть null-значением.
     */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "acc_type", nullable = false)
    private UserType userType;
}

