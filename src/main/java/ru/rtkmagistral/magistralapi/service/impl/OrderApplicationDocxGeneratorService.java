package ru.rtkmagistral.magistralapi.service.impl;

import org.apache.poi.xwpf.usermodel.*;
import org.springframework.stereotype.Service;
import ru.rtkmagistral.magistralapi.domain.jpa.Order;
import ru.rtkmagistral.magistralapi.domain.jpa.User;
import ru.rtkmagistral.magistralapi.service.spec.IOrderApplicationDocxGeneratorService;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Сервис для генерации документа, соответствующего заявке.
 */
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

    /**
     * Генерирует DOCX-заявку по шаблону и данным заказа.
     *
     * @param templatePath путь к шаблону .docx
     * @param order заказ, из него берутся адреса/параметры груза/отправитель/получатель
     * @param applicationNumber номер заявки (в шапку "ЗАЯВКА № ...")
     * @param applicationDate дата заявки (в шапку, формат dd.MM.yyyy)
     * @param contractText строка с договором/офертой (для 2-й строки таблицы)
     * @param pickupDate дата забора отправления исполнителем (может быть null)
     * @param deliveryDate предполагаемая дата вручения (может быть null)
     * @param places количество мест (если < 1 — будет 1)
     * @param extraInfo доп. инфа для последней строки (может быть пусто/null)
     * @param companyName название компании для BUSINESS (если пусто — будет ФИО)
     * @return готовый docx в байтах
     * @throws IOException если шаблон не читается или docx не записывается
     */
    @Override
    public byte[] generate(
            Path templatePath,
            Order order,
            String applicationNumber,
            OffsetDateTime applicationDate,
            String contractText,
            OffsetDateTime pickupDate,
            OffsetDateTime deliveryDate,
            int places,
            String extraInfo,
            String companyName
    ) throws IOException {

        try (InputStream is = new FileInputStream(templatePath.toFile());
             XWPFDocument doc = new XWPFDocument(is)) {

            Map<String, String> values = buildValues(order, contractText, pickupDate, deliveryDate, places, extraInfo, companyName);

            fillHeaderParagraphs(doc, applicationNumber, applicationDate);
            fillTableBySecondColumnKey(doc, values);
            forceTimesNewRomanEverywhere(doc);

            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                doc.write(baos);
                return baos.toByteArray();
            }
        }
    }

    private Map<String, String> buildValues(
            Order order,
            String contractText,
            OffsetDateTime pickupDate,
            OffsetDateTime deliveryDate,
            int places,
            String extraInfo,
            String companyName
    ) {
        User u = order.getUser();

        String senderName = safeJoin(" ", u.getSurname(), u.getName(), u.getFathersName()).trim();
        String customerName = (u.getUserType() == User.UserType.BUSINESS && companyName != null && !companyName.isBlank())
                ? companyName
                : senderName;

        String senderAddress = order.getShippingAddress();
        String senderContact = safeJoin(", ", nullIfBlank(senderName), nullIfBlank(u.getPhone())).trim();

        String receiverName = order.getReceiverFio();
        String receiverAddress = order.getArrivalAddress();
        String receiverContact = safeJoin(", ", nullIfBlank(order.getReceiverFio()), nullIfBlank(order.getReceiverPhone())).trim();

        String type = mapShipmentType(order.getTypeOfShipment());
        String secret = order.isSecretCargo() ? "СЕКРЕТНО" : "Без грифа секретности";

        String nature = order.getNatureOfInvestment() != null ? order.getNatureOfInvestment().getRussianTitle() : "";

        String cost = formatKopeika(order.getCostOfInvestmentInKopeika());
        String weight = order.getWeightGr() + " г";
        String placesStr = String.valueOf(Math.max(1, places));
        String dims = (order.getLengthCentiCm() / 100) + " x " + (order.getWidthCentiCm() / 100) + " x " + (order.getHeightCentiCm() / 100) + " см";

        Map<String, String> map = new HashMap<>();
        map.put(KEY_CUSTOMER_NAME, customerName);
        map.put(KEY_CONTRACT, nullToEmpty(contractText));
        map.put(KEY_SENDER_NAME, senderName);
        map.put(KEY_SENDER_ADDRESS, nullToEmpty(senderAddress));
        map.put(KEY_SENDER_CONTACT, nullToEmpty(senderContact));
        map.put(KEY_PICKUP_DATE, pickupDate != null ? RU_DATE.format(pickupDate) : "");
        map.put(KEY_RECEIVER_NAME, nullToEmpty(receiverName));
        map.put(KEY_RECEIVER_ADDRESS, nullToEmpty(receiverAddress));
        map.put(KEY_RECEIVER_CONTACT, nullToEmpty(receiverContact));
        map.put(KEY_DELIVERY_DATE, deliveryDate != null ? RU_DATE.format(deliveryDate) : "");
        map.put(KEY_TYPE, type);
        map.put(KEY_SECRET, secret);
        map.put(KEY_NATURE, nature);
        map.put(KEY_COST, cost);
        map.put(KEY_WEIGHT, weight);
        map.put(KEY_PLACES, placesStr);
        map.put(KEY_DIMENSIONS, dims);
        map.put(KEY_EXTRA, nullToEmpty(extraInfo));
        return map;
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

    private String normalize(String s) {
        return (s == null ? "" : s)
                .replace('\u00A0', ' ')
                .replace("\n", " ")
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
