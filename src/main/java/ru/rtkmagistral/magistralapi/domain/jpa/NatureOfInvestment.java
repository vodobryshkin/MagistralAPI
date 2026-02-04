package ru.rtkmagistral.magistralapi.domain.jpa;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum NatureOfInvestment {
    ALCOHOLIC_PRODUCTS("Алкогольная продукция"),
    COSTUME_JEWELRY("Бижутерия"),
    BIOLOGICAL_MATERIALS("Биологические материалы"),
    HABERDASHERY_AND_TEXTILE_GOODS("Галантерея, изделия из текстиля"),
    DOCUMENTATION("Документация"),
    GOLD_BULLION_ORE_SCRAP_GRANULES("Золото (слитки, руда, лом и гранулы)"),
    GOLD_LIQUID("Золото в жидком виде"),
    GOLD_POWDER("Золото в порошке"),
    STONES("Камни"),
    HOUSEHOLD_AND_INDUSTRIAL_PIERCING_AND_CUTTING_ITEMS("Колющие и режущие предметы хозяйственно-бытового и производственного назначения"),
    CONTROL_AND_IDENTIFICATION_MARKS_KIZ("Контрольно-измерительные знаки (КИЗ)"),
    COSMETICS("Косметика"),
    CULTURAL_PROPERTY("Культурные ценности"),
    MEDICINES("Медикаменты"),
    METAL_PRODUCTS("Металлические изделия"),
    PLATINUM_GROUP_METALS_LIQUID("Металлы платиновой группы в жидком виде"),
    PLATINUM_GROUP_METALS_POWDER("Металлы платиновой группы в порошке"),
    PLATINUM_GROUP_METALS_BULLION_ORE_SCRAP_GRANULES("Металлы платиновой группы (слитки, руда, лом и гранулы)"),
    COINS("Монеты"),
    NON_METALLIC_PRODUCTS("Неметаллические изделия"),
    PRINTED_MATERIALS("Печатные материалы"),
    PARCELS_WITH_BATTERIES("Посылки, упаковки с вложением аккумуляторных батарей"),
    PARCELS_WITH_GAS_CONTAINERS("Посылки, упаковки с вложением емкостей с газом"),
    PARCELS_WITH_LIQUID_CONTAINERS("Посылки, упаковки с вложением емкостей с жидкостью"),
    DEVICES_AND_TOOLS("Приборы и инструменты"),
    FOOD_PRODUCTS("Продукты питания"),
    SILVER_BULLION_ORE_SCRAP_GRANULES("Серебро (слитки, руда, лом и гранулы)"),
    SILVER_LIQUID("Серебро в жидком виде"),
    SILVER_POWDER("Серебро в порошке"),
    HOUSEHOLD_CHEMICALS("Средства бытовой химии"),
    TOBACCO_PRODUCTS("Табачная продукция"),
    CHEMICAL_PRODUCTS("Химическая продукция"),
    FRAGILE_AND_BREAKABLE_ITEMS("Хрупкие и бьющиеся изделия"),
    SECURITIES("Ценные бумаги"),
    ELECTRICAL_AND_ELECTRONIC_EQUIPMENT("Электрическое и электронное оборудование"),
    JEWELRY("Ювелирные изделия");

    private final String russianTitle;
}
