package ru.rtkmagistral.magistralapi.dto.resend_token;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class ResendTokenDTO {
    private final int code;
    private String message;
}
