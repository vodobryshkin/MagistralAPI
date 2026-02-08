package ru.rtkmagistral.magistralapi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MinioDTO {
    private byte[] document;
    private String filename;
}
