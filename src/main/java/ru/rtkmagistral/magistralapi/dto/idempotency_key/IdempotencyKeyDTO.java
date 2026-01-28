package ru.rtkmagistral.magistralapi.dto.idempotency_key;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

import java.util.UUID;

/**
 * DTO, которое используется для передачи параметров в сущность IdempotencyKey.
 */
@Data
@AllArgsConstructor
@ToString
public class IdempotencyKeyDTO {
    private UUID id;
    private String httpMethod;
    private String path;
    private String requestBody;
}
