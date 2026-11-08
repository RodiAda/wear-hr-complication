package io.github.rodiada.hrcomplication;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Looper;

final class OneShotMeasurement implements SensorEventListener {
    private static final long TIMEOUT_MILLIS = 30_000;
    private static final long MIN_INTERVAL_MILLIS = 10 * 60 * 1000L;
    private static long lastStart;

    private final Context context;
    private final SensorManager sensorManager;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable timeout = this::stop;

    private OneShotMeasurement(Context context, SensorManager sensorManager) {
        this.context = context;
        this.sensorManager = sensorManager;
    }

    static synchronized void startIfStale(Context context) {
        long now = System.currentTimeMillis();
        if (now - HeartRateStore.time(context) < MIN_INTERVAL_MILLIS || now - lastStart < MIN_INTERVAL_MILLIS) {
            return;
        }
        Context app = context.getApplicationContext();
        SensorManager manager = app.getSystemService(SensorManager.class);
        Sensor sensor = manager == null ? null : manager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
        if (sensor == null) {
            return;
        }
        lastStart = now;
        OneShotMeasurement measurement = new OneShotMeasurement(app, manager);
        if (manager.registerListener(measurement, sensor, SensorManager.SENSOR_DELAY_NORMAL)) {
            measurement.handler.postDelayed(measurement.timeout, TIMEOUT_MILLIS);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.values.length == 0 || event.values[0] <= 0
                || event.accuracy < SensorManager.SENSOR_STATUS_ACCURACY_LOW) {
            return;
        }
        HeartRateStore.save(context, Math.round(event.values[0]), System.currentTimeMillis());
        stop();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private void stop() {
        handler.removeCallbacks(timeout);
        sensorManager.unregisterListener(this);
    }
}
