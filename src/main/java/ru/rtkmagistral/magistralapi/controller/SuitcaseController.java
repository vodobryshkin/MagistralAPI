package ru.rtkmagistral.magistralapi.controller;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.rtkmagistral.magistralapi.dto.auth.AuthResponse;
import ru.rtkmagistral.magistralapi.dto.order.OrderResponse;
import ru.rtkmagistral.magistralapi.dto.order.OrderResponseDTO;
import ru.rtkmagistral.magistralapi.dto.suitcase.CreateSuitcaseRequest;
import ru.rtkmagistral.magistralapi.exception.ValidationResponse;
import ru.rtkmagistral.magistralapi.security.authorization.ForVerifiedUsers;
import ru.rtkmagistral.magistralapi.service.spec.ISuitcaseService;

import java.util.UUID;

/**
 * Контроллер, принимающий запросы идущие на эндпойнт "/suitcases".
 */
@Tag(
        name = "Операции с заявками на доставку чемоданов",
        description = "Операции, связанные с заявками на доставку чемоданов в Магистраль (оформление новой заявки)."
)
@RestController
@RequestMapping(
        value = "/suitcases",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
)
@RequiredArgsConstructor
public class SuitcaseController {
    private final ISuitcaseService suitcaseService;

    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Заявка на доставку чемодана была успешно оформлена.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = OrderResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Не удалось аутентифицировать пользователя.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class),
                            examples = {
                                    @ExampleObject(
                                            description = "Время жизни access-токена вышло.",
                                            name = "ACCESS_TOKEN_HAS_EXPIRED",
                                            value = "{\"message\":\"ACCESS_TOKEN_HAS_EXPIRED\"}"
                                    ),
                                    @ExampleObject(
                                            description = "Access-токен семантически некорректен.",
                                            name = "ACCESS_TOKEN_INVALID",
                                            value = "{\"message\":\"ACCESS_TOKEN_INVALID\"}"
                                    ),
                                    @ExampleObject(
                                            description = "Не удалось найти пользователя с email из access-токена.",
                                            name = "USER_NOT_FOUND",
                                            value = "{\"message\":\"USER_NOT_FOUND\"}"
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Переданные данные для оформления заявки на чемодан семантически некорректные.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ValidationResponse.class),
                            examples = {
                                    @ExampleObject(
                                            description = "Данные не соответствуют заявленным форматам, ввиду чего не прошли валидацию. Все возможные сообщения об ошибках валидации представлены в объекте-примере.",
                                            name = "VALIDATION_ERROR",
                                            value = """
                                                    {
                                                        "message": "VALIDATION_ERROR",
                                                        "validation_errors": {
                                                          "shippingAddress": [
                                                            "CANNOT_BE_BLANK"
                                                          ],
                                                          "widthCentiCm": [
                                                            "MUST_BE_GREATER_THAN_0"
                                                          ],
                                                          "weightGr": [
                                                            "MUST_BE_GREATER_THAN_0"
                                                          ],
                                                          "costOfInvestmentInKopeika": [
                                                            "MUST_BE_GREATER_THAN_0"
                                                          ],
                                                          "heightCentiCm": [
                                                            "MUST_BE_GREATER_THAN_0"
                                                          ],
                                                          "lengthCentiCm": [
                                                            "MUST_BE_GREATER_THAN_0"
                                                          ]
                                                        }
                                                    }
                                                    """
                                    )
                            }
                    )
            )
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    @ForVerifiedUsers
    public ResponseEntity<OrderResponse> createSuitcase(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Данные, необходимые для оформления заявки на доставку чемодана.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CreateSuitcaseRequest.class)
                    )
            )
            @RequestBody @Valid CreateSuitcaseRequest createSuitcaseRequest,
            @Parameter(
                    in = ParameterIn.HEADER,
                    name = "Idempotency-Key",
                    required = true,
                    description = """
                        UUID для идемпотентности оформления заявки.
                        При повторном запросе с тем же Idempotency-Key сервер должен вернуть результат первого выполнения (без создания дубля).
                        """,
                    example = "b3b6c2a2-9d6d-4c77-9d15-7b3c4bd1a2a9"
            )
            @RequestHeader("Idempotency-Key") UUID uuid,
            HttpServletRequest httpServletRequest,
            Authentication authentication
    ) {
        OrderResponseDTO orderResponseDTO = suitcaseService.createIdempotentSuitcase(
                uuid,
                authentication.getName(),
                createSuitcaseRequest,
                httpServletRequest.getMethod(),
                httpServletRequest.getRequestURI()
        );

        return ResponseEntity
                .status(orderResponseDTO.getCode())
                .headers(orderResponseDTO.getHeaders())
                .body(orderResponseDTO.getBody());
    }
}
