package ru.rtkmagistral.magistralapi.service.spec;

import ru.rtkmagistral.magistralapi.dto.order.OrderDocumentDTO;

import java.io.IOException;
import java.io.InputStream;

/**
 * Интерфейс для определения функциональности OrderApplicationDocxGeneratorService.
 */
public interface IOrderApplicationDocxGeneratorService {
    /**
     * Генерирует DOCX-заявку по шаблону и данным заказа.
     *
     * @return готовый docx в байтах
     * @throws IOException если шаблон не читается или docx не записывается
     */
    byte[] generate(InputStream template, OrderDocumentDTO dto) throws IOException;
}
