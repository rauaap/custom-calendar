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

    private WidgetPrefs() {
    }

    public static final class Settings {
        public final int nameColor;
        public final int dateColor;
        public final int backgroundColor;
        public final float fontSizeSp;
        public final String format;
        public final boolean useCalendarColorForName;
        public final boolean useCalendarColorForDate;

        public Settings(int nameColor, int dateColor, int backgroundColor, float fontSizeSp, String format,
                         boolean useCalendarColorForName, boolean useCalendarColorForDate) {
            this.nameColor = nameColor;
            this.dateColor = dateColor;
            this.backgroundColor = backgroundColor;
            this.fontSizeSp = fontSizeSp;
            this.format = format;
            this.useCalendarColorForName = useCalendarColorForName;
            this.useCalendarColorForDate = useCalendarColorForDate;
        }

        public static Settings defaults() {
            return new Settings(DEFAULT_NAME_COLOR, DEFAULT_DATE_COLOR, DEFAULT_BACKGROUND_COLOR,
                    DEFAULT_FONT_SIZE_SP, DEFAULT_FORMAT, DEFAULT_USE_CALENDAR_COLOR, DEFAULT_USE_CALENDAR_COLOR);
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
                p.getFloat(key("fontSize", appWidgetId), d.fontSizeSp),
                p.getString(key("format", appWidgetId), d.format),
                p.getBoolean(key("useCalColorName", appWidgetId), d.useCalendarColorForName),
                p.getBoolean(key("useCalColorDate", appWidgetId), d.useCalendarColorForDate));
    }

    public static void save(Context context, int appWidgetId, Settings settings) {
        prefs(context).edit()
                .putInt(key("nameColor", appWidgetId), settings.nameColor)
                .putInt(key("dateColor", appWidgetId), settings.dateColor)
                .putInt(key("bgColor", appWidgetId), settings.backgroundColor)
                .putFloat(key("fontSize", appWidgetId), settings.fontSizeSp)
                .putString(key("format", appWidgetId), settings.format)
                .putBoolean(key("useCalColorName", appWidgetId), settings.useCalendarColorForName)
                .putBoolean(key("useCalColorDate", appWidgetId), settings.useCalendarColorForDate)
                .apply();
    }

    public static void remove(Context context, int appWidgetId) {
        prefs(context).edit()
                .remove(key("nameColor", appWidgetId))
                .remove(key("dateColor", appWidgetId))
                .remove(key("bgColor", appWidgetId))
                .remove(key("fontSize", appWidgetId))
                .remove(key("format", appWidgetId))
                .remove(key("useCalColorName", appWidgetId))
                .remove(key("useCalColorDate", appWidgetId))
                .apply();
    }

    private static String key(String name, int appWidgetId) {
        return name + "_" + appWidgetId;
    }
}
