package com.aapr.customcalendar.format;

import java.util.Collections;
import java.util.List;

public final class EventFormatResult {
    private final String text;
    private final List<int[]> nameRanges;

    public EventFormatResult(String text, List<int[]> nameRanges) {
        this.text = text;
        this.nameRanges = Collections.unmodifiableList(nameRanges);
    }

    public String getText() {
        return text;
    }

    public List<int[]> getNameRanges() {
        return nameRanges;
    }
}
