package ru.rtkmagistral.magistralapi.exception;

import java.util.Objects;

/**
 * Ошибка адресного разрешения или тарифного расчёта.
 * Отделена от {@link OrderException}, чтобы ошибки цены не маскировались состоянием создания заказа.
 */
public class PricingException extends RuntimeException {
    private final PricingErrorCode code;

    public PricingException(PricingErrorCode code) {
        super(Objects.requireNonNull(code, "code").name());
        this.code = code;
    }

    public PricingException(PricingErrorCode code, Throwable cause) {
        super(Objects.requireNonNull(code, "code").name(), cause);
        this.code = code;
    }

    public PricingErrorCode getCode() {
        return code;
    }
}
