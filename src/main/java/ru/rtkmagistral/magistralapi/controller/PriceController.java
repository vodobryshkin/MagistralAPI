package ru.rtkmagistral.magistralapi.controller;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.rtkmagistral.magistralapi.dto.pricing.PriceCalculationResult;
import ru.rtkmagistral.magistralapi.dto.pricing.PriceQuoteRequest;
import ru.rtkmagistral.magistralapi.dto.pricing.PriceQuoteResponse;
import ru.rtkmagistral.magistralapi.exception.ValidationResponse;
import ru.rtkmagistral.magistralapi.security.authorization.ForVerifiedUsers;
import ru.rtkmagistral.magistralapi.service.spec.IPriceQuoteService;

/**
 * Контроллер предварительного расчёта стоимости доставки. Позволяет узнать цену до оформления
 * как обычного заказа ("/orders/price"), так и заявки на доставку чемодана ("/suitcases/price").
 */
@Tag(
        name = "Расчёт стоимости доставки",
        description = "Предварительный расчёт стоимости доставки без создания заказа."
)
@RestController
@RequestMapping(
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
)
@RequiredArgsConstructor
public class PriceController {
    private final IPriceQuoteService priceQuoteService;

    @org.springframework.beans.factory.annotation.Value("${suitcase.price-coefficient}")
    private double suitcasePriceCoefficient;

    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Стоимость доставки успешно рассчитана.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PriceQuoteResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Переданные данные для расчёта семантически некорректные.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ValidationResponse.class)
                    )
            )
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/orders/price")
    @ForVerifiedUsers
    public ResponseEntity<PriceQuoteResponse> calculateOrderPrice(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Данные, влияющие на стоимость доставки.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PriceQuoteRequest.class)
                    )
            )
            @RequestBody @Valid PriceQuoteRequest request
    ) {
        return ResponseEntity.ok(toResponse(priceQuoteService.quote(request)));
    }

    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Стоимость доставки чемодана успешно рассчитана.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PriceQuoteResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Переданные данные для расчёта семантически некорректные.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ValidationResponse.class)
                    )
            )
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/suitcases/price")
    @ForVerifiedUsers
    public ResponseEntity<PriceQuoteResponse> calculateSuitcasePrice(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Данные, влияющие на стоимость доставки чемодана.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PriceQuoteRequest.class)
                    )
            )
            @RequestBody @Valid PriceQuoteRequest request
    ) {
        return ResponseEntity.ok(toResponse(priceQuoteService.quote(request, suitcasePriceCoefficient)));
    }

    private PriceQuoteResponse toResponse(PriceCalculationResult result) {
        return new PriceQuoteResponse(result.priceInKopeika(), result.chargeableWeightKg());
    }
}
