package ru.rtkmagistral.magistralapi.domain.jpa;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

/**
 * Доменная сущность "Компания". Описывает юридических лиц в системе.
 */
@Entity
@Table(name = "companies")
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Company {
    /**
     * Конструктор для сущности "Компания". В него передаются все поля, которые можно считать с DTO CreateCompanyRequest.
     *
     * @param title название компании.
     * @param inn ИНН компании.
     * @param kpp КПП компании.
     * @param okved ОКВЭД компании.
     */
    public Company(String title, String inn, String kpp, String okved) {
        this.title = title;
        this.inn = inn;
        this.kpp = kpp;
        this.okved = okved;
    }

    /**
     * Уникальный идентификатор компании.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Пользователь, который прикреплён к компании. Не может быть null-значением и должен быть уникальным для каждого пользователя.
     */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    /**
     * Название компании. Не может быть null-значением.
     */
    @Column(name = "title", columnDefinition = "TEXT", nullable = false)
    private String title;

    /**
     * ИНН компании. Не может быть null-значением и должен быть уникальным для каждого пользователя.
     */
    @Column(name = "inn", nullable = false, unique = true)
    private String inn;

    /**
     * КПП компании. Не может быть null-значением и должен быть уникальным для каждого пользователя.
     */
    @Column(name = "kpp", nullable = false, unique = true)
    private String kpp;

    /**
     * ОКВЭД компании. Не может быть null-значением.
     */
    @Column(name = "okved", nullable = false)
    private String okved;
}
