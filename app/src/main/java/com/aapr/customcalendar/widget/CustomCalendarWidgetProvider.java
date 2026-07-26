package com.aapr.customcalendar.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.CalendarContract;
import android.view.View;
import android.widget.RemoteViews;

import com.aapr.customcalendar.R;

public final class CustomCalendarWidgetProvider extends AppWidgetProvider {

    /** Sent explicitly to this component only; see {@link #requestRefresh}. */
    private static final String ACTION_REFRESH_WIDGETS = "com.aapr.customcalendar.action.REFRESH_WIDGETS";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId);
        }
        // Re-registers in case a device reboot cleared the (unpersisted) content-trigger job.
        CalendarChangeJobService.schedule(context);
    }

    @Override
    public void onEnabled(Context context) {
        CalendarChangeJobService.schedule(context);
    }

    @Override
    public void onDisabled(Context context) {
        CalendarChangeJobService.cancel(context);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            WidgetPrefs.remove(context, appWidgetId);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ACTION_REFRESH_WIDGETS.equals(intent.getAction())) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
            if (appWidgetIds != null) {
                for (int appWidgetId : appWidgetIds) {
                    // The list's row views are cached by the adapter independently of the top-level
                    // widget RemoteViews — background/etc. below refresh with updateWidget(), but
                    // name/date color, font size, and padding live in per-row views that only
                    // re-render in response to this call. It has to come first: issued after
                    // updateWidget() it supersedes the still-pending RemoteViews update, and the
                    // new top-level views never reach the host.
                    appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_list);
                    updateWidget(context, appWidgetManager, appWidgetId);
                }
            }
            return;
        }
        super.onReceive(context, intent);
    }

    /**
     * Asks the provider to re-render the given widget. Callers outside this provider must go
     * through here rather than calling {@link #updateWidget} themselves: AppWidgetService flags a
     * widget whose provider hasn't completed an update broadcast (which is every widget that has a
     * configure Activity, since those never get the initial onUpdate) as "tracking update", and
     * while that flag is set it stores RemoteViews passed to updateAppWidget() but never forwards
     * them to the host — "Trying to notify widget update deferred for id: N" in logcat. The pending
     * views are only flushed when a broadcast to the provider completes, so doing the update inside
     * onReceive is what actually makes it reach the launcher.
     */
    public static void requestRefresh(Context context, int appWidgetId) {
        Intent intent = new Intent(context, CustomCalendarWidgetProvider.class)
                .setAction(ACTION_REFRESH_WIDGETS)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[]{appWidgetId});
        context.sendBroadcast(intent);
    }

    public static void updateWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        // The RemoteViews and their PendingIntents outlive the receiver context onUpdate hands us.
        context = context.getApplicationContext();
        WidgetPrefs.Settings settings = WidgetPrefs.load(context, appWidgetId);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        views.setInt(R.id.widget_root, "setBackgroundColor", settings.backgroundColor);

        if (CalendarEventRepository.hasPermission(context)) {
            Intent serviceIntent = new Intent(context, EventListRemoteViewsService.class);
            serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            serviceIntent.setData(Uri.parse("customcalendar://widget/" + appWidgetId));
            views.setRemoteAdapter(R.id.widget_list, serviceIntent);
            views.setEmptyView(R.id.widget_list, R.id.widget_empty);
            views.setViewVisibility(R.id.widget_list, View.VISIBLE);
            views.setTextViewText(R.id.widget_empty, context.getString(R.string.widget_empty_text));

            // Row clicks: template intent must leave data unset so each row's fill-in intent supplies it.
            // That makes it implicit, which needs FLAG_ALLOW_UNSAFE_IMPLICIT_INTENT alongside FLAG_MUTABLE
            // on API 34+ — this is the officially sanctioned opt-in for the RemoteViews collection-template
            // + fill-in-intent pattern (RemoteViews.setPendingIntentTemplate docs call this out explicitly).
            Intent rowTemplateIntent = new Intent(Intent.ACTION_VIEW).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent rowTemplatePendingIntent = PendingIntent.getActivity(context, 0, rowTemplateIntent,
                    PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_ALLOW_UNSAFE_IMPLICIT_INTENT);
            views.setPendingIntentTemplate(R.id.widget_list, rowTemplatePendingIntent);

            // Tapping the widget background (or the empty state) opens Calendar to today.
            Intent openCalendarIntent = new Intent(Intent.ACTION_VIEW)
                    .setData(CalendarContract.CONTENT_URI)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent openCalendarPendingIntent = PendingIntent.getActivity(context, appWidgetId,
                    openCalendarIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            views.setOnClickPendingIntent(R.id.widget_root, openCalendarPendingIntent);
        } else {
            views.setViewVisibility(R.id.widget_list, View.GONE);
            views.setTextViewText(R.id.widget_empty, context.getString(R.string.widget_permission_needed_text));

            Intent configIntent = new Intent(context, WidgetConfigureActivity.class);
            configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            configIntent.setData(Uri.parse("customcalendar://configure/" + appWidgetId));
            PendingIntent pendingIntent = PendingIntent.getActivity(context, appWidgetId, configIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent);
        }

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}
