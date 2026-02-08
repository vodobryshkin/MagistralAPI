package ru.rtkmagistral.magistralapi.service.spec;

import org.springframework.web.multipart.MultipartFile;

public interface IMinioService {
    /**
     * Сервис для загрузки файла в MinIO по пути:
     * orders/<год>/<месяц>/<день>/<filename>
     *
     * @param file файл на загрузку.
     * @return ключ объекта в MinIO.
     */
    String uploadToOrders(MultipartFile file);
}
