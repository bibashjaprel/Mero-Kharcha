package com.example.merokharcha.utils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class CurrencyUtils {
    public static String formatCurrency(double amount) {
        // Nepali/Indian formatting (Lakhs/Crores) is complex with standard Java Locale.
        // For simple thousands separator as requested (1,000 and 10,000), 
        // DecimalFormat with grouping works well.
        DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);
        formatter.applyPattern("#,##,###.##");
        
        // Remove .00 if it's a whole number for cleaner look
        String formatted = formatter.format(amount);
        if (formatted.endsWith(".00")) {
            formatted = formatted.substring(0, formatted.length() - 3);
        } else if (formatted.contains(".") && formatted.endsWith("0")) {
             // Optional: Handle 12.50 -> 12.5
        }
        
        return "Rs. " + formatted;
    }

    public static String formatCurrencyNoPrefix(double amount) {
        DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);
        formatter.applyPattern("#,##,###.##");
        String formatted = formatter.format(amount);
        if (formatted.endsWith(".00")) {
            formatted = formatted.substring(0, formatted.length() - 3);
        }
        return formatted;
    }
}