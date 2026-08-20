package com.aapr.customcalendar.widget;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;

public final class WidgetPrefs {

    private static final String PREFS_NAME = "widget_prefs";

    public static final int DEFAULT_NAME_COLOR = Color.parseColor("#00A676");
    public static final int DEFAULT_DATE_COLOR = Color.parseColor("#CCCCCC");
    public static final int DEFAULT_BACKGROUND_COLOR = Color.parseColor("#CC000000");
    public static final float DEFAULT_FONT_SIZE_SP = 14f;
    public static final String DEFAULT_FORMAT = "%E — %A %d %B, %H:%M";
    public static final boolean DEFAULT_USE_CALENDAR_COLOR = false;
    public static final boolean DEFAULT_SHOW_EMPTY_TEXT = true;
    public static final int DEFAULT_LOOKAHEAD_DAYS = 14;
    public static final int DEFAULT_MAX_EVENTS = 20;
    /** Square, i.e. what every widget looked like before the radius was configurable. */
    public static final int DEFAULT_CORNER_RADIUS_DP = 0;

    public static final int MIN_LOOKAHEAD_DAYS = 1;
    public static final int MAX_LOOKAHEAD_DAYS = 365;
    public static final int MIN_MAX_EVENTS = 1;
    public static final int MAX_MAX_EVENTS = 100;
    public static final int MAX_CORNER_RADIUS_DP = 48;

    private WidgetPrefs() {
    }

    /** Parses user input, falling back to {@code fallback} when it is empty or not a number. */
    public static int clampCount(String input, int min, int max, int fallback) {
        int value;
        try {
            value = Integer.parseInt(input.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
        return Math.max(min, Math.min(max, value));
    }

    public static final class Settings {
        public final int nameColor;
        public final int dateColor;
        public final int backgroundColor;
        public final int cornerRadiusDp;
        public final float fontSizeSp;
        public final String format;
        public final boolean useCalendarColorForName;
        public final boolean useCalendarColorForDate;
        public final boolean showEmptyText;
        public final int lookaheadDays;
        public final int maxEvents;

        public Settings(int nameColor, int dateColor, int backgroundColor, int cornerRadiusDp,
                         float fontSizeSp, String format,
                         boolean useCalendarColorForName, boolean useCalendarColorForDate,
                         boolean showEmptyText, int lookaheadDays, int maxEvents) {
            this.nameColor = nameColor;
            this.dateColor = dateColor;
            this.backgroundColor = backgroundColor;
            this.cornerRadiusDp = Math.max(0, Math.min(MAX_CORNER_RADIUS_DP, cornerRadiusDp));
            this.fontSizeSp = fontSizeSp;
            this.format = format;
            this.useCalendarColorForName = useCalendarColorForName;
            this.useCalendarColorForDate = useCalendarColorForDate;
            this.showEmptyText = showEmptyText;
            this.lookaheadDays = lookaheadDays;
            this.maxEvents = maxEvents;
        }

        public static Settings defaults() {
            return new Settings(DEFAULT_NAME_COLOR, DEFAULT_DATE_COLOR, DEFAULT_BACKGROUND_COLOR,
                    DEFAULT_CORNER_RADIUS_DP, DEFAULT_FONT_SIZE_SP, DEFAULT_FORMAT,
                    DEFAULT_USE_CALENDAR_COLOR, DEFAULT_USE_CALENDAR_COLOR,
                    DEFAULT_SHOW_EMPTY_TEXT, DEFAULT_LOOKAHEAD_DAYS, DEFAULT_MAX_EVENTS);
        }
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static Settings load(Context context, int appWidgetId) {
        SharedPreferences p = prefs(context);
        Settings d = Settings.defaults();
        return new Settings(
                p.getInt(key("nameColor", appWidgetId), d.nameColor),
                p.getInt(key("dateColor", appWidgetId), d.dateColor),
                p.getInt(key("bgColor", appWidgetId), d.backgroundColor),
                p.getInt(key("cornerRadius", appWidgetId), d.cornerRadiusDp),
                p.getFloat(key("fontSize", appWidgetId), d.fontSizeSp),
                p.getString(key("format", appWidgetId), d.format),
                p.getBoolean(key("useCalColorName", appWidgetId), d.useCalendarColorForName),
                p.getBoolean(key("useCalColorDate", appWidgetId), d.useCalendarColorForDate),
                p.getBoolean(key("showEmptyText", appWidgetId), d.showEmptyText),
                p.getInt(key("lookaheadDays", appWidgetId), d.lookaheadDays),
                p.getInt(key("maxEvents", appWidgetId), d.maxEvents));
    }

    public static void save(Context context, int appWidgetId, Settings settings) {
        prefs(context).edit()
                .putInt(key("nameColor", appWidgetId), settings.nameColor)
                .putInt(key("dateColor", appWidgetId), settings.dateColor)
                .putInt(key("bgColor", appWidgetId), settings.backgroundColor)
                .putInt(key("cornerRadius", appWidgetId), settings.cornerRadiusDp)
                .putFloat(key("fontSize", appWidgetId), settings.fontSizeSp)
                .putString(key("format", appWidgetId), settings.format)
                .putBoolean(key("useCalColorName", appWidgetId), settings.useCalendarColorForName)
                .putBoolean(key("useCalColorDate", appWidgetId), settings.useCalendarColorForDate)
                .putBoolean(key("showEmptyText", appWidgetId), settings.showEmptyText)
                .putInt(key("lookaheadDays", appWidgetId), settings.lookaheadDays)
                .putInt(key("maxEvents", appWidgetId), settings.maxEvents)
                .apply();
    }

    public static void remove(Context context, int appWidgetId) {
        prefs(context).edit()
                .remove(key("nameColor", appWidgetId))
                .remove(key("dateColor", appWidgetId))
                .remove(key("bgColor", appWidgetId))
                .remove(key("cornerRadius", appWidgetId))
                .remove(key("fontSize", appWidgetId))
                .remove(key("format", appWidgetId))
                .remove(key("useCalColorName", appWidgetId))
                .remove(key("useCalColorDate", appWidgetId))
                .remove(key("showEmptyText", appWidgetId))
                .remove(key("lookaheadDays", appWidgetId))
                .remove(key("maxEvents", appWidgetId))
                .apply();
    }

    private static String key(String name, int appWidgetId) {
        return name + "_" + appWidgetId;
    }
}
