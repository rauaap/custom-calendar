package com.aapr.customcalendar.widget;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.CalendarContract;
import android.util.TypedValue;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.aapr.customcalendar.R;
import com.aapr.customcalendar.format.EventFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class EventRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    private final Context context;
    private final int appWidgetId;
    private List<EventItem> events = new ArrayList<>();
    private WidgetPrefs.Settings settings = WidgetPrefs.Settings.defaults();

    public EventRemoteViewsFactory(Context context, int appWidgetId) {
        this.context = context;
        this.appWidgetId = appWidgetId;
    }

    @Override
    public void onCreate() {
        loadData();
    }

    @Override
    public void onDataSetChanged() {
        loadData();
    }

    private void loadData() {
        settings = WidgetPrefs.load(context, appWidgetId);
        events = CalendarEventRepository.hasPermission(context)
                ? CalendarEventRepository.loadUpcomingEvents(context, settings.lookaheadDays, settings.maxEvents)
                : new ArrayList<>();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        EventItem event = events.get(position);
        int nameColor = settings.useCalendarColorForName ? event.getDisplayColor() : settings.nameColor;
        int dateColor = settings.useCalendarColorForDate ? event.getDisplayColor() : settings.dateColor;
        RemoteViews row = new RemoteViews(context.getPackageName(), R.layout.widget_list_item);
        CharSequence line = EventFormatter.format(settings.format, event.getTitle(),
                event.getStart(), nameColor, dateColor, Locale.getDefault());
        row.setTextViewText(R.id.event_line, line);
        // Cap the row at the number of lines the format asks for, so a long title is
        // ellipsized instead of wrapping and making this row taller than the others.
        row.setInt(R.id.event_line, "setMaxLines", EventFormatter.countLines(line));
        row.setTextViewTextSize(R.id.event_line, TypedValue.COMPLEX_UNIT_SP, settings.fontSizeSp);

        int horizontalPaddingPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12,
                context.getResources().getDisplayMetrics());
        int verticalPaddingPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                settings.fontSizeSp * 0.3f, context.getResources().getDisplayMetrics());
        row.setViewPadding(R.id.event_line, horizontalPaddingPx, verticalPaddingPx,
                horizontalPaddingPx, verticalPaddingPx);

        Uri eventUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, event.getEventId());
        Intent fillInIntent = new Intent();
        fillInIntent.setData(eventUri);
        fillInIntent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, event.getStart().toInstant().toEpochMilli());
        fillInIntent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, event.getEnd().toInstant().toEpochMilli());
        row.setOnClickFillInIntent(R.id.event_line, fillInIntent);

        return row;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getCount() {
        return events.size();
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public void onDestroy() {
        events = new ArrayList<>();
    }
}
