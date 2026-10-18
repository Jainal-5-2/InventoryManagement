package com.mycompany.inventorymanagement;

import java.text.NumberFormat;
import java.util.Locale;

public final class Currency {
    private static final NumberFormat FORMAT =
            NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-PH"));

    private Currency() {}

    public static String format(double amount) {
        return FORMAT.format(amount);
    }
}