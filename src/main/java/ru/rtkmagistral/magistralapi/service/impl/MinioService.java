package ru.rtkmagistral.magistralapi.service.impl;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.rtkmagistral.magistralapi.service.spec.IMinioService;

import java.io.InputStream;
import java.security.InvalidKeyException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class MinioService implements IMinioService {
    @Value("${minio.main-bucket-name}")
    private String bucket;

    private final MinioClient minioClient;

    /**
     * Сервис для загрузки файла в MinIO по пути:
     * orders/<год>/<месяц>/<день>/<filename>
     *
     * @param file файл на загрузку.
     * @return ключ объекта в MinIO.
     */
    @Override
    public String uploadToOrders(MultipartFile file) {
        Objects.requireNonNull(file, "file is null");

        LocalDate d = LocalDate.now(ZoneId.of("Europe/Moscow"));

        String originalName = file.getOriginalFilename();
        String filename = sanitizeFilename(originalName);

        String objectName = "orders/%04d/%02d/%02d/%s"
                .formatted(d.getYear(), d.getMonthValue(), d.getDayOfMonth(), filename);

        String contentType = file.getContentType();

        if (contentType == null || contentType.isBlank()) {
            contentType = "application/octet-stream";
        }

        try (InputStream is = file.getInputStream()) {
            long size = file.getSize();

            PutObjectArgs.Builder args = PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .contentType(contentType);

            if (size >= 0) {
                args.stream(is, size, -1);
            } else {
                args.stream(is, -1, 10 * 1024 * 1024);
            }

            minioClient.putObject(args.build());
            return objectName;

        } catch (ErrorResponseException | InsufficientDataException | InternalException |
                 InvalidKeyException | InvalidResponseException | ServerException |
                 XmlParserException e) {
            throw new RuntimeException("MinIO error while uploading: " + objectName, e);
        } catch (Exception e) {
            throw new RuntimeException("Upload failed: " + objectName, e);
        }
    }

    private static String sanitizeFilename(String name) {
        if (name == null || name.isBlank()) {
            return "file";
        }

        String cleaned = name
                .replace("\\", "_")
                .replace("/", "_")
                .replaceAll("[\\r\\n\\t]", " ")
                .trim();

        if (cleaned.length() > 180) {
            cleaned = cleaned.substring(0, 180);
        }

        return cleaned.isBlank() ? "file" : cleaned;
    }
}

