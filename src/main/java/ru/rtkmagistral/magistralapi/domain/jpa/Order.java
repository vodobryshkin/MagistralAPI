package ru.rtkmagistral.magistralapi.domain.jpa;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Доменная сущность "Заказ на доставку". Описывает заказ в системе.
 */
@Entity
@Table(name = "orders")
@NoArgsConstructor
@Getter
@Setter
@ToString(exclude = "user")
public class Order {
    /**
     * Enum TYPE_OF_SHIPMENT описанный в DDL
     */
    public enum TypeOfShipment {
        PACKAGE,
        PACKET
    }

    /**
     * Enum ORDER_STATUS описанный в DDL
     */
    public enum OrderStatus {
        ACCEPTED,
        FINISHED
    }

    /**
     * Уникальный идентификатор заказа.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Пользователь, который привязан к заказу. Не может быть null-значением.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Текущий статус заказа. Может принимать только значения enum'а OrderStatus. Не может быть null-значением.
     */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "order_status", nullable = false)
    private OrderStatus orderStatus;

    /**
     * Адрес, откуда происходит доставка. Не может быть null-значением.
     */
    @Column(name = "shipping_address", length = 512, nullable = false)
    private String shippingAddress;

    /**
     * Адрес, куда осуществляется доставка. Не может быть null-значением.
     */
    @Column(name = "arrival_address", length = 512, nullable = false)
    private String arrivalAddress;

    /**
     * Длина груза в санти сантиметрах. Не может быть null-значением.
     */
    @Column(name = "length_centi_cm", nullable = false)
    private int lengthCentiCm;

    /**
     * Ширина груза в санти сантиметрах. Не может быть null-значением.
     */
    @Column(name = "width_centi_cm", nullable = false)
    private int widthCentiCm;

    /**
     * Высота груза в санти сантиметрах. Не может быть null-значением.
     */
    @Column(name = "height_centi_cm", nullable = false)
    private int heightCentiCm;

    /**
     * Вес груза в граммах. Не может быть null-значением.
     */
    @Column(name = "weight_gr", nullable = false)
    private int weightGr;

    /**
     * Стоимость вложения в копейках. Не может быть null-значением.
     */
    @Column(name = "cost_of_investment_kopeika", nullable = false)
    private Long costOfInvestmentInKopeika;

    /**
     * Вид отправления заказа. Может принимать только значения enum'а TypeOfShipment. Не может быть null-значением.
     */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "type_of_shipment", nullable = false)
    private TypeOfShipment typeOfShipment;

    /**
     * Характер вложения заказа. Может принимать только значения enum'а NatureOfInvestment. Не может быть null-значением.
     */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "nature_of_investment", nullable = false)
    private NatureOfInvestment natureOfInvestment;

    /**
     * Поле, отвечающее за то, нужно ли отправить заказ как можно скорее. Не может быть null-значением.
     */
    @Column(name = "deliver_as_soon_as_possible", nullable = false)
    private boolean deliverAsSoonAsPossible;

    /**
     * Желаемое время доставки.
     */
    @Column(name = "wishing_delivery_time")
    private OffsetDateTime wishingDeliveryTime;

    /**
     * Цена заказа в копейках. Не может быть null-значением.
     */
    @Column(name = "price_kopeika", nullable = false)
    private Long priceInKopeika;

    /**
     * Поле, отвечающее за то, нужно ли отправить заказ как совершенно секретный груз. Не может быть null-значением.
     */
    @Column(name = "secret_cargo", nullable = false)
    private boolean secretCargo;

    /**
     * ФИО получателя заказа. Не может быть null-значением.
     */
    @Column(name = "receiver_fio", nullable = false)
    private String receiverFio;

    /**
     * Номер телефона получателя заказа. Не может быть null-значением.
     */
    @Column(name = "receiver_phone", nullable = false)
    private String receiverPhone;
}
