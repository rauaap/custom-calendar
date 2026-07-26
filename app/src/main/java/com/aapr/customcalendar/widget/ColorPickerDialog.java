package com.aapr.customcalendar.widget;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/** Real HSV color picker: saturation/value square, hue slider, alpha slider, and a synced hex field. */
public final class ColorPickerDialog {

    public interface OnColorPickedListener {
        void onColorPicked(int color);
    }

    private ColorPickerDialog() {
    }

    public static void show(Context context, int currentColor, OnColorPickedListener listener) {
        int dp8 = dp(context, 8);
        int dp32 = dp(context, 32);
        int dp40 = dp(context, 40);
        int dp200 = dp(context, 200);

        float[] hsv = new float[3];
        Color.RGBToHSV(Color.red(currentColor), Color.green(currentColor), Color.blue(currentColor), hsv);
        int[] alpha255 = {Color.alpha(currentColor)};
        int[] resultColor = {currentColor};

        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(dp8, dp8, dp8, dp8);

        ColorSwatchView preview = new ColorSwatchView(context);
        preview.setColor(currentColor);
        LinearLayout.LayoutParams previewParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp40);
        previewParams.bottomMargin = dp8;
        container.addView(preview, previewParams);

        SatValSquareView svView = new SatValSquareView(context);
        svView.setHue(hsv[0]);
        svView.setSatVal(hsv[1], hsv[2]);
        LinearLayout.LayoutParams svParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp200);
        container.addView(svView, svParams);

        HueSliderView hueView = new HueSliderView(context);
        hueView.setHue(hsv[0]);
        LinearLayout.LayoutParams hueParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp32);
        hueParams.topMargin = dp8;
        container.addView(hueView, hueParams);

        AlphaSliderView alphaView = new AlphaSliderView(context);
        alphaView.setAlpha255(alpha255[0]);
        alphaView.setOpaqueColor(Color.HSVToColor(hsv));
        LinearLayout.LayoutParams alphaParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp32);
        alphaParams.topMargin = dp8;
        container.addView(alphaView, alphaParams);

        TextView opacityLabel = new TextView(context);
        opacityLabel.setText(opacityText(alpha255[0]));
        opacityLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        opacityLabel.setGravity(Gravity.END);
        container.addView(opacityLabel);

        // "#" is a fixed label, not part of the editable text, so it can never be deleted.
        LinearLayout hexRow = new LinearLayout(context);
        hexRow.setOrientation(LinearLayout.HORIZONTAL);
        hexRow.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams hexRowParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        hexRowParams.topMargin = dp8;

        TextView hashLabel = new TextView(context);
        hashLabel.setText("#");
        hashLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);

        EditText hexField = new EditText(context);
        hexField.setInputType(InputType.TYPE_CLASS_TEXT);
        hexField.setHint("RRGGBB or RRGGBBAA");
        hexField.setFilters(new InputFilter[]{new HexInputFilter(), new InputFilter.LengthFilter(8)});
        hexField.setText(toHexDigits(currentColor));
        // Select-all on focus so typing a new value replaces it outright, instead of partially
        // overwriting the old digits and accidentally leaving a stale trailing digit behind.
        hexField.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                hexField.selectAll();
            }
        });
        LinearLayout.LayoutParams hexFieldParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);

        hexRow.addView(hashLabel);
        hexRow.addView(hexField, hexFieldParams);
        container.addView(hexRow, hexRowParams);

        Runnable onColorUpdated = () -> {
            int opaque = Color.HSVToColor(hsv);
            int composed = (alpha255[0] << 24) | (opaque & 0x00FFFFFF);
            resultColor[0] = composed;
            preview.setColor(composed);
            alphaView.setOpaqueColor(opaque);
            opacityLabel.setText(opacityText(alpha255[0]));
            if (!hexField.hasFocus()) {
                hexField.setText(toHexDigits(composed));
            }
        };

        svView.setListener((sat, val) -> {
            hsv[1] = sat;
            hsv[2] = val;
            onColorUpdated.run();
        });
        hueView.setListener(hue -> {
            hsv[0] = hue;
            svView.setHue(hue);
            onColorUpdated.run();
        });
        alphaView.setListener(a -> {
            alpha255[0] = a;
            onColorUpdated.run();
        });

        hexField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!hexField.hasFocus()) {
                    return;
                }
                // The input filter guarantees only [0-9A-Fa-f] ever lands here; a length other
                // than 6 or 8 just means the user isn't done typing yet, not an error.
                Integer parsed = parseHexDigits(s.toString());
                if (parsed == null) {
                    return;
                }
                int color = parsed;
                alpha255[0] = Color.alpha(color);
                Color.RGBToHSV(Color.red(color), Color.green(color), Color.blue(color), hsv);
                svView.setHue(hsv[0]);
                svView.setSatVal(hsv[1], hsv[2]);
                hueView.setHue(hsv[0]);
                alphaView.setAlpha255(alpha255[0]);
                alphaView.setOpaqueColor(Color.HSVToColor(hsv));
                opacityLabel.setText(opacityText(alpha255[0]));
                resultColor[0] = color;
                preview.setColor(color);
            }
        });

        ScrollView scroll = new ScrollView(context);
        scroll.addView(container);

        new AlertDialog.Builder(context)
                .setTitle("Choose a color")
                .setView(scroll)
                .setPositiveButton("OK", (dialog, which) -> listener.onColorPicked(resultColor[0]))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private static String opacityText(int alpha255) {
        return "Opacity: " + Math.round((alpha255 / 255f) * 100) + "%";
    }

    /** Hex digits only, no leading #: RRGGBBAA (alpha last), not Android's usual AARRGGBB. */
    private static String toHexDigits(int color) {
        return String.format("%02X%02X%02X%02X", Color.red(color), Color.green(color), Color.blue(color),
                Color.alpha(color));
    }

    /** Returns null (not yet a complete color) unless digits is exactly 6 (RRGGBB) or 8 (RRGGBBAA) hex chars. */
    private static Integer parseHexDigits(String digits) {
        if (digits.length() == 6) {
            long rgb = Long.parseLong(digits, 16);
            int r = (int) ((rgb >> 16) & 0xFF);
            int g = (int) ((rgb >> 8) & 0xFF);
            int b = (int) (rgb & 0xFF);
            return 0xFF000000 | (r << 16) | (g << 8) | b;
        }
        if (digits.length() == 8) {
            long rgba = Long.parseLong(digits, 16);
            int r = (int) ((rgba >> 24) & 0xFF);
            int g = (int) ((rgba >> 16) & 0xFF);
            int b = (int) ((rgba >> 8) & 0xFF);
            int a = (int) (rgba & 0xFF);
            return (a << 24) | (r << 16) | (g << 8) | b;
        }
        return null;
    }

    /** Strips anything that isn't a hex digit and uppercases a-f, instead of rejecting the whole edit. */
    private static final class HexInputFilter implements InputFilter {
        @Override
        public CharSequence filter(CharSequence source, int start, int end, android.text.Spanned dest,
                                    int dstart, int dend) {
            StringBuilder sb = new StringBuilder(end - start);
            for (int i = start; i < end; i++) {
                char c = source.charAt(i);
                if (c >= '0' && c <= '9' || c >= 'A' && c <= 'F') {
                    sb.append(c);
                } else if (c >= 'a' && c <= 'f') {
                    sb.append(Character.toUpperCase(c));
                }
            }
            if (sb.length() == end - start) {
                return null;
            }
            return sb.toString();
        }
    }

    private static int dp(Context context, int value) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value,
                context.getResources().getDisplayMetrics());
    }
}
