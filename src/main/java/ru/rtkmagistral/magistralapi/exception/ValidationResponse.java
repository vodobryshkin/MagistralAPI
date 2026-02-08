package ru.rtkmagistral.magistralapi.exception;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class ValidationResponse {
    private String message;

    @JsonProperty("validation_errors")
    private Map<String, List<String>> errors;
}
