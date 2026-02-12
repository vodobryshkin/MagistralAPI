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
import ru.rtkmagistral.magistralapi.dto.order.CreateOrderRequest;
import ru.rtkmagistral.magistralapi.dto.order.OrderResponse;
import ru.rtkmagistral.magistralapi.dto.order.OrderResponseDTO;
import ru.rtkmagistral.magistralapi.exception.ValidationResponse;
import ru.rtkmagistral.magistralapi.security.authorization.ForVerifiedUsers;
import ru.rtkmagistral.magistralapi.service.spec.IOrdersService;

import java.util.UUID;

/**
 * Контроллер, принимающий запросы идущие на эндпойнт "/orders"
 */
@Tag(
        name = "Операции с заказами на доставку",
        description = "Операции, связанные с заказами на доставку (добавление нового заказа)."
)
@RestController
@RequestMapping(
        value = "/orders",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
)
@RequiredArgsConstructor
public class OrderController {
    private final IOrdersService ordersService;

    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Заказ был успешно создан.",
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
                    description = "Переданные данные для оформлении заказа семантически некорректные.",
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
                                                          "receiverFio": [
                                                            "CANNOT_BE_BLANK"
                                                          ],
                                                          "receiverPhone": [
                                                            "CANNOT_BE_BLANK",
                                                            "MUST_MATCH_FORMAT"
                                                          ],
                                                          "agreeWithTheTermsOfTheAgreement": [
                                                            "MUST_BE_TRUE"
                                                          ],
                                                          "widthCentiCm": [
                                                            "MUST_BE_GREATER_THAN_0"
                                                          ],
                                                          "shippingAddress": [
                                                            "CANNOT_BE_BLANK"
                                                          ],
                                                          "arrivalAddress": [
                                                            "CANNOT_BE_BLANK"
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
    public ResponseEntity<OrderResponse> createOrder(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Данные, необходимые для создания заказа.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CreateOrderRequest.class)
                    )
            )
            @RequestBody @Valid CreateOrderRequest createOrderRequest,
            @Parameter(
                    in = ParameterIn.HEADER,
                    name = "Idempotency-Key",
                    required = true,
                    description = """
                        UUID для идемпотентности создания заказа.
                        При повторном запросе с тем же Idempotency-Key сервер должен вернуть результат первого выполнения (без создания дубля).
                        """,
                    example = "b3b6c2a2-9d6d-4c77-9d15-7b3c4bd1a2a9"
            )
            @RequestHeader("Idempotency-Key") UUID uuid,
            HttpServletRequest httpServletRequest,
            Authentication authentication
    ) {
        OrderResponseDTO orderResponseDTO = ordersService.createIdempotentOrder(
                uuid,
                authentication.getName(),
                createOrderRequest,
                httpServletRequest.getMethod(),
                httpServletRequest.getRequestURI()
        );

        return ResponseEntity
                .status(orderResponseDTO.getCode())
                .headers(orderResponseDTO.getHeaders())
                .body(orderResponseDTO.getBody());
    }
}
