package com.aapr.customcalendar.widget;

import java.time.ZonedDateTime;

public final class EventItem {
    private final long eventId;
    private final String title;
    private final ZonedDateTime start;
    private final ZonedDateTime end;
    private final int displayColor;

    public EventItem(long eventId, String title, ZonedDateTime start, ZonedDateTime end, int displayColor) {
        this.eventId = eventId;
        this.title = title;
        this.start = start;
        this.end = end;
        this.displayColor = displayColor;
    }

    public long getEventId() {
        return eventId;
    }

    public String getTitle() {
        return title;
    }

    public ZonedDateTime getStart() {
        return start;
    }

    public ZonedDateTime getEnd() {
        return end;
    }

    /** The event's (or its calendar's) color, per CalendarContract.Instances.DISPLAY_COLOR. */
    public int getDisplayColor() {
        return displayColor;
    }
}
