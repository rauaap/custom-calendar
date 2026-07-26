package com.aapr.customcalendar.widget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * One alarm per widget, set for the moment the earliest event on screen ends — the first time
 * after now that the widget's query would return a different list.
 *
 * Nothing else re-queries the calendar as time alone passes: the content-trigger job only fires
 * on calendar edits, and re-issuing the top-level RemoteViews does not reload the list (the host
 * keeps the adapter it already has for an identical service intent, so only an explicit
 * notifyAppWidgetViewDataChanged() produces new rows). Without this alarm a finished event stays
 * on the widget until the calendar happens to be edited.
 */
final class EventExpiryAlarm {

    /**
     * The system batches an inexact alarm to the far end of its window, so this is how late a
     * finished event can leave the widget. Exact alarms would need SCHEDULE_EXACT_ALARM, a
     * user-granted permission meant for alarm clocks, and API 31+ clamps windows shorter than
     * this anyway — so this is the promptness available without asking for anything.
     */
    private static final long WINDOW_MILLIS = TimeUnit.MINUTES.toMillis(10);

    /**
     * Floor on how soon an alarm may be set. A window that opens in the past fires immediately,
     * and since the refresh it triggers reschedules this alarm, a stale expiry could otherwise
     * spin.
     */
    private static final long MIN_DELAY_MILLIS = TimeUnit.MINUTES.toMillis(1);

    private EventExpiryAlarm() {
    }

    /** Arms the alarm for the earliest end time in {@code events}, or cancels it if there is none. */
    static void schedule(Context context, int appWidgetId, List<EventItem> events) {
        AlarmManager alarmManager = context.getSystemService(AlarmManager.class);
        if (alarmManager == null) {
            return;
        }
        PendingIntent operation = refreshOperation(context, appWidgetId);

        long now = System.currentTimeMillis();
        long earliestEnd = Long.MAX_VALUE;
        for (EventItem event : events) {
            long end = event.getEnd().toInstant().toEpochMilli();
            if (end > now && end < earliestEnd) {
                earliestEnd = end;
            }
        }
        if (earliestEnd == Long.MAX_VALUE) {
            alarmManager.cancel(operation);
            return;
        }

        // RTC rather than RTC_WAKEUP: a widget nobody is looking at is not worth a wakeup, and a
        // non-wakeup alarm is delivered as soon as the device is next awake — i.e. by the time the
        // screen is on and the widget is visible again.
        alarmManager.setWindow(AlarmManager.RTC, Math.max(earliestEnd, now + MIN_DELAY_MILLIS),
                WINDOW_MILLIS, operation);
    }

    static void cancel(Context context, int appWidgetId) {
        AlarmManager alarmManager = context.getSystemService(AlarmManager.class);
        if (alarmManager != null) {
            alarmManager.cancel(refreshOperation(context, appWidgetId));
        }
    }

    private static PendingIntent refreshOperation(Context context, int appWidgetId) {
        // Distinct data per widget so each widget's alarm is a separate PendingIntent: extras are
        // not part of the equality PendingIntent matches on, the Uri is.
        Intent intent = new Intent(context.getApplicationContext(), CustomCalendarWidgetProvider.class)
                .setAction(CustomCalendarWidgetProvider.ACTION_REFRESH_WIDGETS)
                .setData(Uri.parse("customcalendar://expiry/" + appWidgetId))
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[]{appWidgetId});
        return PendingIntent.getBroadcast(context.getApplicationContext(), 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }
}
