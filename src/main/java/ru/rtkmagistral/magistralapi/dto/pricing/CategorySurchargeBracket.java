package ru.rtkmagistral.magistralapi.dto.pricing;

/**
 * Интервал дополнительного сбора за оценочную стоимость вложения
 * (для ювелирных изделий, драгоценных металлов, драгоценных камней и бижутерии).
 *
 * @param from    нижняя граница оценочной стоимости в рублях (включительно).
 * @param to      верхняя граница в рублях включительно; {@code null} — без верхней границы.
 * @param base    базовый сбор в рублях.
 * @param percent процент, начисляемый с суммы, превышающей нижнюю границу интервала.
 */
public record CategorySurchargeBracket(
        long from,
        Long to,
        long base,
        double percent
) {
}
