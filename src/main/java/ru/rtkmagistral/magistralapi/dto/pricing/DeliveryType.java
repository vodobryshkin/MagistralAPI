package ru.rtkmagistral.magistralapi.dto.pricing;

/**
 * Тип доставки, определяющий тарифную таблицу: от двери клиента или от отделения спецсвязи («окна»).
 * Выводится из того, указан ли по каждой стороне адрес (дверь) или пункт выдачи (окно).
 */
public enum DeliveryType {
    DOOR_DOOR,
    WINDOW_DOOR,
    WINDOW_WINDOW;

    /**
     * Определяет тип доставки по признакам «отправление/получение через отделение спецсвязи».
     *
     * @param senderIsOffice   отправление сдаётся в отделение (окно), иначе забор по адресу (дверь).
     * @param receiverIsOffice получение в отделении (окно), иначе доставка по адресу (дверь).
     * @return соответствующий тип доставки.
     */
    public static DeliveryType of(boolean senderIsOffice, boolean receiverIsOffice) {
        if (senderIsOffice && receiverIsOffice) {
            return WINDOW_WINDOW;
        }
        if (!senderIsOffice && !receiverIsOffice) {
            return DOOR_DOOR;
        }
        return WINDOW_DOOR;
    }
}
