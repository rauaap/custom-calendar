package com.aapr.customcalendar.widget;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public final class CalendarEventRepository {

    private CalendarEventRepository() {
    }

    public static boolean hasPermission(Context context) {
        return context.checkSelfPermission(Manifest.permission.READ_CALENDAR)
                == PackageManager.PERMISSION_GRANTED;
    }

    public static List<EventItem> loadUpcomingEvents(Context context, int lookaheadDays, int maxEvents) {
        List<EventItem> events = new ArrayList<>();
        if (!hasPermission(context)) {
            return events;
        }

        long now = System.currentTimeMillis();
        long rangeEnd = now + TimeUnit.DAYS.toMillis(lookaheadDays);

        Uri uri = CalendarContract.Instances.CONTENT_URI.buildUpon()
                .appendPath(String.valueOf(now))
                .appendPath(String.valueOf(rangeEnd))
                .build();

        String[] projection = {
                CalendarContract.Instances.EVENT_ID,
                CalendarContract.Instances.TITLE,
                CalendarContract.Instances.BEGIN,
                CalendarContract.Instances.END,
                CalendarContract.Instances.ALL_DAY,
                CalendarContract.Instances.DISPLAY_COLOR,
        };
        String sortOrder = CalendarContract.Instances.BEGIN + " ASC";

        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, sortOrder);
        if (cursor == null) {
            return events;
        }
        try {
            int idIdx = cursor.getColumnIndex(CalendarContract.Instances.EVENT_ID);
            int titleIdx = cursor.getColumnIndex(CalendarContract.Instances.TITLE);
            int beginIdx = cursor.getColumnIndex(CalendarContract.Instances.BEGIN);
            int endIdx = cursor.getColumnIndex(CalendarContract.Instances.END);
            int allDayIdx = cursor.getColumnIndex(CalendarContract.Instances.ALL_DAY);
            int displayColorIdx = cursor.getColumnIndex(CalendarContract.Instances.DISPLAY_COLOR);

            while (cursor.moveToNext() && events.size() < maxEvents) {
                long eventId = cursor.getLong(idIdx);
                String title = cursor.getString(titleIdx);
                long begin = cursor.getLong(beginIdx);
                long end = cursor.getLong(endIdx);
                boolean allDay = cursor.getInt(allDayIdx) != 0;
                int displayColor = cursor.getInt(displayColorIdx) | 0xFF000000;

                ZoneId zone = allDay ? ZoneOffset.UTC : ZoneId.systemDefault();
                ZonedDateTime start = Instant.ofEpochMilli(begin).atZone(zone);
                ZonedDateTime endTime = Instant.ofEpochMilli(end).atZone(zone);

                events.add(new EventItem(eventId, title == null ? "" : title, start, endTime, displayColor));
            }
        } finally {
            cursor.close();
        }
        return events;
    }
}
