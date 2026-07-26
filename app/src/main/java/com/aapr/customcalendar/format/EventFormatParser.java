package com.aapr.customcalendar.format;

import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Parses a strftime-like format string. Supports a subset of the standard
 * Linux date(1)/strftime specifiers plus a custom %E for the event title.
 * %E is safe to repurpose because strftime only ever uses E as a modifier
 * prefix before another conversion char (%Ex, %EY, ...), never standalone.
 * Backslash escapes are also honoured, as date(1) does with its own \n and \t.
 */
public final class EventFormatParser {

    private EventFormatParser() {
    }

    public static EventFormatResult parse(String pattern, String eventTitle, ZonedDateTime date, Locale locale) {
        StringBuilder out = new StringBuilder();
        List<int[]> nameRanges = new ArrayList<>();

        int i = 0;
        int len = pattern.length();
        while (i < len) {
            char c = pattern.charAt(i);
            if (c == '\\' && i < len - 1) {
                char esc = pattern.charAt(i + 1);
                switch (esc) {
                    case 'n':
                        out.append('\n');
                        break;
                    case '\\':
                        out.append('\\');
                        break;
                    default:
                        out.append('\\').append(esc);
                        break;
                }
                i += 2;
                continue;
            }

            if (c != '%' || i == len - 1) {
                out.append(c);
                i++;
                continue;
            }

            char spec = pattern.charAt(i + 1);
            if (spec == 'E') {
                int start = out.length();
                out.append(eventTitle);
                nameRanges.add(new int[]{start, out.length()});
            } else {
                String resolved = resolve(spec, date, locale);
                if (resolved == null) {
                    out.append('%').append(spec);
                } else {
                    out.append(resolved);
                }
            }
            i += 2;
        }

        return new EventFormatResult(out.toString(), nameRanges);
    }

    private static String resolve(char spec, ZonedDateTime date, Locale locale) {
        switch (spec) {
            case 'Y':
                return String.format(locale, "%04d", date.getYear());
            case 'y':
                return String.format(locale, "%02d", date.getYear() % 100);
            case 'm':
                return String.format(locale, "%02d", date.getMonthValue());
            case 'd':
                return String.format(locale, "%02d", date.getDayOfMonth());
            case 'e':
                return String.format(locale, "%2d", date.getDayOfMonth());
            case 'A':
                return date.getDayOfWeek().getDisplayName(TextStyle.FULL, locale);
            case 'a':
                return date.getDayOfWeek().getDisplayName(TextStyle.SHORT, locale);
            case 'B':
                return date.getMonth().getDisplayName(TextStyle.FULL, locale);
            case 'b':
            case 'h':
                return date.getMonth().getDisplayName(TextStyle.SHORT, locale);
            case 'H':
                return String.format(locale, "%02d", date.getHour());
            case 'I': {
                int h = date.getHour() % 12;
                return String.format(locale, "%02d", h == 0 ? 12 : h);
            }
            case 'M':
                return String.format(locale, "%02d", date.getMinute());
            case 'p':
                return date.getHour() < 12 ? "AM" : "PM";
            case 'j':
                return String.format(locale, "%03d", date.getDayOfYear());
            case '%':
                return "%";
            default:
                return null;
        }
    }
}
