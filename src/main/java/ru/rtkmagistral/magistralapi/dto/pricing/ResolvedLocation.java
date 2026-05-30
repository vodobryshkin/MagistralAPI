package ru.rtkmagistral.magistralapi.dto.pricing;

/**
 * Результат разбора адреса: приведённый к таблице зон город, региональный коэффициент
 * и (если применимо) доплата за отдалённость.
 *
 * @param city        город из таблицы «Зональное распределение».
 * @param coefficient региональный коэффициент (1,0 — административный центр, 1,15 — Ленинградская область, 1,25 — иной субъект).
 * @param remotePerKg доплата за отдалённость, руб./кг; {@code null} — населённый пункт не в списке отдалённых.
 */
public record ResolvedLocation(
        String city,
        double coefficient,
        Integer remotePerKg
) {
}
