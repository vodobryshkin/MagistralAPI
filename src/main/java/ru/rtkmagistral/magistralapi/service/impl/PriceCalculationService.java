package ru.rtkmagistral.magistralapi.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.rtkmagistral.magistralapi.domain.jpa.NatureOfInvestment;
import ru.rtkmagistral.magistralapi.dto.pricing.CategorySurchargeBracket;
import ru.rtkmagistral.magistralapi.dto.pricing.DeliveryType;
import ru.rtkmagistral.magistralapi.dto.pricing.PriceCalculationInput;
import ru.rtkmagistral.magistralapi.dto.pricing.PriceCalculationResult;
import ru.rtkmagistral.magistralapi.dto.pricing.TariffRow;
import ru.rtkmagistral.magistralapi.exception.PricingErrorCode;
import ru.rtkmagistral.magistralapi.exception.PricingException;
import ru.rtkmagistral.magistralapi.service.spec.IPriceCalculationService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Расчёт стоимости экспресс-отправления по справочникам из файла «Стоимость».
 * Последовательность: тариф по зоне и весу для выбранного типа доставки → региональный коэффициент →
 * доплата за отдалённость → сбор за категорию вложения → НДС 22% → скидка 0,85.
 */
@Service
@RequiredArgsConstructor
public class PriceCalculationService implements IPriceCalculationService {
    private static final BigDecimal VOLUMETRIC_DIVISOR = BigDecimal.valueOf(5000);
    private static final BigDecimal VAT_MULTIPLIER = new BigDecimal("1.22");
    private static final BigDecimal DISCOUNT_MULTIPLIER = new BigDecimal("0.85");
    private static final BigDecimal HALF_KG = new BigDecimal("0.5");

    /**
     * Категории вложения, к которым применяется дополнительный сбор за оценочную стоимость:
     * ювелирные изделия, бижутерия, драгоценные камни и драгоценные металлы.
     */
    private static final Set<NatureOfInvestment> VALUABLE_CATEGORIES = EnumSet.of(
            NatureOfInvestment.JEWELRY,
            NatureOfInvestment.COSTUME_JEWELRY,
            NatureOfInvestment.STONES,
            NatureOfInvestment.GOLD_BULLION_ORE_SCRAP_GRANULES,
            NatureOfInvestment.GOLD_LIQUID,
            NatureOfInvestment.GOLD_POWDER,
            NatureOfInvestment.SILVER_BULLION_ORE_SCRAP_GRANULES,
            NatureOfInvestment.SILVER_LIQUID,
            NatureOfInvestment.SILVER_POWDER,
            NatureOfInvestment.PLATINUM_GROUP_METALS_BULLION_ORE_SCRAP_GRANULES,
            NatureOfInvestment.PLATINUM_GROUP_METALS_LIQUID,
            NatureOfInvestment.PLATINUM_GROUP_METALS_POWDER
    );

    private final PricingReferenceData referenceData;

    @Override
    public PriceCalculationResult calculate(PriceCalculationInput input) {
        DeliveryType deliveryType = DeliveryType.of(input.senderIsOffice(), input.receiverIsOffice());

        Integer zone = referenceData.zone(input.senderCity(), input.receiverCity());
        if (zone == null) {
            throw new PricingException(PricingErrorCode.PRICING_ZONE_NOT_FOUND);
        }

        TariffRow tariff = referenceData.tariff(deliveryType, zone);
        if (tariff == null) {
            throw new PricingException(PricingErrorCode.PRICING_TARIFF_NOT_AVAILABLE);
        }

        BigDecimal chargeableWeight = chargeableWeightKg(input);

        BigDecimal base = baseTariff(tariff, chargeableWeight, input.receiverCity());

        BigDecimal coefficient = BigDecimal.valueOf(input.senderCoefficient())
                .multiply(BigDecimal.valueOf(input.receiverCoefficient()));
        BigDecimal withCoefficient = base.multiply(coefficient);

        BigDecimal remote = remoteSurcharge(input, chargeableWeight);
        BigDecimal category = categorySurcharge(input);

        BigDecimal beforeVat = withCoefficient.add(remote).add(category);
        BigDecimal total = beforeVat.multiply(VAT_MULTIPLIER).multiply(DISCOUNT_MULTIPLIER);

        long priceInKopeika = total.multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact();

        return new PriceCalculationResult(priceInKopeika, zone, deliveryType, chargeableWeight.doubleValue());
    }

