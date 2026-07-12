package ru.rtkmagistral.magistralapi.exception;

/**
 * Машиночитаемые причины отказа при разрешении адреса и расчёте тарифа.
 */
public enum PricingErrorCode {
    PRICING_ADDRESS_NOT_RESOLVED,
    PRICING_LOCALITY_NOT_RESOLVED,
    PRICING_REGION_NOT_SUPPORTED,
    PRICING_COUNTRY_NOT_SUPPORTED,
    PRICING_ZONE_NOT_FOUND,
    PRICING_TARIFF_NOT_AVAILABLE,
    DADATA_UNAVAILABLE
}
