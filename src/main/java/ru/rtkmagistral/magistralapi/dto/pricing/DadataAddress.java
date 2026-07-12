package ru.rtkmagistral.magistralapi.dto.pricing;

/**
 * Структурированный адрес, возвращённый Dadata.
 * Для тарификации критичны код субъекта и фактический населённый пункт; район области
 * намеренно не используется как замена города/посёлка.
 *
 * @param countryIsoCode    двухбуквенный код страны.
 * @param region            название субъекта РФ.
 * @param regionFiasId      ФИАС/ГАР-код субъекта РФ.
 * @param regionIsoCode     ISO-код субъекта РФ, например {@code RU-LEN}.
 * @param area              район внутри субъекта, если присутствует.
 * @param areaFiasId        ФИАС/ГАР-код района.
 * @param city              город, если адрес относится к городу.
 * @param cityFiasId        ФИАС/ГАР-код города.
 * @param settlement        иной населённый пункт (село, посёлок и т.п.).
 * @param settlementFiasId  ФИАС/ГАР-код населённого пункта.
 * @param capitalMarker     2 — центр субъекта, 1 — центр района, 0 — прочее.
 * @param fiasLevel         уровень детализации адреса в ФИАС/ГАР.
 * @param unrestrictedValue полный адрес одной строкой.
 */
public record DadataAddress(
        String countryIsoCode,
        String region,
        String regionFiasId,
        String regionIsoCode,
        String area,
        String areaFiasId,
        String city,
        String cityFiasId,
        String settlement,
        String settlementFiasId,
        Integer capitalMarker,
        Integer fiasLevel,
        String unrestrictedValue
) {
    /**
     * Возвращает фактический населённый пункт. Район субъекта здесь не используется:
     * район не является корректной тарифной точкой.
     */
    public String localityName() {
        if (settlement != null && !settlement.isBlank()) {
            return settlement;
        }
        if (city != null && !city.isBlank()) {
            return city;
        }
        return null;
    }

    public String localityFiasId() {
        if (settlementFiasId != null && !settlementFiasId.isBlank()) {
            return settlementFiasId;
        }
        if (cityFiasId != null && !cityFiasId.isBlank()) {
            return cityFiasId;
        }
        return null;
    }

    public boolean isRegionalCapital() {
        return Integer.valueOf(2).equals(capitalMarker);
    }
}
