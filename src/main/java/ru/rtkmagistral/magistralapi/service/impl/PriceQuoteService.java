package ru.rtkmagistral.magistralapi.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.rtkmagistral.magistralapi.domain.jpa.NatureOfInvestment;
import ru.rtkmagistral.magistralapi.dto.pricing.PriceCalculationInput;
import ru.rtkmagistral.magistralapi.dto.pricing.PriceCalculationResult;
import ru.rtkmagistral.magistralapi.dto.pricing.PriceQuoteRequest;
import ru.rtkmagistral.magistralapi.dto.pricing.ResolvedLocation;
import ru.rtkmagistral.magistralapi.service.spec.ICityResolver;
import ru.rtkmagistral.magistralapi.service.spec.IPriceCalculationService;
import ru.rtkmagistral.magistralapi.service.spec.IPriceQuoteService;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Реализация предварительного расчёта стоимости. Резолвит города отправления и получения по адресам
 * и применяет тарифную логику. Присланная клиентом цена не используется — стоимость считается заново.
 */
@Service
@RequiredArgsConstructor
public class PriceQuoteService implements IPriceQuoteService {
    private final ICityResolver cityResolver;
    private final IPriceCalculationService priceCalculationService;

    @Override
    public PriceCalculationResult quote(PriceQuoteRequest request) {
        return quote(request, 1.0);
    }

    @Override
    public PriceCalculationResult quote(PriceQuoteRequest request, double multiplier) {
        ResolvedLocation sender = cityResolver.resolve(request.getShippingAddress());
        ResolvedLocation receiver = cityResolver.resolve(request.getArrivalAddress());

        boolean senderIsOffice = request.getShippingFromOffice() != null
                ? request.getShippingFromOffice()
                : sender.office();
        boolean receiverIsOffice = request.getArrivalToOffice() != null
                ? request.getArrivalToOffice()
                : receiver.office();

        PriceCalculationInput input = new PriceCalculationInput(
                sender.city(),
                receiver.city(),
                senderIsOffice,
                receiverIsOffice,
                request.getWeightGr(),
                request.getLengthCentiCm(),
                request.getWidthCentiCm(),
                request.getHeightCentiCm(),
                request.getNatureOfInvestment() != null ? request.getNatureOfInvestment() : NatureOfInvestment.OTHER,
                request.getCostOfInvestmentInKopeika(),
                sender.coefficient(),
                receiver.coefficient(),
                sender.remotePerKg(),
                receiver.remotePerKg()
        );

        PriceCalculationResult result = priceCalculationService.calculate(input);
        return applyMultiplier(result, multiplier);
    }

    @Override
    public long calculatePriceInKopeika(String shippingAddress,
                                        String arrivalAddress,
                                        Boolean shippingFromOffice,
                                        Boolean arrivalToOffice,
                                        int weightGr,
                                        int lengthCentiCm,
                                        int widthCentiCm,
                                        int heightCentiCm,
                                        NatureOfInvestment natureOfInvestment,
                                        Long costOfInvestmentKopeika) {
        return calculatePriceInKopeika(
                shippingAddress, arrivalAddress, shippingFromOffice, arrivalToOffice,
                weightGr, lengthCentiCm, widthCentiCm, heightCentiCm,
                natureOfInvestment, costOfInvestmentKopeika, 1.0);
    }

    @Override
    public long calculatePriceInKopeika(String shippingAddress,
                                        String arrivalAddress,
                                        Boolean shippingFromOffice,
                                        Boolean arrivalToOffice,
                                        int weightGr,
                                        int lengthCentiCm,
                                        int widthCentiCm,
                                        int heightCentiCm,
                                        NatureOfInvestment natureOfInvestment,
                                        Long costOfInvestmentKopeika,
                                        double multiplier) {
        PriceQuoteRequest request = new PriceQuoteRequest(
                shippingAddress,
                shippingFromOffice,
                arrivalAddress,
                arrivalToOffice,
                lengthCentiCm,
                widthCentiCm,
                heightCentiCm,
                weightGr,
                costOfInvestmentKopeika,
                natureOfInvestment
        );
        return quote(request, multiplier).priceInKopeika();
    }

    /**
     * Домножает итоговую цену на коэффициент в самом конце расчёта (поверх НДС и скидки).
     * При коэффициенте 1.0 результат не меняется.
     */
    private PriceCalculationResult applyMultiplier(PriceCalculationResult result, double multiplier) {
        if (multiplier == 1.0) {
            return result;
        }
        long adjusted = BigDecimal.valueOf(result.priceInKopeika())
                .multiply(BigDecimal.valueOf(multiplier))
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact();
        return new PriceCalculationResult(
                adjusted,
                result.zone(),
                result.deliveryType(),
                result.chargeableWeightKg()
        );
    }
}
