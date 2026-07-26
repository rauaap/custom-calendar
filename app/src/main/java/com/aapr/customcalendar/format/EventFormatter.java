package com.aapr.customcalendar.format;

import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;

import java.time.ZonedDateTime;
import java.util.Locale;

/**
 * Renders an event line as a Spannable: the %E (event name) substitution
 * gets nameColor, everything else (date output and literal characters)
 * gets dateColor. Non-overlapping spans avoid depending on span draw order.
 */
public final class EventFormatter {

    private EventFormatter() {
    }

    public static Spannable format(String pattern, String eventTitle, ZonedDateTime date,
                                    int nameColor, int dateColor, Locale locale) {
        EventFormatResult result = EventFormatParser.parse(pattern, eventTitle, date, locale);
        String text = result.getText();
        SpannableStringBuilder ssb = new SpannableStringBuilder(text);

        int cursor = 0;
        for (int[] range : result.getNameRanges()) {
            if (range[0] > cursor) {
                ssb.setSpan(new ForegroundColorSpan(dateColor), cursor, range[0],
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            ssb.setSpan(new ForegroundColorSpan(nameColor), range[0], range[1],
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            cursor = range[1];
        }
        if (cursor < text.length()) {
            ssb.setSpan(new ForegroundColorSpan(dateColor), cursor, text.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return ssb;
    }

    /** Number of lines in formatted text, i.e. one more than its newline count. */
    public static int countLines(CharSequence text) {
        int lines = 1;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '\n') {
                lines++;
            }
        }
        return lines;
    }
}
