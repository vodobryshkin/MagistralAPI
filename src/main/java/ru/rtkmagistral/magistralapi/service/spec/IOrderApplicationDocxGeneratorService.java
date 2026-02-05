package ru.rtkmagistral.magistralapi.service.spec;

import ru.rtkmagistral.magistralapi.domain.jpa.Order;

import java.io.IOException;
import java.nio.file.Path;
import java.time.OffsetDateTime;

/**
 * Интерфейс для определения функциональности OrderApplicationDocxGeneratorService.
 */
public interface IOrderApplicationDocxGeneratorService {
    /**
     * Генерирует DOCX-заявку по шаблону и данным заказа.
     *
     * @param templatePath путь к шаблону .docx
     * @param order заказ, из него берутся адреса/параметры груза/отправитель/получатель
     * @param applicationNumber номер заявки (в шапку "ЗАЯВКА № ...")
     * @param applicationDate дата заявки (в шапку, формат dd.MM.yyyy)
     * @param contractText строка с договором/офертой (для 2-й строки таблицы)
     * @param pickupDate дата забора отправления исполнителем (может быть null)
     * @param deliveryDate предполагаемая дата вручения (может быть null)
     * @param places количество мест (если < 1 — будет 1)
     * @param extraInfo доп. инфа для последней строки (может быть пусто/null)
     * @param companyName название компании для BUSINESS (если пусто — будет ФИО)
     * @return готовый docx в байтах
     * @throws IOException если шаблон не читается или docx не записывается
     */
     byte[] generate(
            Path templatePath,
            Order order,
            String applicationNumber,
            OffsetDateTime applicationDate,
            String contractText,
            OffsetDateTime pickupDate,
            OffsetDateTime deliveryDate,
            int places,
            String extraInfo,
            String companyName
    ) throws IOException;
}
