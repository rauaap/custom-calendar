package com.aapr.customcalendar;

import android.view.View;

/**
 * Apps targeting API 35+ get edge-to-edge content by default with no way to opt out,
 * so plain (non-AndroidX) activities must apply system bar insets as padding themselves
 * or content draws under the status/nav bars.
 */
public final class EdgeToEdge {

    private EdgeToEdge() {
    }

    public static void applyInsetPadding(View root) {
        int baseLeft = root.getPaddingLeft();
        int baseTop = root.getPaddingTop();
        int baseRight = root.getPaddingRight();
        int baseBottom = root.getPaddingBottom();
        root.setOnApplyWindowInsetsListener((v, insets) -> {
            v.setPadding(baseLeft + insets.getSystemWindowInsetLeft(), baseTop + insets.getSystemWindowInsetTop(),
                    baseRight + insets.getSystemWindowInsetRight(), baseBottom + insets.getSystemWindowInsetBottom());
            return insets;
        });
    }
}
