package io.github.rodiada.hrcomplication;

import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester;

final class HeartRateStore {
    private static final String PREFS = "heart_rate";
    private static final String KEY_BPM = "bpm";
    private static final String KEY_TIME = "time";

    private HeartRateStore() {
    }

    static void save(Context context, int bpm, long timeMillis) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        if (timeMillis < prefs.getLong(KEY_TIME, 0)) {
            return;
        }
        prefs.edit().putInt(KEY_BPM, bpm).putLong(KEY_TIME, timeMillis).apply();
        ComplicationDataSourceUpdateRequester.create(
                context, new ComponentName(context, HeartRateComplicationService.class)).requestUpdateAll();
    }

    static int bpm(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt(KEY_BPM, 0);
    }

    static long time(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getLong(KEY_TIME, 0);
    }
}
