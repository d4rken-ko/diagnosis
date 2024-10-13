package eu.thedarken.diagnosis;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class DGautostart extends BroadcastReceiver {
    private final String TAG = "eu.thedarken.diagnosis.DGautostart";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "진단 자동 시작이 호출되었습니다.");
        Intent svc = new Intent(context, DGoverlay.class);
        context.startService(svc);
    }
}
