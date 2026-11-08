package io.github.rodiada.hrcomplication;

import android.content.Context;
import android.util.Log;

import androidx.health.services.client.HealthServices;
import androidx.health.services.client.PassiveMonitoringClient;
import androidx.health.services.client.data.DataType;
import androidx.health.services.client.data.PassiveListenerConfig;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.Collections;

final class PassiveRegistration {
    private static final String TAG = "HrComplication";

    private PassiveRegistration() {
    }

    static void register(Context context) {
        Context app = context.getApplicationContext();
        PassiveMonitoringClient client = HealthServices.getClient(app).getPassiveMonitoringClient();
        PassiveListenerConfig config = PassiveListenerConfig.builder()
                .setDataTypes(Collections.singleton(DataType.HEART_RATE_BPM))
                .build();
        ListenableFuture<Void> future = client.setPassiveListenerServiceAsync(PassiveDataService.class, config);
        future.addListener(() -> {
            try {
                future.get();
            } catch (Exception e) {
                Log.e(TAG, "passive listener registration failed", e);
            }
        }, Runnable::run);
    }
}
