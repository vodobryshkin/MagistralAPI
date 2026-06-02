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
        ResolvedLocation sender = cityResolver.resolve(request.getShippingAddress());
        ResolvedLocation receiver = cityResolver.resolve(request.getArrivalAddress());

        PriceCalculationInput input = new PriceCalculationInput(
                sender.city(),
                receiver.city(),
                Boolean.TRUE.equals(request.getShippingFromOffice()),
                Boolean.TRUE.equals(request.getArrivalToOffice()),
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

        return priceCalculationService.calculate(input);
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
        return quote(request).priceInKopeika();
    }
}
