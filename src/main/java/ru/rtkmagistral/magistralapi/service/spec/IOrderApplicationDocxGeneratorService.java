package ru.rtkmagistral.magistralapi.service.spec;

import ru.rtkmagistral.magistralapi.dto.order.OrderDocumentDTO;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Интерфейс для определения функциональности OrderApplicationDocxGeneratorService.
 */
public interface IOrderApplicationDocxGeneratorService {
    /**
     * Генерирует DOCX-заявку по шаблону и данным заказа.
     *
     * @param templatePath путь к шаблону .docx
     * @return готовый docx в байтах
     * @throws IOException если шаблон не читается или docx не записывается
     */
     byte[] generate(
            Path templatePath,
            OrderDocumentDTO orderDocumentDTO
    ) throws IOException;
}
