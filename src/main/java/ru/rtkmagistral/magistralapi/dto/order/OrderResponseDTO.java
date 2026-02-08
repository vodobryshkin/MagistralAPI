package ru.rtkmagistral.magistralapi.dto.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpHeaders;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public final class OrderResponseDTO {
    private int code;
    private HttpHeaders headers;
    private final OrderResponse body;
}
