package com.aapr.customcalendar;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.TextView;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView view = new TextView(this);
        view.setText(R.string.main_instructions);
        view.setGravity(Gravity.CENTER);
        int padding = (int) (24 * getResources().getDisplayMetrics().density);
        view.setPadding(padding, padding, padding, padding);
        setContentView(view);
        EdgeToEdge.applyInsetPadding(view);
    }
}