    private BigDecimal chargeableWeightKg(PriceCalculationInput input) {
        BigDecimal actual = BigDecimal.valueOf(input.weightGr()).divide(BigDecimal.valueOf(1000), 6, RoundingMode.HALF_UP);

        BigDecimal lengthCm = centiCmToCm(input.lengthCentiCm());
        BigDecimal widthCm = centiCmToCm(input.widthCentiCm());
        BigDecimal heightCm = centiCmToCm(input.heightCentiCm());
        BigDecimal volumetric = lengthCm.multiply(widthCm).multiply(heightCm)
                .divide(VOLUMETRIC_DIVISOR, 6, RoundingMode.HALF_UP);

        return actual.max(volumetric);
    }

    private BigDecimal centiCmToCm(int centiCm) {
        return BigDecimal.valueOf(centiCm).divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);
    }

    private BigDecimal baseTariff(TariffRow tariff, BigDecimal weightKg, String receiverCity) {
        if (weightKg.compareTo(HALF_KG) <= 0) {
            Integer underHalf = referenceData.underHalfKgTariff(receiverCity);
            if (underHalf != null) {
                return BigDecimal.valueOf(underHalf);
            }
            return requireTariffValue(firstBracket(tariff));
        }

        List<Integer> brackets = tariff.brackets();
        if (weightKg.compareTo(BigDecimal.valueOf(5)) <= 0) {
            int index = bracketIndex(weightKg);
            return requireTariffValue(brackets.get(index));
        }
        if (weightKg.compareTo(BigDecimal.valueOf(10)) <= 0) {
            return stepped(requireTariffValue(brackets.get(8)), weightKg, 5, tariff.per5to10());
        }
        if (weightKg.compareTo(BigDecimal.valueOf(20)) <= 0) {
            return stepped(requireTariffValue(tariff.total10()), weightKg, 10, tariff.per10to20());
        }
        if (weightKg.compareTo(BigDecimal.valueOf(50)) <= 0) {
            return stepped(requireTariffValue(tariff.total20()), weightKg, 20, tariff.per20to50());
        }
        return stepped(requireTariffValue(tariff.total50()), weightKg, 50, tariff.perOver50());
    }

    private Integer firstBracket(TariffRow tariff) {
        return tariff.brackets().isEmpty() ? null : tariff.brackets().getFirst();
    }

    private BigDecimal stepped(BigDecimal baseAtThreshold, BigDecimal weightKg, int threshold, Integer perKg) {
        long extraKg = weightKg.subtract(BigDecimal.valueOf(threshold)).setScale(0, RoundingMode.CEILING).longValueExact();
        return baseAtThreshold.add(requireTariffValue(perKg).multiply(BigDecimal.valueOf(extraKg)));
    }

    private int bracketIndex(BigDecimal weightKg) {
        int index = weightKg.divide(HALF_KG, 0, RoundingMode.CEILING).intValueExact() - 2;
        if (index < 0) {
            return 0;
        }
        return Math.min(index, 8);
    }

    private BigDecimal requireTariffValue(Integer value) {
        if (value == null) {
            throw new PricingException(PricingErrorCode.PRICING_TARIFF_NOT_AVAILABLE);
        }
        return BigDecimal.valueOf(value);
    }

    private BigDecimal remoteSurcharge(PriceCalculationInput input, BigDecimal weightKg) {
        long chargeableKg = weightKg.setScale(0, RoundingMode.CEILING).longValueExact();
        BigDecimal perKgSum = BigDecimal.ZERO;
        if (input.senderRemotePerKg() != null) {
            perKgSum = perKgSum.add(BigDecimal.valueOf(input.senderRemotePerKg()));
        }
        if (input.receiverRemotePerKg() != null) {
            perKgSum = perKgSum.add(BigDecimal.valueOf(input.receiverRemotePerKg()));
        }
        return perKgSum.multiply(BigDecimal.valueOf(chargeableKg));
    }

    private BigDecimal categorySurcharge(PriceCalculationInput input) {
        if (!VALUABLE_CATEGORIES.contains(input.natureOfInvestment())) {
            return BigDecimal.ZERO;
        }
        BigDecimal valueRub = BigDecimal.valueOf(input.declaredValueKopeika())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        for (CategorySurchargeBracket bracket : referenceData.categorySurcharge()) {
            boolean aboveLower = valueRub.compareTo(BigDecimal.valueOf(bracket.from())) >= 0;
            boolean belowUpper = bracket.to() == null || valueRub.compareTo(BigDecimal.valueOf(bracket.to())) <= 0;
            if (aboveLower && belowUpper) {
                BigDecimal surcharge = BigDecimal.valueOf(bracket.base());
                if (bracket.percent() > 0) {
                    BigDecimal over = valueRub.subtract(BigDecimal.valueOf(bracket.from()));
                    BigDecimal percentPart = over.multiply(BigDecimal.valueOf(bracket.percent()))
                            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                    surcharge = surcharge.add(percentPart);
                }
                return surcharge;
            }
        }
        return BigDecimal.ZERO;
    }
}
