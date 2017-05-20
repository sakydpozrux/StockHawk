package com.udacity.stockhawk.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created by sakydpozrux on 20/05/2017.
 */

public class StockFormatUtils {
    private static final DecimalFormat dollarFormatWithPlus;
    private static final DecimalFormat dollarFormat;
    private static final DecimalFormat percentageFormat;

    static {
        dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        dollarFormatWithPlus.setPositivePrefix("+$");
        percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
        percentageFormat.setMaximumFractionDigits(2);
        percentageFormat.setMinimumFractionDigits(2);
        percentageFormat.setPositivePrefix("+");
    }

    public static String getDollarFormat(float aFloat, boolean showPlusSign) {
        if (showPlusSign)
            return dollarFormatWithPlus.format(aFloat);

        return dollarFormat.format(aFloat);
    }

    public static String getPercentageFormat(float aFloat) {
        return percentageFormat.format(aFloat);
    }
}
