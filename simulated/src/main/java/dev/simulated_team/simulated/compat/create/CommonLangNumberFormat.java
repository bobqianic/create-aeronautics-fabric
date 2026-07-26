package dev.simulated_team.simulated.compat.create;

import java.text.NumberFormat;
import java.util.Locale;

public final class CommonLangNumberFormat {
    private CommonLangNumberFormat() {
    }

    public static String format(double value) {
        if (value == 0) {
            value = 0;
        }
        final NumberFormat format = NumberFormat.getNumberInstance(Locale.ROOT);
        format.setMaximumFractionDigits(2);
        format.setMinimumFractionDigits(0);
        format.setGroupingUsed(true);
        return format.format(value).replace('\u00a0', ' ');
    }
}
