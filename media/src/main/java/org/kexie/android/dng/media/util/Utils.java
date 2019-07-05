package org.kexie.android.dng.media.util;

import java.util.Calendar;
import java.util.Locale;

public class Utils {
    public static String getProgressTime(int total) {
        total /= 1000;
        int minute = total / 60;
        int second = total % 60;
        minute %= 60;
        return String.format(Locale.CHINA, "%02d:%02d", minute, second);
    }

    public static int safeUnBoxInt(Long value) {
        return (int) safeUnBox(value);
    }

    public static long safeUnBox(Long value) {
        return value == null ? 0 : value;
    }

    public static boolean safeUnBox(Boolean b) {
        return b != null && b;
    }

    public static boolean isSameMouth(Calendar calendar1, Calendar calendar2) {
        return calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR)
                && calendar1.get(Calendar.MONTH) == calendar2.get(Calendar.MONTH);
    }
}
