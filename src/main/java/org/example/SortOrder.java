package org.example;

public enum SortOrder {
    PUBLISH_DATE_DESC("publishDateDesc"),
    PUBLISH_DATE_ASC("publishDateAsc"),
    PRICE_DESC("priceDesc"),
    PRICE_ASC("priceAsc");

    private final String value;

    SortOrder(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
