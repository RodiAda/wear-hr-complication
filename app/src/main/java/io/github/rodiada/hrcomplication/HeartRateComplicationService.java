package io.github.rodiada.hrcomplication;

import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.drawable.Icon;

import androidx.wear.watchface.complications.data.ComplicationData;
import androidx.wear.watchface.complications.data.ComplicationText;
import androidx.wear.watchface.complications.data.ComplicationType;
import androidx.wear.watchface.complications.data.MonochromaticImage;
import androidx.wear.watchface.complications.data.NoDataComplicationData;
import androidx.wear.watchface.complications.data.PlainComplicationText;
import androidx.wear.watchface.complications.data.RangedValueComplicationData;
import androidx.wear.watchface.complications.data.ShortTextComplicationData;
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceService;
import androidx.wear.watchface.complications.datasource.ComplicationRequest;

public class HeartRateComplicationService extends ComplicationDataSourceService {
    private static final float MIN_BPM = 40f;
    private static final float MAX_BPM = 200f;
    private static final long MAX_AGE_MILLIS = 3 * 60 * 60 * 1000L;

    @Override
    public void onComplicationActivated(int complicationInstanceId, ComplicationType type) {
        PassiveRegistration.register(this);
    }

    @Override
    public ComplicationData getPreviewData(ComplicationType type) {
        return build(type, 72);
    }

    @Override
    public void onComplicationRequest(ComplicationRequest request, ComplicationRequestListener listener) {
        PassiveRegistration.register(this);
        OneShotMeasurement.startIfStale(this);
        int bpm = HeartRateStore.bpm(this);
        boolean fresh = System.currentTimeMillis() - HeartRateStore.time(this) < MAX_AGE_MILLIS;
        ComplicationData data = bpm > 0 && fresh
                ? build(request.getComplicationType(), bpm)
                : new NoDataComplicationData(build(request.getComplicationType(), -1));
        try {
            listener.onComplicationData(data);
        } catch (android.os.RemoteException ignored) {
        }
    }

    private ComplicationData build(ComplicationType type, int bpm) {
        boolean placeholder = bpm < 0;
        ComplicationText text = placeholder
                ? ComplicationText.PLACEHOLDER
                : new PlainComplicationText.Builder(String.valueOf(bpm)).build();
        ComplicationText description = new PlainComplicationText.Builder(placeholder
                ? getString(R.string.no_data)
                : getString(R.string.content_description, bpm)).build();
        MonochromaticImage image = new MonochromaticImage.Builder(
                Icon.createWithResource(this, R.drawable.ic_heart)).build();
        PendingIntent tap = tapAction();
        if (type == ComplicationType.RANGED_VALUE) {
            float value = placeholder
                    ? RangedValueComplicationData.PLACEHOLDER
                    : Math.max(MIN_BPM, Math.min(MAX_BPM, bpm));
            RangedValueComplicationData.Builder builder = new RangedValueComplicationData.Builder(
                    value, MIN_BPM, MAX_BPM, description)
                    .setText(text)
                    .setMonochromaticImage(image);
            if (tap != null) {
                builder.setTapAction(tap);
            }
            return builder.build();
        }
        if (type == ComplicationType.SHORT_TEXT) {
            ShortTextComplicationData.Builder builder = new ShortTextComplicationData.Builder(text, description)
                    .setMonochromaticImage(image);
            if (tap != null) {
                builder.setTapAction(tap);
            }
            return builder.build();
        }
        return null;
    }

    private PendingIntent tapAction() {
        Intent intent = getPackageManager().getLaunchIntentForPackage("com.samsung.android.wear.shealth");
        if (intent == null) {
            return null;
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
    }
}
