package com.aapr.customcalendar.widget;

import android.Manifest;
import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.aapr.customcalendar.EdgeToEdge;
import com.aapr.customcalendar.R;
import com.aapr.customcalendar.format.EventFormatter;

import java.time.ZonedDateTime;
import java.util.Locale;

public final class WidgetConfigureActivity extends Activity {

    private static final int REQUEST_CALENDAR_PERMISSION = 1;
    private static final int MIN_FONT_SP = 10;

    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    private int nameColor;
    private int dateColor;
    private int backgroundColor;
    private float fontSizeSp;
    private String format;
    private boolean useCalendarColorForName;
    private boolean useCalendarColorForDate;
    private boolean showEmptyText;

    private FrameLayout previewContainer;
    private TextView previewText;
    private TextView permissionBanner;
    private Button grantPermissionButton;
    private ColorSwatchView nameSwatch;
    private ColorSwatchView dateSwatch;
    private ColorSwatchView backgroundSwatch;
    private CheckBox nameCalendarColorCheckbox;
    private CheckBox dateCalendarColorCheckbox;
    private CheckBox showEmptyTextCheckbox;
    private SeekBar fontSeekBar;
    private TextView fontSizeValue;
    private EditText formatEdit;
    private Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        appWidgetId = extras != null
                ? extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
                : AppWidgetManager.INVALID_APPWIDGET_ID;

        Intent cancelResult = new Intent();
        cancelResult.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        setResult(RESULT_CANCELED, cancelResult);

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

        setContentView(R.layout.activity_widget_configure);
        EdgeToEdge.applyInsetPadding(findViewById(R.id.configure_root));

        previewContainer = findViewById(R.id.preview_container);
        previewText = findViewById(R.id.preview_text);
        permissionBanner = findViewById(R.id.text_permission_banner);
        grantPermissionButton = findViewById(R.id.button_grant_permission);
        nameSwatch = findViewById(R.id.swatch_name_color);
        dateSwatch = findViewById(R.id.swatch_date_color);
        backgroundSwatch = findViewById(R.id.swatch_background_color);
        nameCalendarColorCheckbox = findViewById(R.id.checkbox_name_calendar_color);
        dateCalendarColorCheckbox = findViewById(R.id.checkbox_date_calendar_color);
        showEmptyTextCheckbox = findViewById(R.id.checkbox_show_empty_text);
        fontSeekBar = findViewById(R.id.seekbar_font_size);
        fontSizeValue = findViewById(R.id.text_font_size_value);
        formatEdit = findViewById(R.id.edit_format);
        saveButton = findViewById(R.id.button_save);

        WidgetPrefs.Settings settings = WidgetPrefs.load(this, appWidgetId);
        nameColor = settings.nameColor;
        dateColor = settings.dateColor;
        backgroundColor = settings.backgroundColor;
        fontSizeSp = settings.fontSizeSp;
        format = settings.format;
        useCalendarColorForName = settings.useCalendarColorForName;
        useCalendarColorForDate = settings.useCalendarColorForDate;
        showEmptyText = settings.showEmptyText;

        nameSwatch.setColor(nameColor);
        dateSwatch.setColor(dateColor);
        backgroundSwatch.setColor(backgroundColor);
        fontSeekBar.setProgress(clampProgress(Math.round(fontSizeSp) - MIN_FONT_SP));
        formatEdit.setText(format);
        nameCalendarColorCheckbox.setChecked(useCalendarColorForName);
        dateCalendarColorCheckbox.setChecked(useCalendarColorForDate);
        showEmptyTextCheckbox.setChecked(showEmptyText);
        setSwatchEnabled(nameSwatch, !useCalendarColorForName);
        setSwatchEnabled(dateSwatch, !useCalendarColorForDate);

