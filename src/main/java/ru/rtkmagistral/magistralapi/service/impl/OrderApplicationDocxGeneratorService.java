package ru.rtkmagistral.magistralapi.service.impl;

import org.apache.poi.xwpf.usermodel.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.rtkmagistral.magistralapi.domain.jpa.Order;
import ru.rtkmagistral.magistralapi.dto.order.OrderBlock;
import ru.rtkmagistral.magistralapi.dto.order.OrderDocumentDTO;
import ru.rtkmagistral.magistralapi.dto.user.UserBlock;
import ru.rtkmagistral.magistralapi.service.spec.IOrderApplicationDocxGeneratorService;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class OrderApplicationDocxGeneratorService implements IOrderApplicationDocxGeneratorService {
    private static final String KEY_CUSTOMER_NAME = "Наименование Заказчика";
    private static final String KEY_CONTRACT = "Договор оказания услуг специальной связи по доставке Отправлений";
    private static final String KEY_SENDER_NAME = "Наименование отправителя";
    private static final String KEY_SENDER_ADDRESS = "Адрес отправителя (место получения Отправлений)";
    private static final String KEY_SENDER_CONTACT = "Контактное лицо и телефон отправителя";
    private static final String KEY_PICKUP_DATE = "Дата получения Отправлений Исполнителем";
    private static final String KEY_RECEIVER_NAME = "Наименование получателя";
    private static final String KEY_RECEIVER_ADDRESS = "Адрес получателя (место вручения Отправлений)";
    private static final String KEY_RECEIVER_CONTACT = "Представитель получателя, уполномоченный на получение Отправлений, его телефон";
    private static final String KEY_DELIVERY_DATE = "Предполагаемая дата вручения Отправлений получателю";
    private static final String KEY_TYPE = "Вид Отправлений";
    private static final String KEY_SECRET = "Важность Отправлений";
    private static final String KEY_NATURE = "Характер вложения (Индекс вложения)";
    private static final String KEY_COST = "Стоимость вложения";
    private static final String KEY_WEIGHT = "Вес брутто";
    private static final String KEY_PLACES = "Количество мест";
    private static final String KEY_DIMENSIONS = "Объем";
    private static final String KEY_EXTRA = "Информация о свойствах Отправления, об условиях поставки (перевозки) и иная информация, необходимая для доставки";

    private static final DateTimeFormatter RU_DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @Value("${mail.document.customer-name}")
    private String customerNameFixed;

    @Value("${mail.document.contract-text}")
    private String contractTextFixed;

    @Override
    public byte[] generate(InputStream template, OrderDocumentDTO dto) throws IOException {
        try (XWPFDocument doc = new XWPFDocument(template)) {
            Map<String, String> values = buildValues(dto);

            fillHeaderParagraphs(doc, dto.getApplicationNumber(), dto.getApplicationDate());
            fillTableBySecondColumnKey(doc, values);
            forceTimesNewRomanEverywhere(doc);

            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                doc.write(baos);
                return baos.toByteArray();
            }
        }
    }

    private Map<String, String> buildValues(OrderDocumentDTO dto) {
        UserBlock u = dto.getUser();
        OrderBlock o = dto.getOrder();

        String personName = safeJoin(" ", u.getSurname(), u.getName(), u.getFathersName()).trim();

        String senderName = (u.getCompanyName() != null && !u.getCompanyName().isBlank())
                ? u.getCompanyName()
                : personName;

        String senderAddress = o.getShippingAddress();
        String senderContact = safeJoin(", ", nullIfBlank(personName), nullIfBlank(u.getPhone())).trim();

        String receiverName = o.getReceiverFio();
        String receiverAddress = o.getArrivalAddress();
        String receiverContact = safeJoin(", ", nullIfBlank(o.getReceiverFio()), nullIfBlank(o.getReceiverPhone())).trim();

        String type = mapShipmentType(o.getTypeOfShipment());
        String secret = "Без грифа секретности";

        String nature = o.getNatureOfInvestment() != null ? o.getNatureOfInvestment().getRussianTitle() : "";

        String cost = formatKopeika(o.getCostOfInvestmentInKopeika());
        String weight = o.getWeightGr() + " г";
        String placesStr = String.valueOf(Math.max(1, dto.getPlaces()));
        String dims = formatDimensionsCm(o.getLengthCentiCm(), o.getWidthCentiCm(), o.getHeightCentiCm());

        Map<String, String> map = new HashMap<>();
        map.put(KEY_CUSTOMER_NAME, nullToEmpty(customerNameFixed));
        map.put(KEY_CONTRACT, nullToEmpty(contractTextFixed));
        map.put(KEY_SENDER_NAME, nullToEmpty(senderName));
        map.put(KEY_SENDER_ADDRESS, nullToEmpty(senderAddress));
        map.put(KEY_SENDER_CONTACT, nullToEmpty(senderContact));
        map.put(KEY_PICKUP_DATE, dto.getPickupDate() != null ? RU_DATE.format(dto.getPickupDate()) : "");
        map.put(KEY_RECEIVER_NAME, nullToEmpty(receiverName));
        map.put(KEY_RECEIVER_ADDRESS, nullToEmpty(receiverAddress));
        map.put(KEY_RECEIVER_CONTACT, nullToEmpty(receiverContact));
        map.put(KEY_DELIVERY_DATE, dto.getDeliveryDate() != null ? RU_DATE.format(dto.getDeliveryDate()) : "");
        map.put(KEY_TYPE, type);
        map.put(KEY_SECRET, secret);
        map.put(KEY_NATURE, nature);
        map.put(KEY_COST, cost);
        map.put(KEY_WEIGHT, weight);
        map.put(KEY_PLACES, placesStr);
        map.put(KEY_DIMENSIONS, dims);
        map.put(KEY_EXTRA, buildExtra(dto));
        return map;
    }

    /**
     * Формирует содержимое графы «иная информация»: пользовательский комментарий и итоговую
     * стоимость доставки (если она рассчитана). Обе части необязательны и при отсутствии опускаются.
     */
    private String buildExtra(OrderDocumentDTO dto) {
        String deliveryCost = dto.getPriceInKopeika() != null
                ? "Стоимость доставки: " + formatKopeika(dto.getPriceInKopeika())
                : null;
        return safeJoin("\n", nullIfBlank(dto.getExtraInfo()), deliveryCost);
    }

    private void fillHeaderParagraphs(XWPFDocument doc, String applicationNumber, OffsetDateTime applicationDate) {
        String num = nullToEmpty(applicationNumber);
        String date = applicationDate != null ? RU_DATE.format(applicationDate) : "";

        for (XWPFParagraph p : doc.getParagraphs()) {
            String t = p.getText();
            if (t == null) continue;

            if (t.contains("ЗАЯВКА №")) {
                replaceParagraphTextKeepSimple(p, "ЗАЯВКА № " + num);
            } else if (t.contains("20__ г.") || t.contains("«____»")) {
                if (!date.isBlank()) {
                    replaceParagraphTextKeepSimple(p, "«" + date + "»");
                }
            }
        }
    }

    private void fillTableBySecondColumnKey(XWPFDocument doc, Map<String, String> values) {
        for (XWPFTable table : doc.getTables()) {
            for (XWPFTableRow row : table.getRows()) {
                List<XWPFTableCell> cells = row.getTableCells();
                if (cells.size() < 3) continue;

                String key = normalize(cells.get(1).getText());
                if (key.isBlank()) continue;

                for (Map.Entry<String, String> e : values.entrySet()) {
                    if (normalize(e.getKey()).equals(key)) {
                        setCellText(cells.get(2), e.getValue());
                        break;
                    }
                }
            }
        }
    }

    private void setCellText(XWPFTableCell cell, String text) {
        int pCount = cell.getParagraphs().size();
        for (int i = pCount - 1; i >= 0; i--) {
            cell.removeParagraph(i);
        }
        XWPFParagraph p = cell.addParagraph();
        XWPFRun run = p.createRun();
        run.setText(nullToEmpty(text));
        applyTimesNewRoman(run);
    }

    private void replaceParagraphTextKeepSimple(XWPFParagraph p, String newText) {
        for (int i = p.getRuns().size() - 1; i >= 0; i--) {
            p.removeRun(i);
        }
        XWPFRun r = p.createRun();
        r.setText(newText);
        applyTimesNewRoman(r);
    }

    private String mapShipmentType(Order.TypeOfShipment t) {
        if (t == null) return "";
        return switch (t) {
            case PACKAGE -> "Посылка";
            case PACKET -> "Пакет";
        };
    }

    private String formatKopeika(Long kopeika) {
        if (kopeika == null) return "";
        BigDecimal rub = BigDecimal.valueOf(kopeika).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        return rub.toPlainString() + " руб.";
    }

    private String formatDimensionsCm(int lengthCentiCm, int widthCentiCm, int heightCentiCm) {
        BigDecimal l = BigDecimal.valueOf(lengthCentiCm).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal w = BigDecimal.valueOf(widthCentiCm).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal h = BigDecimal.valueOf(heightCentiCm).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        return l.stripTrailingZeros().toPlainString() + " x " +
                w.stripTrailingZeros().toPlainString() + " x " +
                h.stripTrailingZeros().toPlainString() + " см";
    }

    private String normalize(String s) {
        return (s == null ? "" : s)
                .replace('\u00A0', ' ')
                .replace("\n", " ")
                .replace("\r", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    private String nullIfBlank(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isBlank() ? null : t;
    }

    private String safeJoin(String sep, String... parts) {
        List<String> ok = new ArrayList<>();
        for (String p : parts) {
            if (p != null && !p.trim().isBlank()) ok.add(p.trim());
        }
        return String.join(sep, ok);
    }

    private void forceTimesNewRomanEverywhere(XWPFDocument doc) {
        for (XWPFParagraph p : doc.getParagraphs()) {
            for (XWPFRun r : p.getRuns()) {
                applyTimesNewRoman(r);
            }
        }
        for (XWPFTable t : doc.getTables()) {
            for (XWPFTableRow row : t.getRows()) {
                for (XWPFTableCell c : row.getTableCells()) {
                    for (XWPFParagraph p : c.getParagraphs()) {
                        for (XWPFRun r : p.getRuns()) {
                            applyTimesNewRoman(r);
                        }
                    }
                }
            }
        }
    }

    private void applyTimesNewRoman(XWPFRun run) {
        run.setFontFamily("Times New Roman");
        try {
            run.setFontFamily("Times New Roman", XWPFRun.FontCharRange.ascii);
            run.setFontFamily("Times New Roman", XWPFRun.FontCharRange.hAnsi);
            run.setFontFamily("Times New Roman", XWPFRun.FontCharRange.cs);
            run.setFontFamily("Times New Roman", XWPFRun.FontCharRange.eastAsia);
        } catch (Throwable ignored) {
        }
    }
}
