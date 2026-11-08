package io.github.rodiada.hrcomplication;

import android.os.SystemClock;

import androidx.health.services.client.PassiveListenerService;
import androidx.health.services.client.data.DataPointContainer;
import androidx.health.services.client.data.DataType;
import androidx.health.services.client.data.SampleDataPoint;

import java.time.Instant;
import java.util.List;

public class PassiveDataService extends PassiveListenerService {
    @Override
    public void onNewDataPointsReceived(DataPointContainer dataPoints) {
        List<SampleDataPoint<Double>> samples = dataPoints.getData(DataType.HEART_RATE_BPM);
        Instant boot = Instant.ofEpochMilli(System.currentTimeMillis() - SystemClock.elapsedRealtime());
        SampleDataPoint<Double> latest = null;
        long latestTime = 0;
        for (SampleDataPoint<Double> sample : samples) {
            long time = sample.getTimeInstant(boot).toEpochMilli();
            if (sample.getValue() > 0 && time >= latestTime) {
                latest = sample;
                latestTime = time;
            }
        }
        if (latest != null) {
            HeartRateStore.save(this, (int) Math.round(latest.getValue()), latestTime);
        }
    }
}