        nameSwatch.setOnClickListener(v -> ColorPickerDialog.show(this, nameColor, color -> {
            nameColor = color;
            nameSwatch.setColor(color);
            updatePreview();
        }));
        dateSwatch.setOnClickListener(v -> ColorPickerDialog.show(this, dateColor, color -> {
            dateColor = color;
            dateSwatch.setColor(color);
            updatePreview();
        }));
        backgroundSwatch.setOnClickListener(v -> ColorPickerDialog.show(this, backgroundColor, color -> {
            backgroundColor = color;
            backgroundSwatch.setColor(color);
            updatePreview();
        }));

        nameCalendarColorCheckbox.setOnCheckedChangeListener((buttonView, checked) -> {
            useCalendarColorForName = checked;
            setSwatchEnabled(nameSwatch, !checked);
        });
        dateCalendarColorCheckbox.setOnCheckedChangeListener((buttonView, checked) -> {
            useCalendarColorForDate = checked;
            setSwatchEnabled(dateSwatch, !checked);
        });

        showEmptyTextCheckbox.setOnCheckedChangeListener((buttonView, checked) -> showEmptyText = checked);

        fontSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                fontSizeSp = progress + MIN_FONT_SP;
                updatePreview();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        formatEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                format = s.toString();
                updatePreview();
            }
        });

        grantPermissionButton.setOnClickListener(v -> {
            if (shouldShowRequestPermissionRationale(Manifest.permission.READ_CALENDAR)) {
                requestPermissions(new String[]{Manifest.permission.READ_CALENDAR}, REQUEST_CALENDAR_PERMISSION);
            } else {
                Intent settingsIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                settingsIntent.setData(Uri.fromParts("package", getPackageName(), null));
                startActivity(settingsIntent);
            }
        });

        saveButton.setOnClickListener(v -> onSaveClicked());

        updatePreview();
        updatePermissionUi();
        if (!CalendarEventRepository.hasPermission(this)) {
            requestPermissions(new String[]{Manifest.permission.READ_CALENDAR}, REQUEST_CALENDAR_PERMISSION);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updatePermissionUi();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        updatePermissionUi();
    }

    private void updatePermissionUi() {
        boolean granted = CalendarEventRepository.hasPermission(this);
        permissionBanner.setVisibility(granted ? View.GONE : View.VISIBLE);
        grantPermissionButton.setVisibility(granted ? View.GONE : View.VISIBLE);
        saveButton.setEnabled(granted);
    }

    private void updatePreview() {
        previewContainer.setBackgroundColor(backgroundColor);
        ZonedDateTime sample = ZonedDateTime.now().plusDays(1).withHour(14).withMinute(30);
        CharSequence line = EventFormatter.format(format, getString(R.string.preview_sample_title), sample,
                nameColor, dateColor, Locale.getDefault());
        previewText.setText(line);
        previewText.setMaxLines(EventFormatter.countLines(line));
        previewText.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSizeSp);
        fontSizeValue.setText(Math.round(fontSizeSp) + "sp");
    }

    private int clampProgress(int progress) {
        return Math.max(0, Math.min(fontSeekBar.getMax(), progress));
    }

    private void setSwatchEnabled(ColorSwatchView swatch, boolean enabled) {
        swatch.setEnabled(enabled);
        swatch.setAlpha(enabled ? 1f : 0.35f);
    }

    private void onSaveClicked() {
        WidgetPrefs.Settings toSave = new WidgetPrefs.Settings(nameColor, dateColor, backgroundColor, fontSizeSp,
                format == null || format.isEmpty() ? WidgetPrefs.DEFAULT_FORMAT : format,
                useCalendarColorForName, useCalendarColorForDate, showEmptyText);
        WidgetPrefs.save(this, appWidgetId, toSave);

        // Deliberately not calling AppWidgetManager.updateAppWidget() from here: updates sent
        // outside a provider broadcast are held back by AppWidgetService and never reach the
        // launcher. The provider does the re-render in onReceive instead.
        CustomCalendarWidgetProvider.requestRefresh(this, appWidgetId);

        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        setResult(RESULT_OK, resultValue);
        finish();
    }
}
