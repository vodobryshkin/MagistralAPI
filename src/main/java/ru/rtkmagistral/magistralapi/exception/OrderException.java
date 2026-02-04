package ru.rtkmagistral.magistralapi.exception;

/**
 * Ошибка, которая выкидывается при работе с сущностью "Заказ на доставку".
 */
public class OrderException extends RuntimeException {
    public OrderException(String message) {
        super(message);
    }
}
