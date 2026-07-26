package com.aapr.customcalendar.widget;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.provider.CalendarContract;

import com.aapr.customcalendar.R;

/**
 * Content-trigger job that wakes up whenever the Calendar Provider changes, so widgets
 * refresh promptly instead of waiting for the ~30 minute updatePeriodMillis floor.
 * Content-trigger jobs fire once and must be re-registered after each firing.
 */
public final class CalendarChangeJobService extends JobService {

    private static final int JOB_ID = 1001;

    @Override
    public boolean onStartJob(JobParameters params) {
        Context appContext = getApplicationContext();
        AppWidgetManager manager = AppWidgetManager.getInstance(appContext);
        int[] appWidgetIds = manager.getAppWidgetIds(
                new ComponentName(appContext, CustomCalendarWidgetProvider.class));
        for (int appWidgetId : appWidgetIds) {
            manager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_list);
        }
        schedule(appContext);
        jobFinished(params, false);
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

    public static void schedule(Context context) {
        JobScheduler scheduler = context.getSystemService(JobScheduler.class);
        if (scheduler == null) {
            return;
        }
        JobInfo jobInfo = new JobInfo.Builder(JOB_ID, new ComponentName(context, CalendarChangeJobService.class))
                .addTriggerContentUri(new JobInfo.TriggerContentUri(CalendarContract.CONTENT_URI,
                        JobInfo.TriggerContentUri.FLAG_NOTIFY_FOR_DESCENDANTS))
                .setTriggerContentUpdateDelay(3000)
                .build();
        scheduler.schedule(jobInfo);
    }

    public static void cancel(Context context) {
        JobScheduler scheduler = context.getSystemService(JobScheduler.class);
        if (scheduler != null) {
            scheduler.cancel(JOB_ID);
        }
    }
}
